package com.hutoma.api.logic;

import com.amazonaws.services.devicefarm.model.Upload;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Logger;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.*;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import com.hutoma.api.auth.Role;
import com.hutoma.api.auth.Secured;
import hutoma.api.server.ai.api_root;
import hutoma.api.server.db.ai;
import hutoma.api.server.utils.utils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static hutoma.api.server.utils.utils.getConfigProp;

/**
 * Created by mauriziocibelli on 28/04/16.
 */
public class TrainingLogic {

    Config config;
    JsonSerializer jsonSerializer;
    MessageQueue messageQueue;
    HTMLExtractor htmlExtractor;
    Database database;
    Tools tools;
    Logger logger;

    private final String LOGFROM = "traininglogic";

    public class UploadTooLargeException extends Exception {
    }

    @Inject
    public TrainingLogic(Config config, JsonSerializer jsonSerializer, MessageQueue messageQueue, HTMLExtractor htmlExtractor, Database database, Tools tools, Logger logger) {
        this.config = config;
        this.jsonSerializer = jsonSerializer;
        this.messageQueue = messageQueue;
        this.htmlExtractor = htmlExtractor;
        this.database = database;
        this.tools = tools;
        this.logger = logger;
    }

    public String uploadFile(SecurityContext securityContext, String devid, String aiid, int type, String url, InputStream uploadedInputStream, FormDataContentDisposition fileDetail) {

        ArrayList<String> source;

        api_root._myAIs myai = new api_root._myAIs();
        api_root._status st = new api_root._status();
        st.code = 200;
        st.info ="success";
        myai.status =st;
        api_root._ai _ai = new api_root._ai();

        long maxUploadFileSize = config.getMaxUploadSize();

        try {

            switch (type) {

                // 0 = training file is text chat
                case 0:
                    logger.logDebug(LOGFROM, "training from uploaded training file");
                    checkMaxUploadFileSize(fileDetail, maxUploadFileSize);
                    source = getFile(maxUploadFileSize, uploadedInputStream);
                    database.updateAiTrainingFile(aiid, parseTrainingFile(source));
                    messageQueue.pushMessageReadyForTraining(devid, aiid);
                    if (source.size() > config.getMaxClusterLines()) {
                        messageQueue.pushMessageClusterSplit(devid, aiid, config.getClusterMinProbability());
                    }
                    break;

                // 1 = training file is a document
                case 1:
                    logger.logDebug(LOGFROM, "training from uploaded document");
                    checkMaxUploadFileSize(fileDetail, maxUploadFileSize);
                    source = getFile(maxUploadFileSize, uploadedInputStream);
                    database.updateAiTrainingFile(aiid, String.join("\n", source));
                    messageQueue.pushMessagePreprocessTrainingText(devid, aiid);
                    break;

                // 2 = training file is a webpage
                case 2:
                    logger.logDebug(LOGFROM, "training from uploaded URL");
                    database.updateAiTrainingFile(aiid, getTextFromUrl(url, maxUploadFileSize));
                    messageQueue.pushMessagePreprocessTrainingText(devid, aiid);
                    break;

                default:
                    logger.logInfo(LOGFROM, "bad request type");
                    st.code = 400;
                    st.info = "Bad request type";
                    break;
            }
        }
        catch (IOException ioe) {
            logger.logInfo(LOGFROM, "html extraction error " + ioe.toString());
            st.code = 400;
            st.info = "Bad request";
        }
        catch (HTMLExtractor.HtmlExtractionException ht) {
            logger.logInfo(LOGFROM, "html extraction error " + ht.getCause().toString());
            st.code = 400;
            st.info = "HTML extraction error";
        }
        catch (UploadTooLargeException tooLarge) {
            logger.logInfo(LOGFROM, "upload attempt was larger than maximum allowed");
            st.code = 413;
            st.info = "Payload Too Large";
        }
        catch (Database.DatabaseException dde) {
            logger.logError(LOGFROM, "database error " + dde.getCause().toString());
            st.code = 500;
            st.info = "Internal server error";
        }
        catch (Exception ex) {
            logger.logError(LOGFROM, "exception " + ex.toString());
            st.code = 500;
            st.info = "Internal server error";
        }
        finally {
            try {
                uploadedInputStream.close();
            } catch (Throwable ignore) {}
         }
        return jsonSerializer.serialize(myai);
    }

    void checkMaxUploadFileSize(FormDataContentDisposition fileDetail, long maxUploadFileSize) throws UploadTooLargeException {
        if (null!=fileDetail) {
            if (fileDetail.getSize()>maxUploadFileSize) {
                throw new UploadTooLargeException();
            }
        }
    }

    public String delete(SecurityContext securityContext, String devid, String aiid) {
        api_root._myAIs myai = new api_root._myAIs();
        api_root._status st = new api_root._status();
        st.code = 200;
        st.info ="success";
        myai.status = st;

        //api_root._ai _ai = new api_root._ai();
        messageQueue.pushMessageDeleteTraining(devid, aiid);
        //_ai.ai_status = String.valueOf(MessageQueue.AwsMessage.training_queued);
        return jsonSerializer.serialize(myai);
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
            sb.append(tools.textSanitizer(line)).append('\n');
        }
        return sb.toString();
    }

    /**
     * Adds context to conversational exchanges
     * @param training list of strings, one for each line of conversation
     * @return single string with processed training
     */
    String parseTrainingFile(List<String> training) {

        StringBuilder parsedFile = new StringBuilder();
        String lastAISentence = "";

        boolean humanTalkingNow = true;
        boolean lastLineEmpty = true;
        for (String currentSentence:training) {

            // empty line means a new conversation exchange
            if (currentSentence.isEmpty()) {
                // the human starts
                humanTalkingNow = true;
                // no previous AI response
                lastAISentence = "";
                // only one empty line at a time
                if (!lastLineEmpty) {
                    parsedFile.append('\n');
                    lastLineEmpty = true;
                }
            } else {
                // if it's the human's turn and there was a previous AI response
                // then we prepend it in square brackets
                if (humanTalkingNow && (!lastAISentence.isEmpty())) {
                    parsedFile.append('[').append(lastAISentence).append("] ");
                }
                // and we list the sentence
                parsedFile.append(currentSentence).append('\n');
                // if the AI is talking then store the reponse
                if (!humanTalkingNow) {
                    lastAISentence = currentSentence;
                }
                // switch turns from human to AI
                humanTalkingNow = !humanTalkingNow;
                lastLineEmpty = false;
            }
        }
        // add an empty line if there wasn't one already
        if (!lastLineEmpty) {
            parsedFile.append('\n');
        }
        return parsedFile.toString();
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
                    source.add(tools.textSanitizer(line));
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