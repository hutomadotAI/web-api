package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.LogMap;
import com.hutoma.api.connectors.AIServices;
import com.hutoma.api.connectors.DatabaseEntitiesIntents;
import com.hutoma.api.connectors.HTMLExtractor;
import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.ApiTrainingMaterials;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.memory.IMemoryIntentHandler;
import com.hutoma.api.memory.MemoryIntentHandler;
import com.hutoma.api.validation.Validate;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Inject;

import static com.hutoma.api.common.ResultEvent.UPLOAD_MISSING_RESPONSE;
import static com.hutoma.api.common.ResultEvent.UPLOAD_NO_CONTENT;
import static com.hutoma.api.connectors.Database.DatabaseException;

/**
 * Logic to handle AI training.
 */
public class TrainingLogic {

    private static final String EMPTY_STRING = "";
    private static final String EOL = "\n";
    private static final String LOGFROM = "traininglogic";

    private final Config config;
    private final AIServices aiServices;
    private final HTMLExtractor htmlExtractor;
    private final DatabaseEntitiesIntents database;
    private final ILogger logger;
    private final Validate validate;
    private final IMemoryIntentHandler memoryIntentHandler;
    private final JsonSerializer jsonSerializer;

    @Inject
    public TrainingLogic(Config config, AIServices aiServices, HTMLExtractor htmlExtractor,
                         DatabaseEntitiesIntents database, ILogger logger, Validate validate,
                         IMemoryIntentHandler memoryIntentHandler, JsonSerializer jsonSerializer) {
        this.config = config;
        this.aiServices = aiServices;
        this.htmlExtractor = htmlExtractor;
        this.database = database;
        this.logger = logger;
        this.validate = validate;
        this.memoryIntentHandler = memoryIntentHandler;
        this.jsonSerializer = jsonSerializer;
    }

