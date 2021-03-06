package com.hutoma.api.logic;

import com.hutoma.api.common.*;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.BackendStatus;
import com.hutoma.api.connectors.aiservices.AIServices;
import com.hutoma.api.connectors.db.DatabaseAI;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.ApiTrainingMaterials;
import com.hutoma.api.containers.sub.AiIdentity;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.memory.IMemoryIntentHandler;
import com.hutoma.api.validation.Validate;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.hutoma.api.containers.sub.ResultEvent.UPLOAD_MISSING_RESPONSE;
import static com.hutoma.api.containers.sub.ResultEvent.UPLOAD_NO_CONTENT;

/**
 * Logic to handle AI training.
 */
public class TrainingLogic {

    public static final String TOPIC_MARKER = "@topic_out";

    private static final String EMPTY_STRING = "";
    private static final String EOL = "\n";
    private static final String LOGFROM = "traininglogic";

    private final Config config;
    private final AIServices aiServices;
    private final HTMLExtractor htmlExtractor;
    private final DatabaseAI databaseAi;
    private final ILogger logger;
    private final Validate validate;
    private final IMemoryIntentHandler memoryIntentHandler;
    private final JsonSerializer jsonSerializer;
    private final FeatureToggler featureToggler;
    private final LanguageLogic languageLogic;

    @Inject
    public TrainingLogic(final Config config,
                         final AIServices aiServices,
                         final HTMLExtractor htmlExtractor,
                         final DatabaseAI databaseAi,
                         final ILogger logger,
                         final Validate validate,
                         final IMemoryIntentHandler memoryIntentHandler,
                         final FeatureToggler featureToggler,
                         final JsonSerializer jsonSerializer,
                         final LanguageLogic languageLogic) {
        this.config = config;
        this.aiServices = aiServices;
        this.htmlExtractor = htmlExtractor;
        this.databaseAi = databaseAi;
        this.logger = logger;
        this.validate = validate;
        this.memoryIntentHandler = memoryIntentHandler;
        this.featureToggler = featureToggler;
        this.jsonSerializer = jsonSerializer;
        this.languageLogic = languageLogic;
    }

