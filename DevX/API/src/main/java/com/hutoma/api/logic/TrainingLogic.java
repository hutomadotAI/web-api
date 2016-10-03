package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.Logger;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.HTMLExtractor;
import com.hutoma.api.connectors.MessageQueue;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.validation.Validate;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.hutoma.api.common.ResultEvent.UPLOAD_MISSING_RESPONSE;
import static com.hutoma.api.common.ResultEvent.UPLOAD_NO_CONTENT;

/**
 * Created by mauriziocibelli on 28/04/16.
 */
public class TrainingLogic {

    Config config;
    MessageQueue messageQueue;
    HTMLExtractor htmlExtractor;
    Database database;
    Tools tools;
    Logger logger;
    Validate validate;

    private final String LOGFROM = "traininglogic";

    private static final String EMPTY_STRING= "";
    private static final String PREVIOUS_AI_PREFIX = "[";
    private static final String PREVIOUS_AI_SUFFIX = "] ";
    private static final String EOL = "\n";

    public class UploadTooLargeException extends Exception {
    }


    @Inject
    public TrainingLogic(Config config, MessageQueue messageQueue, HTMLExtractor htmlExtractor,
                         Database database, Tools tools, Logger logger, Validate validate) {
        this.config = config;
        this.messageQueue = messageQueue;
        this.htmlExtractor = htmlExtractor;
        this.database = database;
        this.tools = tools;
        this.logger = logger;
        this.validate = validate;
    }

    public ApiResult uploadFile(SecurityContext securityContext, String devid, UUID aiid, int type, String url, InputStream uploadedInputStream, FormDataContentDisposition fileDetail) {

        ArrayList<String> source;

        long maxUploadFileSize = config.getMaxUploadSize();

        try {
            switch (type) {

                // 0 = training file is text chat
                case 0:
                    logger.logDebug(LOGFROM, "training from uploaded training file");
                    if (null==fileDetail) {
                        return ApiError.getBadRequest("upload could not be processed");
                    }
                    checkMaxUploadFileSize(fileDetail, maxUploadFileSize);
                    source = getFile(maxUploadFileSize, uploadedInputStream);
                    TrainingFileParsingResult result = parseTrainingFile(source);
                    // Bail out if there are fatal events during the parsing
                    if (result.hasFatalEvents()) {
                        return ApiError.getBadRequest("upload parsing errors", result.getEvents());
                    }
                    if (!database.updateAiTrainingFile(aiid, result.getTrainingText())) {
                        return ApiError.getNotFound("ai not found");
                    }
                    messageQueue.pushMessageReadyForTraining(devid, aiid);
                    if (source.size() > config.getMaxClusterLines()) {
                        messageQueue.pushMessageClusterSplit(devid, aiid, config.getClusterMinProbability());
                    }
                    return new ApiResult().setSuccessStatus("upload accepted",
                            result.getEventCount() == 0 ? null : result.getEvents());

                // 1 = training file is a document
                case 1:
                    logger.logDebug(LOGFROM, "training from uploaded document");
                    if (null==fileDetail) {
                        return ApiError.getBadRequest("upload could not be processed");
                    }
                    checkMaxUploadFileSize(fileDetail, maxUploadFileSize);
                    source = getFile(maxUploadFileSize, uploadedInputStream);
                    if (!database.updateAiTrainingFile(aiid, String.join(EOL, source))) {
                        return ApiError.getNotFound("ai not found");
                    }
                    messageQueue.pushMessagePreprocessTrainingText(devid, aiid);
                    return new ApiResult().setSuccessStatus("upload document accepted");

                // 2 = training file is a webpage
                case 2:
                    logger.logDebug(LOGFROM, "training from uploaded URL");
                    if (!database.updateAiTrainingFile(aiid, getTextFromUrl(url, maxUploadFileSize))) {
                        return ApiError.getNotFound("ai not found");
                    }
                    messageQueue.pushMessagePreprocessTrainingText(devid, aiid);
                    return new ApiResult().setSuccessStatus("url training accepted");

                default:
                    return ApiError.getBadRequest("incorrect training type");
            }
        }
        catch (IOException ioe) {
            logger.logInfo(LOGFROM, "html extraction error " + ioe.toString());
            return ApiError.getBadRequest();
        }
        catch (HTMLExtractor.HtmlExtractionException ht) {
            logger.logInfo(LOGFROM, "html extraction error " + ht.getCause().toString());
            return ApiError.getBadRequest("html extraction error");
        }
        catch (UploadTooLargeException tooLarge) {
            logger.logInfo(LOGFROM, "upload attempt was larger than maximum allowed");
            return ApiError.getPayloadTooLarge();
        }
        catch (Database.DatabaseException dde) {
            logger.logError(LOGFROM, "database error " + dde.getCause().toString());
            return ApiError.getInternalServerError();
        }
        catch (Exception ex) {
            logger.logError(LOGFROM, "exception " + ex.toString());
            return ApiError.getInternalServerError();
        }
        finally {
            try {
                uploadedInputStream.close();
            } catch (Throwable ignore) {}
        }
    }