    public ApiResult uploadFile(final String devid, final UUID aiid, final TrainingType type, final String url,
                                final InputStream uploadedInputStream, final FormDataContentDisposition fileDetail) {

        ArrayList<String> source;

        long maxUploadFileSize = 1024L * this.config.getMaxUploadSizeKb();
        LogMap logMap = LogMap.map("AIID", aiid);

        try {
            switch (type) {

                // 0 = training file is text chat
                case TEXT:
                    if (fileDetail == null) {
                        this.logger.logUserTraceEvent(LOGFROM, "UploadFile - no file specified", devid, logMap);
                        return ApiError.getBadRequest("no file was specified");
                    }
                    checkMaxUploadFileSize(fileDetail, maxUploadFileSize);
                    source = getFile(maxUploadFileSize, uploadedInputStream);
                    TrainingFileParsingResult result = parseTrainingFile(source);
                    // Bail out if there are fatal events during the parsing
                    if (result.hasFatalEvents()) {
                        for (int i = 0; i < result.getEvents().size(); i++) {
                            logMap.put(String.format("event%d", i),
                                    String.format("%s - %s",
                                            result.getEvents().get(i).getKey().name(),
                                            result.getEvents().get(i).getValue()));
                        }
                        this.logger.logUserTraceEvent(LOGFROM, "UploadFile - file parsing errors", devid, logMap);
                        return ApiError.getBadRequest("File parsing errors", result.getEvents());
                    }
                    if (!this.database.updateAiTrainingFile(aiid, result.getTrainingText())) {
                        this.logger.logUserTraceEvent(LOGFROM, "UploadFile - AI not found", devid, logMap);
                        return ApiError.getNotFound();
                    }

                    String trainingMaterials = this.getTrainingMaterialsCommon(devid, aiid);
                    if (trainingMaterials == null) {
                        this.logger.logUserTraceEvent(LOGFROM, "UploadFile - training materials null after validation",
                                devid, logMap);
                        return ApiError.getInternalServerError();
                    }

                    this.aiServices.stopTrainingIfNeeded(devid, aiid);
                    return uploadTrainingFile(devid, aiid, trainingMaterials, result);

                // 1 = training file is a document
                case DOCUMENT:
                    if (null == fileDetail) {
                        this.logger.logUserTraceEvent(LOGFROM, "UploadDocument - no file specified", devid,
                                logMap);
                        return ApiError.getBadRequest("No file was specified");
                    }
                    checkMaxUploadFileSize(fileDetail, maxUploadFileSize);
                    source = getFile(maxUploadFileSize, uploadedInputStream);
                    if (!this.database.updateAiTrainingFile(aiid, String.join(EOL, source))) {
                        this.logger.logUserTraceEvent(LOGFROM, "UploadDocument - AI not found", devid, logMap);
                        return ApiError.getNotFound("ai not found");
                    }

                    // in case of an unstructured text we simply upload the file with no further processing
                    //  so unless the upload fails then we always return ok
                    this.logger.logUserTraceEvent(LOGFROM, "UploadDocument", devid, logMap);
                    return new ApiResult().setSuccessStatus("Document uploaded");

                // 2 = training file is a webpage
                case WEBPAGE:
                    if (!this.database.updateAiTrainingFile(aiid, getTextFromUrl(url, maxUploadFileSize))) {
                        this.logger.logUserTraceEvent(LOGFROM, "UploadWebPage - AI not found", devid, logMap);
                        return ApiError.getNotFound("Bot not found");
                    }
                    // in case of an unstructured webpage we simply upload the file with no further processing
                    //  so unless the upload fails then we always return ok
                    this.logger.logUserTraceEvent(LOGFROM, "UploadWebPage", devid, logMap);
                    return new ApiResult().setSuccessStatus("Document from url uploaded");

                default:
                    this.logger.logUserTraceEvent(LOGFROM, "UploadFile - incorrect training type", devid, logMap);
                    return ApiError.getBadRequest("Incorrect training type");
            }
        } catch (IOException ioe) {
            this.logger.logUserExceptionEvent(LOGFROM, "UploadFile", devid, ioe);
            return ApiError.getInternalServerError();
        } catch (HTMLExtractor.HtmlExtractionException ht) {
            logMap.put("Cause", ht.getCause().toString());
            this.logger.logUserTraceEvent(LOGFROM, "UploadFile - html extraction error", devid, logMap);
            return ApiError.getBadRequest("HTML extraction error");
        } catch (UploadTooLargeException tooLarge) {
            this.logger.logUserTraceEvent(LOGFROM, "UploadFile - upload attempt was larger than maximum allowed",
                    devid, logMap);
            return ApiError.getPayloadTooLarge();
        } catch (Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "UploadFile", devid, e);
            return ApiError.getInternalServerError();
        } finally {
            try {
                uploadedInputStream.close();
            } catch (Throwable ignore) {
                // Log an error since this can cause excessive open handles
                this.logger.logError(LOGFROM, "error closing file");
            }
        }
    }

    /**
     * Submit a training request to SQS only if the training file is avaialable or a previous
     * valid training session was stopped.
     * In all other cases we return an error
     * @param devid
     * @param aiid
     * @return
     */
    public ApiResult startTraining(final String devid, final UUID aiid) {

        ApiAi ai;
        LogMap logMap = LogMap.map("AIID", aiid);
        try {
            ai = this.database.getAI(devid, aiid, this.jsonSerializer);
        } catch (DatabaseException ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "StartTraining", devid, ex);
            return ApiError.getInternalServerError();
        }
        if (ai == null) {
            this.logger.logUserTraceEvent(LOGFROM, "StartTraining - AI not found", devid, logMap);
            return ApiError.getNotFound();
        }
        TrainingStatus trainingStatus = ai.getSummaryAiStatus();
        logMap.add("Start from state", trainingStatus.name());
        if (trainingStatus == TrainingStatus.AI_READY_TO_TRAIN
                || trainingStatus == TrainingStatus.AI_TRAINING_STOPPED) {
            try {
                this.aiServices.startTraining(devid, aiid);
            } catch (AIServices.AiServicesException | RuntimeException ex) {
                this.logger.logUserExceptionEvent(LOGFROM, "StartTraining", devid, ex);
                return ApiError.getInternalServerError();
            }
            // Delete all memory variables for this AI
            this.memoryIntentHandler.deleteAllIntentsForAi(aiid);
            this.logger.logUserTraceEvent(LOGFROM, "StartTraining", devid, logMap);
            return new ApiResult().setSuccessStatus("Training session started.");
        } else {
            this.logger.logUserTraceEvent(LOGFROM, "StartTraining - start in invalid state", devid, logMap);
            switch (trainingStatus) {
                case AI_TRAINING_COMPLETE:
                    return ApiError.getBadRequest("Training could not be started because it was already completed.");
                case AI_TRAINING:
                    return ApiError.getBadRequest("A training session is already running.");
                case AI_TRAINING_QUEUED:
                    return ApiError.getBadRequest("A training session is already queued.");
                case AI_UNDEFINED:
                    return ApiError.getBadRequest("No training file or not ready to train.");
                default:
                    return ApiError.getBadRequest("Malformed training file. Training could not be started.");
            }
        }

    }

    /**
     * Send a stop msg to SQS only if a training session is currently ongoing
     * @param devid
     * @param aiid
     * @return
     */

    public ApiResult stopTraining(final String devid, final UUID aiid) {
        try {
            LogMap logMap = LogMap.map("AIID", aiid);
            ApiAi ai = this.database.getAI(devid, aiid, this.jsonSerializer);
            if (ai == null) {
                this.logger.logUserTraceEvent(LOGFROM, "StopTraining - AI not found", devid, logMap);
                return ApiError.getNotFound();
            }
            TrainingStatus trainingStatus = ai.getSummaryAiStatus();
            if (trainingStatus == TrainingStatus.AI_TRAINING) {
                this.aiServices.stopTraining(devid, aiid);
                this.logger.logUserTraceEvent(LOGFROM, "StopTraining", devid, logMap);
                return new ApiResult().setSuccessStatus("Training session stopped.");
            } else {
                this.logger.logUserTraceEvent(LOGFROM, "StopTraining - AI not in an allowed state to stop training",
                        devid, logMap.put("AI state", trainingStatus.name()));
                return ApiError.getBadRequest("Bot not in an allowed state to stop training");
            }
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "StopTraining", devid, ex);
            return ApiError.getInternalServerError();
        }
    }

    /**
     * An update to an existing training session means we will have to delete any existing neural
     * network and start from scratch.
     * @param devId
     * @param aiid
     * @return
     */

    public ApiResult updateTraining(final String devId, final UUID aiid) {
        try {
            LogMap logMap = LogMap.map("AIID", aiid);
            ApiAi ai = this.database.getAI(devId, aiid, this.jsonSerializer);
            if (ai == null) {
                this.logger.logUserTraceEvent(LOGFROM, "UpdateTraining - AI not found", devId, logMap);
                return ApiError.getNotFound();
            }

            switch (ai.getSummaryAiStatus()) {
                case AI_TRAINING:           // fallthrough
                case AI_READY_TO_TRAIN:     // fallthrough
                case AI_TRAINING_STOPPED:   // fallthrough
                case AI_TRAINING_COMPLETE:  // fallthrough
                case AI_TRAINING_QUEUED:
                    String trainingFile = this.database.getAiTrainingFile(aiid);
                    try {
                        this.aiServices.uploadTraining(devId, aiid, trainingFile);
                        // Delete all memory variables for this AI
                        this.memoryIntentHandler.deleteAllIntentsForAi(aiid);
                        this.logger.logUserTraceEvent(LOGFROM, "UpdateTraining", devId, logMap);
                        return new ApiResult().setSuccessStatus("Training updated");
                    } catch (AIServices.AiServicesException ex) {
                        this.logger.logUserExceptionEvent(LOGFROM, "UpdateTraining", devId, ex);
                        return ApiError.getInternalServerError("Could not update training");
                    }

                default:
                    this.logger.logUserTraceEvent(LOGFROM, "UpdateTraining - could not update training", devId, logMap);
                    return ApiError.getInternalServerError("Could not update the current training session.");
            }
        } catch (Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "UpdateTraining", devId, e);
            return ApiError.getInternalServerError();
        }
    }

    /**
     * Gets the training materials for an AI.
     * @param devId
     * @param aiid
     * @return
     */
    public ApiResult getTrainingMaterials(final String devId, final UUID aiid) {
        try {
            LogMap logMap = LogMap.map("AIID", aiid);
            String trainingMaterials = this.getTrainingMaterialsCommon(devId, aiid);
            if (trainingMaterials == null) {
                this.logger.logUserTraceEvent(LOGFROM, "GetTrainingMaterials - AI not founf", devId, logMap);
                return ApiError.getNotFound();
            }
            ApiTrainingMaterials result = new ApiTrainingMaterials(trainingMaterials);
            this.logger.logUserTraceEvent(LOGFROM, "GetTrainingMaterials", devId, logMap);
            return result.setSuccessStatus();
        } catch (DatabaseException dbe) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetTrainingMaterials", devId, dbe);
            return ApiError.getInternalServerError();
        }
    }

    /**
     * Adds context to conversational exchanges
     * @param training list of strings, one for each line of conversation
     * @return single string with processed training
     */
    public TrainingFileParsingResult parseTrainingFile(final List<String> training) {

        TrainingFileParsingResult result = new TrainingFileParsingResult();
        String lastHumanSentence = EMPTY_STRING;
        List<String> validConversation = new ArrayList<>();
        boolean humanTalkingNow = true;
        boolean lastLineEmpty = true;

        for (String currentSentence : training) {

            // empty line means a new conversation exchange
            if (currentSentence.isEmpty()) {
                if (!lastLineEmpty) {
                    // If the last question didn't have an answer then
                    // ignore the last question
                    if (!humanTalkingNow) {
                        removeLastConversationEntry(validConversation);
                        result.addEvent(UPLOAD_MISSING_RESPONSE, lastHumanSentence);
                    }
                    // New conversation
                    validConversation.add(EMPTY_STRING);
                }
                humanTalkingNow = true;
                lastLineEmpty = true;
                continue;
            }

            validConversation.add(currentSentence);

            // if the AI is talking then store the response
            lastHumanSentence = currentSentence;

            humanTalkingNow = !humanTalkingNow;
            lastLineEmpty = false;
        }

        // add an empty line if there wasn't one already
        if (!lastLineEmpty) {
            validConversation.add(EMPTY_STRING);
        }

        // Check for missing response
        if (!humanTalkingNow) {
            // remove the last sentence
            removeLastConversationEntry(validConversation);
            result.addEvent(UPLOAD_MISSING_RESPONSE, lastHumanSentence);
        }

        if (validConversation.stream().anyMatch(s -> !s.isEmpty())) {
            StringBuilder parsedFile = new StringBuilder();
            validConversation.stream().forEach(s -> parsedFile.append(s).append(EOL));
            result.setTrainingText(parsedFile.toString());
        } else {
            result.addEvent(UPLOAD_NO_CONTENT, null);
            result.setTrainingText(EMPTY_STRING);
        }

        return result;
    }

    private String getTrainingMaterialsCommon(final String devId, final UUID aiid) throws DatabaseException {
        StringBuilder sb = new StringBuilder();
        ApiAi ai = this.database.getAI(devId, aiid, this.jsonSerializer);
        if (ai == null) {
            this.logger.logUserTraceEvent(LOGFROM, "GetTrainingMaterialsCommon - AI not found", devId,
                    LogMap.map("AIID", aiid));
            return null;
        }
        String userTrainingFile = this.database.getAiTrainingFile(aiid);
        if (userTrainingFile != null) {
            sb.append(userTrainingFile);
        }
        for (String intentName : this.database.getIntents(devId, aiid)) {
            ApiIntent intent = this.database.getIntent(devId, aiid, intentName);
            for (String userSays : intent.getUserSays()) {
                sb.append(EOL);
                sb.append(userSays).append(EOL);
                sb.append(MemoryIntentHandler.META_INTENT_TAG).append(intentName).append(EOL);
            }
        }
        return sb.toString();
    }

    /**
     * Download from a web-resource, sanitize each line and rejoin string.
     * @param url web-resource
     * @return clean result
     * @throws Exception
     */
    private String getTextFromUrl(final String url, final long maxUploadFileSize) throws Exception {
        // retrieve the url and extract the text
        String article = this.htmlExtractor.getTextFromUrl(url);

        if (article.length() > maxUploadFileSize) {
            throw new UploadTooLargeException();
        }

        // split into lines
        String[] text = article.split(EOL);

        // recombine the lines after they've been sanitised
        StringBuilder sb = new StringBuilder();
        for (String line : text) {
            sb.append(this.validate.textSanitizer(line)).append(EOL);
        }
        return sb.toString();
    }

    /**
     * Remove the last conversation item.
     * @param conversation the conversation so far
     */
    private void removeLastConversationEntry(final List<String> conversation) {
        for (int i = conversation.size() - 1; i >= 0; i--) {
            if (!conversation.get(i).equals(EMPTY_STRING)) {
                conversation.remove(i);
                break;
            }
        }
    }

    private void checkMaxUploadFileSize(final FormDataContentDisposition fileDetail, final long maxUploadFileSize)
            throws UploadTooLargeException {
        if (null != fileDetail) {
            if (fileDetail.getSize() > maxUploadFileSize) {
                throw new UploadTooLargeException();
            }
        }
    }

    /**
     * Reads from InputStream and returns a list of sanitised strings
     * @param maxUploadSize
     * @param uploadedInputStream
     * @return list of strings
     * @throws UploadTooLargeException
     * @throws IOException
     */
    private ArrayList<String> getFile(final long maxUploadSize, final InputStream uploadedInputStream)
            throws UploadTooLargeException, IOException {

        ArrayList<String> source = new ArrayList<>();
        long fileSize = 0;

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(uploadedInputStream, StandardCharsets.UTF_8));
            String line;
            int lineSize;
            while ((line = reader.readLine()) != null) {
                lineSize = line.length() + 2;
                // if the line doesn't push us over the upload limit
                if ((fileSize + lineSize) < maxUploadSize) {
                    source.add(this.validate.textSanitizer(line));
                    fileSize += lineSize;
                } else {
                    throw new UploadTooLargeException();
                }
            }
        } finally {
            if (null != reader) {
                reader.close();
            }
        }
        return source;
    }

    private ApiResult uploadTrainingFile(final String devId, final UUID aiid, final String trainingMaterials,
                                         final TrainingFileParsingResult result)
            throws DatabaseException {
        try {
            this.aiServices.uploadTraining(devId, aiid, trainingMaterials);
        } catch (AIServices.AiServicesException ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "UploadTrainingFile", devId, ex);
            return ApiError.getInternalServerError("Could not upload training data");
        }

        this.logger.logUserTraceEvent(LOGFROM, "UploadFile", devId, LogMap.map("AIID", aiid));
        return new ApiResult().setSuccessStatus("Upload complete",
                result.getEventCount() == 0 ? null : result.getEvents());
    }

    public enum TrainingType {
        TEXT(0),
        DOCUMENT(1),
        WEBPAGE(2);
        private final int type;

        TrainingType(final int type) {
            this.type = type;
        }

        /**
         * Obtains the enum value from the integer type
         * @param type the integer type
         * @return the enum value
         */
        public static TrainingType fromType(final int type) {
            Optional<TrainingType> optType = Arrays.stream(values()).filter(x -> x.type == type).findFirst();
            if (optType.isPresent()) {
                return optType.get();
            }
            throw new IllegalArgumentException("type not supported: " + type);
        }

        public int type() {
            return this.type;
        }
    }

    private static class UploadTooLargeException extends Exception {
    }
}