    public ApiResult uploadFile(final UUID devid,
                                final UUID aiid,
                                final TrainingType type,
                                final String url,
                                final InputStream uploadedInputStream,
                                final FormDataContentDisposition fileDetail) {

        ArrayList<String> source;
        final String devidString = devid.toString();
        long maxUploadFileSize = 1024L * this.config.getMaxUploadSizeKb();
        LogMap logMap = LogMap.map("AIID", aiid);

        try {
            ApiAi ai = this.databaseAi.getAI(devid, aiid, this.jsonSerializer);
            if (ai == null) {
                this.logger.logUserTraceEvent(LOGFROM, "UploadFile - AI not found", devidString, logMap);
                return ApiError.getNotFound();
            }
            if (ai.isReadOnly()) {
                this.logger.logUserTraceEvent(LOGFROM, "UploadFile - Bot is RO", devidString, logMap);
                return ApiError.getBadRequest(AILogic.BOT_RO_MESSAGE);
            }

            switch (type) {

                // 0 = training file is text chat
                case TEXT:
                    if (fileDetail == null) {
                        this.logger.logUserTraceEvent(LOGFROM, "UploadFile - no file specified", devidString, logMap);
                        return ApiError.getBadRequest("no file was specified");
                    }
                    checkMaxUploadFileSize(fileDetail, maxUploadFileSize);
                    source = getFile(maxUploadFileSize, uploadedInputStream);
                    TrainingFileParsingResult result = parseTrainingFile(source);
                    // Bail out if there are fatal events during the parsing
                    if (result.hasFatalEvents()) {
                        for (int i = 0; i < result.getEvents().size(); i++) {
                            logMap.add(String.format("event%d", i),
                                    String.format("%s - %s",
                                            result.getEvents().get(i).getKey().name(),
                                            result.getEvents().get(i).getValue()));
                        }
                        this.logger.logUserTraceEvent(LOGFROM, "UploadFile - file parsing errors", devidString, logMap);
                        return ApiError.getBadRequest("File parsing errors", result.getEvents());
                    }
                    if (!this.databaseAi.updateAiTrainingFile(aiid, result.getTrainingText())) {
                        this.logger.logUserTraceEvent(LOGFROM, "UploadFile - AI not found", devidString, logMap);
                        return ApiError.getNotFound();
                    }

                    String trainingMaterials = this.aiServices.getTrainingMaterialsCommon(devid, aiid,
                            this.jsonSerializer);
                    if (trainingMaterials == null) {
                        this.logger.logUserTraceEvent(LOGFROM, "UploadFile - training materials null after validation",
                                devidString, logMap);
                        return ApiError.getInternalServerError();
                    }
                    return uploadTrainingFile(ai, devid, aiid, trainingMaterials, result);

                // 1 = training file is a document
                case DOCUMENT:
                    if (null == fileDetail) {
                        this.logger.logUserTraceEvent(LOGFROM, "UploadDocument - no file specified", devidString,
                                logMap);
                        return ApiError.getBadRequest("No file was specified");
                    }
                    checkMaxUploadFileSize(fileDetail, maxUploadFileSize);
                    source = getFile(maxUploadFileSize, uploadedInputStream);
                    if (!this.databaseAi.updateAiTrainingFile(aiid, String.join(EOL, source))) {
                        this.logger.logUserTraceEvent(LOGFROM, "UploadDocument - AI not found", devidString, logMap);
                        return ApiError.getNotFound("ai not found");
                    }

                    // in case of an unstructured text we simply upload the file with no further processing
                    //  so unless the upload fails then we always return ok
                    this.logger.logUserTraceEvent(LOGFROM, "UploadDocument", devidString, logMap);
                    return new ApiResult().setSuccessStatus("Document uploaded");

                // 2 = training file is a webpage
                case WEBPAGE:
                    if (!this.databaseAi.updateAiTrainingFile(aiid, getTextFromUrl(url, maxUploadFileSize))) {
                        this.logger.logUserTraceEvent(LOGFROM, "UploadWebPage - AI not found", devidString, logMap);
                        return ApiError.getNotFound("Bot not found");
                    }
                    // in case of an unstructured webpage we simply upload the file with no further processing
                    //  so unless the upload fails then we always return ok
                    this.logger.logUserTraceEvent(LOGFROM, "UploadWebPage", devidString, logMap);
                    return new ApiResult().setSuccessStatus("Document from url uploaded");

                default:
                    this.logger.logUserTraceEvent(LOGFROM, "UploadFile - incorrect training type", devidString, logMap);
                    return ApiError.getBadRequest("Incorrect training type");
            }
        } catch (IOException ioe) {
            this.logger.logUserExceptionEvent(LOGFROM, "UploadFile", devidString, ioe);
            return ApiError.getInternalServerError();
        } catch (HTMLExtractor.HtmlExtractionException ht) {
            logMap.add("Cause", ht.getCause().toString());
            this.logger.logUserTraceEvent(LOGFROM, "UploadFile - html extraction error", devidString, logMap);
            return ApiError.getBadRequest("HTML extraction error");
        } catch (UploadTooLargeException tooLarge) {
            this.logger.logUserTraceEvent(LOGFROM, "UploadFile - upload attempt was larger than maximum allowed",
                    devidString, logMap);
            return ApiError.getPayloadTooLarge();
        } catch (Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "UploadFile", devidString, e);
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
     *
     * @param devid
     * @param aiid
     * @return
     */
    public ApiResult startTraining(final UUID devid, final UUID aiid) {

        ApiAi ai;
        LogMap logMap = LogMap.map("AIID", aiid);
        final String devidString = devid.toString();
        try {
            ai = this.databaseAi.getAIWithStatus(devid, aiid, this.jsonSerializer);
        } catch (DatabaseException ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "StartTraining", devidString, ex);
            return ApiError.getInternalServerError();
        }
        if (ai == null) {
            this.logger.logUserTraceEvent(LOGFROM, "StartTraining - AI not found", devidString, logMap);
            return ApiError.getNotFound();
        }
        logMap.add("EngineVersion", ai.getEngineVersion());
        if (ai.isReadOnly()) {
            this.logger.logUserTraceEvent(LOGFROM, "StartTraining - Bot is RO", devidString, logMap);
            return ApiError.getBadRequest(AILogic.BOT_RO_MESSAGE);
        }

        TrainingStatus trainingStatus = ai.getSummaryAiStatus();
        Locale locale = ai.getLanguage();
        Optional<SupportedLanguage> availableLanguage = languageLogic.getAvailableLanguage(locale, devid, aiid);
        if (!availableLanguage.isPresent()) {
            return ApiError.getBadRequest(String.format("Language not available %s", locale));
        }
        logMap.add("Start from state", trainingStatus.name());
        if (trainingStatus == TrainingStatus.AI_READY_TO_TRAIN
                || trainingStatus == TrainingStatus.AI_TRAINING_STOPPED) {
            try {
                this.aiServices.startTraining(ai.getBackendStatus(),
                        new AiIdentity(devid, aiid, availableLanguage.get(), ai.getEngineVersion()));
            } catch (AIServices.AiServicesException | RuntimeException ex) {
                this.logger.logUserExceptionEvent(LOGFROM, "StartTraining", devidString, ex);
                return ApiError.getInternalServerError();
            }
            // Delete all memory variables for this AI
            this.memoryIntentHandler.resetIntentsStateForAi(devid, aiid);
            this.logger.logUserTraceEvent(LOGFROM, "StartTraining", devidString, logMap);
            return new ApiResult().setSuccessStatus("Training session started.");
        } else {
            this.logger.logUserTraceEvent(LOGFROM, "StartTraining - start in invalid state", devidString, logMap);
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
     *
     * @param devId
     * @param aiid
     * @return
     */

    public ApiResult stopTraining(final UUID devId, UUID aiid) {
        final String devidString = devId.toString();
        LogMap logMap = LogMap.map("AIID", aiid);
        
        try {
            ApiAi ai = this.databaseAi.getAIWithStatus(devId, aiid, this.jsonSerializer);
            if (ai == null) {
                this.logger.logUserTraceEvent(LOGFROM, "StopTraining - AI not found", devidString, logMap);
                return ApiError.getNotFound();
            }
            Locale locale = ai.getLanguage();
            Optional<SupportedLanguage> availableLanguage = languageLogic.getAvailableLanguage(locale, devId, aiid);
            if (!availableLanguage.isPresent()) {
                return ApiError.getBadRequest(String.format("Language not available %s", locale));
            }
    
            logMap.add("EngineVersion", ai.getEngineVersion());
            if (ai.isReadOnly()) {
                this.logger.logUserTraceEvent(LOGFROM, "StopTraining - Bot is RO", devidString, logMap);
                return ApiError.getBadRequest(AILogic.BOT_RO_MESSAGE);
            }
            TrainingStatus trainingStatus = ai.getSummaryAiStatus();
            BackendStatus backendStatus = ai.getBackendStatus();
            TrainingStatus embStatus = backendStatus.getEngineStatus(BackendServerType.EMB).getTrainingStatus();
            if (embStatus == TrainingStatus.AI_TRAINING_QUEUED || embStatus == TrainingStatus.AI_TRAINING) {
                this.aiServices.stopTraining(backendStatus,
                        new AiIdentity(devId, aiid, availableLanguage.get(), ai.getEngineVersion()));
                this.logger.logUserTraceEvent(LOGFROM, "StopTraining", devidString, logMap);
                return new ApiResult().setSuccessStatus("Training session stopped.");
            } else {
                this.logger.logUserTraceEvent(LOGFROM, "StopTraining - AI not in an allowed state to stop training",
                        devidString, logMap.put("AI state", trainingStatus.name()));
                return ApiError.getBadRequest("Bot not in an allowed state to stop training");
            }
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "StopTraining", devidString, ex, logMap);
            return ApiError.getInternalServerError();
        }
    }

    /**
     * An update to an existing training session means we will have to delete any existing neural
     * network and start from scratch.
     *
     * @param devid
     * @param aiid
     * @param allowRetrainReadonlyBot
     * @return
     */
    public ApiResult updateTraining(final UUID devid,
                                    final UUID aiid,
                                    final boolean allowRetrainReadonlyBot) {
        return updateTraining(devid, aiid, null, allowRetrainReadonlyBot);
    }

    /**
     * An update to an existing training session means we will have to delete any existing neural
     * network and start from scratch.
     *
     * @param devid
     * @param aiid
     * @param overridenEngineVersion
     * @param allowRetrainReadonlyBot
     * @return
     */
    public ApiResult updateTraining(final UUID devid,
                                    final UUID aiid,
                                    final String overridenEngineVersion,
                                    final boolean allowRetrainReadonlyBot) {
        final String devidString = devid.toString();
        LogMap logMap = LogMap.map("AIID", aiid);
        try {

            ApiAi ai = Tools.isEmpty(overridenEngineVersion)
                    ? this.databaseAi.getAIWithStatus(devid, aiid, this.jsonSerializer)
                    : this.databaseAi.getAIWithStatusForEngineVersion(devid, aiid, overridenEngineVersion,
                    this.jsonSerializer);
            if (ai == null) {
                this.logger.logUserTraceEvent(LOGFROM, "UpdateTraining - AI not found", devidString, logMap);
                return ApiError.getNotFound();
            }
            String engineVersion = Tools.isEmpty(overridenEngineVersion)
                    ? ai.getEngineVersion() : overridenEngineVersion;
            logMap.add("EngineVersion", engineVersion);
            if (!allowRetrainReadonlyBot && ai.isReadOnly()) {
                this.logger.logUserTraceEvent(LOGFROM, "UpdateTraining - Bot is RO", devidString, logMap);
                return ApiError.getBadRequest(AILogic.BOT_RO_MESSAGE);
            }
            Locale locale = ai.getLanguage();
            Optional<SupportedLanguage> availableLanguage = languageLogic.getAvailableLanguage(locale, devid, aiid);
            if (!availableLanguage.isPresent()) {
                return ApiError.getBadRequest(String.format("Language not available %s", locale));
            }
            switch (ai.getSummaryAiStatus()) {
                case AI_TRAINING:           // fallthrough
                case AI_READY_TO_TRAIN:     // fallthrough
                case AI_TRAINING_STOPPED:   // fallthrough
                case AI_TRAINING_COMPLETE:  // fallthrough
                case AI_TRAINING_QUEUED:    // fallthrough
                case AI_ERROR:              // fallthrough
                case AI_UNDEFINED:
                    try {
                        String trainingMaterials = this.aiServices.getTrainingMaterialsCommon(devid, aiid,
                                this.jsonSerializer);
                        this.aiServices.uploadTraining(ai.getBackendStatus(),
                                new AiIdentity(devid, aiid, availableLanguage.get(), engineVersion),
                                trainingMaterials);
                        // Delete all memory variables for this AI
                        this.memoryIntentHandler.resetIntentsStateForAi(devid, aiid);
                        this.logger.logUserTraceEvent(LOGFROM, "UpdateTraining", devidString, logMap);
                        return new ApiResult().setSuccessStatus("Training updated.");
                    } catch (AIServices.AiServicesException ex) {
                        this.logger.logUserExceptionEvent(LOGFROM, ex.getMessage(), devidString, ex, logMap);
                        return ApiError.getInternalServerError("Could not update training.");
                    }

                default:
                    this.logger.logUserTraceEvent(LOGFROM, "UpdateTraining - could not update training",
                            devidString, logMap);
                    return ApiError.getBadRequest("Invalid training status");
            }
        } catch (Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "UpdateTraining", devidString, e, logMap);
            return ApiError.getInternalServerError();
        }
    }