    void checkMaxUploadFileSize(FormDataContentDisposition fileDetail, long maxUploadFileSize) throws UploadTooLargeException {
        if (null!=fileDetail) {
            if (fileDetail.getSize()>maxUploadFileSize) {
                throw new UploadTooLargeException();
            }
        }
    }

    public ApiResult delete(SecurityContext securityContext, String devid, UUID aiid) {
        try {
            logger.logDebug(LOGFROM, "request to delete training for " + aiid);
            messageQueue.pushMessageDeleteTraining(devid, aiid);
        } catch (MessageQueue.MessageQueueException e) {
            logger.logError(LOGFROM, "message queue exception " + e.toString());
            return ApiError.getInternalServerError();
        }
        return new ApiResult().setSuccessStatus("successfully queued for deletion");
    }

    /**
     * Download from a web-resource, sanitize each line and rejoin string.
     * @param url web-resource
     * @return clean result
     * @throws Exception
     */
    private String getTextFromUrl(String url, long maxUploadFileSize) throws Exception {
        // retrieve the url and extract the text
        String article = htmlExtractor.getTextFromUrl(url);

        if (article.length()>maxUploadFileSize) {
            throw new UploadTooLargeException();
        }

        // split into lines
        String[] text = article.split("\n");

        // recombine the lines after they've been sanitised
        StringBuilder sb = new StringBuilder();
        for(String line: text) {
            sb.append(validate.textSanitizer(line)).append('\n');
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

    /**
     * Adds context to conversational exchanges
     * @param training list of strings, one for each line of conversation
     * @return single string with processed training
     */
    TrainingFileParsingResult parseTrainingFile(List<String> training) {

        TrainingFileParsingResult result = new TrainingFileParsingResult();
        String lastAISentence = EMPTY_STRING;
        String lastHumanSentence = EMPTY_STRING;
        List<String> validConversation = new ArrayList<>();
        boolean humanTalkingNow = true;
        boolean lastLineEmpty = true;

        for (String currentSentence:training) {

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

    /**
     * Reads from InputStream and returns a list of sanitised strings
     * @param maxUploadSize
     * @param uploadedInputStream
     * @return list of strings
     * @throws UploadTooLargeException
     * @throws IOException
     */
    ArrayList<String> getFile(long maxUploadSize, InputStream uploadedInputStream) throws UploadTooLargeException, IOException {

        ArrayList<String> source = new ArrayList<>();
        long fileSize = 0;

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(uploadedInputStream));
            String line;
            int lineSize;
            while ((line = reader.readLine()) != null) {
                lineSize = line.length() + 2;
                // if the line doesn't push us over the upload limit
                if ((fileSize + lineSize) < maxUploadSize) {
                    source.add(validate.textSanitizer(line));
                    fileSize += lineSize;
                } else {
                    throw new UploadTooLargeException();
                }
            }
        }
        finally {
            if (null!=reader) {
                reader.close();
            }
        }
        return source;
    }
}