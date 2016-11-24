package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.Tools;
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
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;

import static com.hutoma.api.common.ResultEvent.UPLOAD_MISSING_RESPONSE;
import static com.hutoma.api.common.ResultEvent.UPLOAD_NO_CONTENT;
import static com.hutoma.api.connectors.Database.DatabaseException;

/**
 * Created by mauriziocibelli on 28/04/16.
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
    private final Tools tools;
    private final ILogger logger;
    private final Validate validate;
    private final IMemoryIntentHandler memoryIntentHandler;

    @Inject
    public TrainingLogic(Config config, AIServices aiServices, HTMLExtractor htmlExtractor,
                         DatabaseEntitiesIntents database, Tools tools, ILogger logger, Validate validate,
                         IMemoryIntentHandler memoryIntentHandler) {
        this.config = config;
        this.aiServices = aiServices;
        this.htmlExtractor = htmlExtractor;
        this.database = database;
        this.tools = tools;
        this.logger = logger;
        this.validate = validate;
        this.memoryIntentHandler = memoryIntentHandler;
    }

    public ApiResult uploadFile(SecurityContext securityContext, String devid, UUID aiid, int type, String url,
                                InputStream uploadedInputStream, FormDataContentDisposition fileDetail) {

        ArrayList<String> source;

        long maxUploadFileSize = this.config.getMaxUploadSize();

        try {
            switch (type) {

                // 0 = training file is text chat
                case 0:
                    this.logger.logDebug(LOGFROM, "training from uploaded training file");
                    if (null == fileDetail) {
                        return ApiError.getBadRequest("upload could not be processed");
                    }
                    checkMaxUploadFileSize(fileDetail, maxUploadFileSize);
                    source = getFile(maxUploadFileSize, uploadedInputStream);
                    TrainingFileParsingResult result = parseTrainingFile(source);
                    // Bail out if there are fatal events during the parsing
                    if (result.hasFatalEvents()) {
                        return ApiError.getBadRequest("upload parsing errors", result.getEvents());
                    }
                    if (!this.database.updateAiTrainingFile(aiid, result.getTrainingText())) {
                        return ApiError.getNotFound("ai not found");
                    }

                    String trainingMaterials = this.getTrainingMaterialsCommon(devid, aiid);
                    if (trainingMaterials == null) {
                        this.logger.logError(LOGFROM, "training materials are null after having passed validation!");
                        return ApiError.getInternalServerError();
                    }

                    try {
                        this.aiServices.uploadTraining(devid, aiid, trainingMaterials);
                    } catch (AIServices.AiServicesException ex) {
                        this.logger.logError(LOGFROM, "error uploading training: " + ex.getMessage());
                        return ApiError.getInternalServerError("could not upload training data to AI servers");
                    }

                    return new ApiResult().setSuccessStatus("upload accepted",
                            result.getEventCount() == 0 ? null : result.getEvents());

                // 1 = training file is a document
                case 1:
                    this.logger.logDebug(LOGFROM, "training from uploaded document");
                    if (null == fileDetail) {
                        return ApiError.getBadRequest("upload could not be processed");
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
                case 2:
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
        } catch (DatabaseException dde) {
            this.logger.logError(LOGFROM, "database error " + dde.getCause().toString());
            return ApiError.getInternalServerError();
        } catch (Exception ex) {
            this.logger.logError(LOGFROM, "exception " + ex.toString());
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
     * @param securityContext
     * @param devid
     * @param aiid
     * @return
     */
    public ApiResult startTraining(SecurityContext securityContext, String devid, UUID aiid) {

        this.logger.logDebug(LOGFROM, "on demand training start");
        ApiAi ai = null;
        try {
            ai = this.database.getAI(devid, aiid);
        } catch (DatabaseException ex) {
            this.logger.logError(LOGFROM, "could not get AI: " + ex.getMessage());
            return ApiError.getBadRequest("Invalid AI.");
        }
        if (ai == null) {
            return ApiError.getNotFound("Unknown AI");
        }
        TrainingStatus trainingStatus = ai.getAiStatus();
        if (trainingStatus == TrainingStatus.NOT_STARTED || trainingStatus == TrainingStatus.STOPPED) {
            try {
                this.aiServices.startTraining(devid, aiid);
            } catch (AIServices.AiServicesException ex) {
                this.logger.logError(LOGFROM, "could not start training: " + ex.getMessage());
                return ApiError.getInternalServerError("Could not start training");
            }
            // Delete all memory variables for this AI
            this.memoryIntentHandler.deleteAllIntentsForAi(aiid);
            return new ApiResult().setSuccessStatus("Training session started.");
        } else {

            switch (trainingStatus) {
                case COMPLETED:
                    return ApiError.getBadRequest("Training could not be started because it was already completed.");
                case IN_PROGRESS:
                    return ApiError.getBadRequest("A training session is already running.");
                case QUEUED:
                    return ApiError.getBadRequest("A training session is already queued.");
                case STOPPED_MAX_TIME:
                    return ApiError.getBadRequest("You reached the maximum allocated time to train your AI. "
                            + "Please upgrade your subscription.");
                default:
                    return ApiError.getBadRequest("Malformed training file. Training could not be started.");
            }
        }

    }

    /**
     * Send a stop msg to SQS only if a training session is currently ongoing
     * @param securityContext
     * @param devid
     * @param aiid
     * @return
     */

    public ApiResult stopTraining(SecurityContext securityContext, String devid, UUID aiid) {
        try {
            this.logger.logDebug(LOGFROM, "on demand training stop");
            ApiAi ai = this.database.getAI(devid, aiid);
            if (ai == null) {
                return ApiError.getNotFound("AI not found");
            }
            TrainingStatus trainingStatus = ai.getAiStatus();

            if (trainingStatus == TrainingStatus.IN_PROGRESS) {
                this.aiServices.stopTraining(devid, aiid);
                return new ApiResult().setSuccessStatus("Training session stopped.");
            }
        } catch (Exception ex) {
            this.logger.logError(LOGFROM, "exception (stopTraining):" + ex.toString());
            return ApiError.getInternalServerError("Internal server error. Training could not be stopped.");

        }
        return ApiError.getBadRequest("Impossible to stop the current training session. ");
    }

    /**
     * An update to an existing training session means we will have to delete any existing neural
     * network and start from scratch.
     * @param securityContext
     * @param devid
     * @param aiid
     * @return
     */

    public ApiResult updateTraining(SecurityContext securityContext, String devid, UUID aiid) {
        try {
            ApiAi ai = this.database.getAI(devid, aiid);
            if (ai == null) {
                return ApiError.getNotFound("AI not found");
            }
            switch (ai.getAiStatus()) {
                case IN_PROGRESS:   // fallthrough
                case NOT_STARTED:   // fallthrough
                case STOPPED:       // fallthrough
                case COMPLETED:     // fallthrough
                case DELETED:
                    this.logger.logDebug(LOGFROM, "on demand training update");
                    this.aiServices.updateTraining(devid, aiid);
                    // Delete all memory variables for this AI
                    this.memoryIntentHandler.deleteAllIntentsForAi(aiid);
                    return new ApiResult().setSuccessStatus("Training session updated.");
                default:
                    this.logger.logError(LOGFROM, "it was impossible to update training session for aiid:"
                            + aiid.toString() + " devid:" + devid);
                    return ApiError.getInternalServerError("Impossible to update the current training session.");

            }
        } catch (Exception e) {
            this.logger.logError(LOGFROM, "exception (stopTraining):" + e.toString());
            return ApiError.getInternalServerError("Internal server error. Training could not be updated.");

        }
    }

    /**
     * Gets the training materials for an AI.
     * @param securityContext
     * @param devId
     * @param aiid
     * @return
     */
    public ApiResult getTrainingMaterials(final SecurityContext securityContext, final String devId, final UUID aiid) {
        try {
            String trainingMaterials = this.getTrainingMaterialsCommon(devId, aiid);
            if (trainingMaterials == null) {
                return ApiError.getNotFound("AI not found");
            }
            ApiTrainingMaterials result = new ApiTrainingMaterials(trainingMaterials);
            result.setSuccessStatus();
            return result;
        } catch (DatabaseException dbe) {
            this.logger.logError(LOGFROM, "exception (getTrainingMaterials):" + dbe.toString());
            return ApiError.getInternalServerError("Internal server error. Could not get the training materials.");
        }
    }

    /**
     * Adds context to conversational exchanges
     * @param training list of strings, one for each line of conversation
     * @return single string with processed training
     */
    public TrainingFileParsingResult parseTrainingFile(List<String> training) {

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
        ApiAi ai = this.database.getAI(devId, aiid);
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
    private String getTextFromUrl(String url, long maxUploadFileSize) throws Exception {
        // retrieve the url and extract the text
        String article = this.htmlExtractor.getTextFromUrl(url);

        if (article.length() > maxUploadFileSize) {
            throw new UploadTooLargeException();
        }

        // split into lines
        String[] text = article.split("\n");

        // recombine the lines after they've been sanitised
        StringBuilder sb = new StringBuilder();
        for (String line : text) {
            sb.append(this.validate.textSanitizer(line)).append('\n');
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

    private void checkMaxUploadFileSize(FormDataContentDisposition fileDetail, long maxUploadFileSize)
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
    private ArrayList<String> getFile(long maxUploadSize, InputStream uploadedInputStream)
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

    private static class UploadTooLargeException extends Exception {
    }
}