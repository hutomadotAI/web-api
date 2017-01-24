package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.connectors.AIServices;
import com.hutoma.api.connectors.DatabaseEntitiesIntents;
import com.hutoma.api.connectors.HTMLExtractor;
import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.ApiTrainingMaterials;
import com.hutoma.api.containers.sub.AiBot;
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
    private static final String PREVIOUS_AI_PREFIX = "[";
    private static final String PREVIOUS_AI_SUFFIX = "] ";
    private static final String EOL = "\n";
    private static final String LOGFROM = "traininglogic";
    private static final String HISTORY_REST_DIRECTIVE = "@reset";


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

        try {
            switch (type) {

                // 0 = training file is text chat
                case TEXT:
                    this.logger.logDebug(LOGFROM, "training from uploaded training file");
                    if (null == fileDetail) {
                        return ApiError.getBadRequest("no file was specified");
                    }
                    checkMaxUploadFileSize(fileDetail, maxUploadFileSize);
                    source = getFile(maxUploadFileSize, uploadedInputStream);
                    TrainingFileParsingResult result = parseTrainingFile(source);
                    // Bail out if there are fatal events during the parsing
                    if (result.hasFatalEvents()) {
                        return ApiError.getBadRequest("file parsing errors", result.getEvents());
                    }
                    if (!this.database.updateAiTrainingFile(aiid, result.getTrainingText())) {
                        return ApiError.getNotFound("ai not found");
                    }

                    String trainingMaterials = this.getTrainingMaterialsCommon(devid, aiid);
                    if (trainingMaterials == null) {
                        this.logger.logError(LOGFROM, "training materials are null after having passed validation!");
                        return ApiError.getInternalServerError();
                    }

                    return uploadCompositeTrainingFile(devid, aiid, trainingMaterials, result);

                // 1 = training file is a document
                case DOCUMENT:
                    this.logger.logDebug(LOGFROM, "training from uploaded document");
                    if (null == fileDetail) {
                        return ApiError.getBadRequest("no file was specified");
                    }
                    checkMaxUploadFileSize(fileDetail, maxUploadFileSize);
                    source = getFile(maxUploadFileSize, uploadedInputStream);
                    if (!this.database.updateAiTrainingFile(aiid, String.join(EOL, source))) {
                        return ApiError.getNotFound("ai not found");
                    }

                    // in case of an unstructured text we simply upload the file with no further processing
                    //  so unless the upload fails then we always return ok
                    return new ApiResult().setSuccessStatus("upload document accepted");

                // 2 = training file is a webpage
                case WEBPAGE:
                    this.logger.logDebug(LOGFROM, "training from uploaded URL");
                    if (!this.database.updateAiTrainingFile(aiid, getTextFromUrl(url, maxUploadFileSize))) {
                        return ApiError.getNotFound("ai not found");
                    }
                    // in case of an unstructured webpage we simply upload the file with no further processing
                    //  so unless the upload fails then we always return ok
                    return new ApiResult().setSuccessStatus("url training accepted");

                default:
                    return ApiError.getBadRequest("incorrect training type");
            }
        } catch (IOException ioe) {
            this.logger.logInfo(LOGFROM, "html extraction error " + ioe.toString());
            return ApiError.getBadRequest();
        } catch (HTMLExtractor.HtmlExtractionException ht) {
            this.logger.logInfo(LOGFROM, "html extraction error " + ht.getCause().toString());
            return ApiError.getBadRequest("html extraction error");
        } catch (UploadTooLargeException tooLarge) {
            this.logger.logInfo(LOGFROM, "upload attempt was larger than maximum allowed");
            return ApiError.getPayloadTooLarge();
        } catch (Exception e) {
            this.logger.logException(LOGFROM, e);
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

        this.logger.logDebug(LOGFROM, "on demand training start");
        ApiAi ai;
        try {
            ai = this.database.getAI(devid, aiid, this.jsonSerializer);
        } catch (DatabaseException ex) {
            this.logger.logException(LOGFROM, ex);
            return ApiError.getInternalServerError();
        }
        if (ai == null) {
            this.logger.logInfo(LOGFROM, "AI not found: " + aiid);
            return ApiError.getNotFound("AI not found");
        }
        TrainingStatus trainingStatus = ai.getSummaryAiStatus();
        this.logger.logDebug(LOGFROM, "training start from state " + trainingStatus);
        if (trainingStatus == TrainingStatus.AI_READY_TO_TRAIN
                || trainingStatus == TrainingStatus.AI_TRAINING_STOPPED) {
            try {
                this.aiServices.startTraining(devid, aiid);
            } catch (AIServices.AiServicesException | RuntimeException ex) {
                this.logger.logException(LOGFROM, ex);
                return ApiError.getInternalServerError();
            }
            // Delete all memory variables for this AI
            this.memoryIntentHandler.deleteAllIntentsForAi(aiid);
            return new ApiResult().setSuccessStatus("Training session started.");
        } else {
            this.logger.logInfo(LOGFROM, "Training start in invalid state: " + trainingStatus);
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
            this.logger.logDebug(LOGFROM, "on demand training stop");
            ApiAi ai = this.database.getAI(devid, aiid, this.jsonSerializer);
            if (ai == null) {
                return ApiError.getNotFound("AI not found");
            }
            TrainingStatus trainingStatus = ai.getSummaryAiStatus();

            if (trainingStatus == TrainingStatus.AI_TRAINING) {
                this.aiServices.stopTraining(devid, aiid);
                return new ApiResult().setSuccessStatus("Training session stopped.");
            }
        } catch (Exception ex) {
            this.logger.logException(LOGFROM, ex);
            return ApiError.getInternalServerError();

        }
        return ApiError.getBadRequest("AI not in an allowed state for stop training");
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
            ApiAi ai = this.database.getAI(devId, aiid, this.jsonSerializer);
            if (ai == null) {
                return ApiError.getNotFound("AI not found");
            }

            switch (ai.getSummaryAiStatus()) {
                case AI_TRAINING:           // fallthrough
                case AI_READY_TO_TRAIN:     // fallthrough
                case AI_TRAINING_STOPPED:   // fallthrough
                case AI_TRAINING_COMPLETE:  // fallthrough
                case AI_TRAINING_QUEUED:
                    this.logger.logDebug(LOGFROM, "on demand training update");
                    String trainingFile = this.database.getAiTrainingFile(aiid);
                    String finalMaterials = composeTrainingMaterials(devId, aiid, trainingFile);
                    try {
                        this.aiServices.uploadTraining(devId, aiid, finalMaterials);
                        // Delete all memory variables for this AI
                        this.memoryIntentHandler.deleteAllIntentsForAi(aiid);
                    } catch (AIServices.AiServicesException ex) {
                        this.logger.logException(LOGFROM, ex);
                        return ApiError.getInternalServerError("could not update training");
                    }
                    return new ApiResult().setSuccessStatus("training updated");

                default:
                    this.logger.logError(LOGFROM, "it was impossible to update training session for aiid:"
                            + aiid.toString() + " devid:" + devId);
                    return ApiError.getInternalServerError("Impossible to update the current training session.");
            }
        } catch (Exception e) {
            this.logger.logException(LOGFROM, e);
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
            String trainingMaterials = this.getTrainingMaterialsCommon(devId, aiid);
            if (trainingMaterials == null) {
                return ApiError.getNotFound("AI not found");
            }
            ApiTrainingMaterials result = new ApiTrainingMaterials(trainingMaterials);
            result.setSuccessStatus();
            return result;
        } catch (DatabaseException dbe) {
            this.logger.logException(LOGFROM, dbe);
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
        String lastAISentence = EMPTY_STRING;
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
                    // Check if the conversaton is completed and instruct the AI to reset the conversation history
                    if (validConversation.size() > 0) {
                        validConversation.set(validConversation.size() - 1,
                                validConversation.get(validConversation.size() - 1) + HISTORY_REST_DIRECTIVE);
                    }
                    validConversation.add(EMPTY_STRING);
                }
                humanTalkingNow = true;
                lastLineEmpty = true;
                continue;
            }

            // if it's the human's turn and there was a previous AI response
            // then we prepend it in square brackets
            if (humanTalkingNow && !lastAISentence.isEmpty() && !lastLineEmpty) {
                lastHumanSentence = currentSentence;
                validConversation.add(String.format("%s%s%s%s", PREVIOUS_AI_PREFIX,
                        lastAISentence, PREVIOUS_AI_SUFFIX, currentSentence));
            } else {
                // and we list the sentence
                validConversation.add(currentSentence);
            }
            // if the AI is talking then store the response
            if (!humanTalkingNow) {
                lastAISentence = currentSentence;
            }
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
            this.logger.logError(LOGFROM, String.format("AI id not found: %s", aiid));
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

    private String composeTrainingMaterials(final String devId, final UUID aiid, final String trainingFile)
            throws DatabaseException {
        // Check if there are any bots associated with this AI
        List<AiBot> bots = this.database.getBotsLinkedToAi(devId, aiid);
        StringBuilder sb = new StringBuilder();
        for (AiBot bot : bots) {
            // And add the training file
            sb.append(this.database.getAiTrainingFile(bot.getAiid()));
            sb.append(EOL).append(EOL);
        }
        sb.append(trainingFile);

        return sb.toString();
    }

    private ApiResult uploadCompositeTrainingFile(final String devId, final UUID aiid, final String trainingMaterials,
                                                  final TrainingFileParsingResult result)
            throws DatabaseException {
        String finalMaterials = composeTrainingMaterials(devId, aiid, trainingMaterials);

        try {
            this.aiServices.uploadTraining(devId, aiid, finalMaterials);
        } catch (AIServices.AiServicesException ex) {
            this.logger.logException(LOGFROM, ex);
            return ApiError.getInternalServerError("could not upload training data to AI servers");
        }

        return new ApiResult().setSuccessStatus("upload accepted",
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