    /**
     * Gets the training materials for an AI.
     *
     * @param devid
     * @param aiid
     * @return
     */
    public ApiResult getTrainingMaterials(final UUID devid, final UUID aiid) {
        final String devidString = devid.toString();
        try {
            LogMap logMap = LogMap.map("AIID", aiid);
            String trainingMaterials = this.aiServices.getTrainingMaterialsCommon(devid, aiid, this.jsonSerializer);
            if (trainingMaterials == null) {
                this.logger.logUserTraceEvent(LOGFROM, "GetTrainingMaterials - AI not found", devidString, logMap);
                return ApiError.getNotFound();
            }
            ApiTrainingMaterials result = new ApiTrainingMaterials(trainingMaterials);
            this.logger.logUserTraceEvent(LOGFROM, "GetTrainingMaterials", devidString, logMap);
            return result.setSuccessStatus();
        } catch (DatabaseException dbe) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetTrainingMaterials", devidString, dbe);
            return ApiError.getInternalServerError();
        }
    }

    /**
     * Adds context to conversational exchanges
     *
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

            boolean isTopicMarker = isTopicMarker(currentSentence);

            // empty line means a new conversation exchange
            if (currentSentence.isEmpty() || isTopicMarker) {
                if (!lastLineEmpty || isTopicMarker(currentSentence)) {
                    // If the last question didn't have an answer then
                    // ignore the last question
                    if (!humanTalkingNow) {
                        removeLastConversationEntry(validConversation);
                        result.addEvent(UPLOAD_MISSING_RESPONSE, lastHumanSentence);
                    }

                    if (isTopicMarker) {
                        validConversation.add(currentSentence);
                    } else {
                        // New conversation
                        validConversation.add(EMPTY_STRING);
                    }
                }
                humanTalkingNow = true;
                lastLineEmpty = currentSentence.isEmpty();
                continue;
            }

            validConversation.add(currentSentence);

            if (!isTopicMarker) {
                // if the AI is talking then store the response
                lastHumanSentence = currentSentence;

                humanTalkingNow = !humanTalkingNow;
            }

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

    public ApiResult getTrainingFile(final UUID devId, final UUID aiid) {
        LogMap logMap = LogMap.map("AIID", aiid);
        try {

            ApiAi ai = this.databaseAi.getAI(devId, aiid, this.jsonSerializer);
            if (ai == null) {
                this.logger.logUserInfoEvent(LOGFROM, "Training file request for unknown aiid",
                        devId.toString(), logMap);
                return ApiError.getNotFound();
            }
            return new ApiTrainingMaterials(this.databaseAi.getAiTrainingFile(aiid)).setSuccessStatus();
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetTrainingFile", devId.toString(), ex, logMap);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getAiTrainingStatus(final UUID devId, final UUID aiid, final String engineVersion) {
        LogMap logMap = LogMap.map("AIID", aiid);
        try {
            ApiAi ai = this.databaseAi.getAIWithStatusForEngineVersion(devId, aiid, engineVersion, this.jsonSerializer);
            if (ai == null) {
                this.logger.logUserInfoEvent(LOGFROM, "Ai status request for unknown aiid",
                        devId.toString(), logMap);
                return ApiError.getNotFound();
            }
            logMap.add("EngineVersion", ai.getEngineVersion());
            TrainingStatus trainingStatus = ai.getSummaryAiStatus();
            ApiResult result = new ApiResult();
            result.setSuccessStatus(trainingStatus.value());
            return result;
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetTrainingFile", devId.toString(), ex, logMap);
            return ApiError.getInternalServerError();
        }
    }

    private boolean isTopicMarker(final String line) {
        return line.startsWith(TrainingLogic.TOPIC_MARKER);
    }

    /**
     * Download from a web-resource, sanitize each line and rejoin string.
     *
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
            sb.append(this.validate.filterControlAndCoalesceSpaces(line)).append(EOL);
        }
        return sb.toString();
    }

    /**
     * Remove the last conversation item.
     *
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
     *
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

        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(uploadedInputStream, StandardCharsets.UTF_8))) {
            String line;
            int lineSize;
            while ((line = reader.readLine()) != null) {
                lineSize = line.length() + 2;
                // if the line doesn't push us over the upload limit
                if ((fileSize + lineSize) < maxUploadSize) {
                    source.add(this.validate.filterControlAndCoalesceSpaces(line));
                    fileSize += lineSize;
                } else {
                    throw new UploadTooLargeException();
                }
            }
        }
        return source;
    }

    private ApiResult uploadTrainingFile(final ApiAi ai,
                                         final UUID devid,
                                         final UUID aiid,
                                         final String trainingMaterials,
                                         final TrainingFileParsingResult result) {
        final String devidString = devid.toString();
        LogMap logMap = LogMap.map("AIID", aiid).put("EngineVersion", ai.getEngineVersion());
        Locale locale = ai.getLanguage();
        Optional<SupportedLanguage> availableLanguage = languageLogic.getAvailableLanguage(locale, devid, aiid);
        if (!availableLanguage.isPresent()) {
            return ApiError.getBadRequest(String.format("Language not available %s", locale));
        }
        try {

            this.aiServices.uploadTraining(ai.getBackendStatus(),
                    new AiIdentity(devid, aiid, availableLanguage.get(), ai.getEngineVersion()),
                    trainingMaterials);
        } catch (AIServices.AiServicesException ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "UploadTrainingFile", devidString, ex, logMap);
            return ApiError.getInternalServerError("Could not upload training data");
        }

        this.logger.logUserTraceEvent(LOGFROM, "UploadFile", devidString, logMap);
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
         *
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

}
