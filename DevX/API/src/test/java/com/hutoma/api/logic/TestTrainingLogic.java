package com.hutoma.api.logic;

import com.hutoma.api.common.*;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.HTMLExtractor;
import com.hutoma.api.connectors.MessageQueue;
import hutoma.api.server.ai.api_root;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.SecurityContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by David MG on 12/08/2016.
 */
public class TestTrainingLogic {

    Config fakeConfig;
    FakeJsonSerializer fakeSerializer;
    MessageQueue fakeMessageQueue;
    DatabaseProxy fakeDatabase;
    Tools fakeTools;
    Logger fakeLogger;
    SecurityContext fakeContext;
    HTMLExtractor fakeExtractor;
    FormDataContentDisposition fakeContentDisposition;
    TrainingLogic logic;

    private String DEVID = "devid";
    private String AIID = "aiid";
    private String VALIDKEY = "RW1wdHlUZXN0S2V5";
    private String UURL = "url://";
    private String SOMETEXT = "some text";
    private String TEXTMULTILINE = "line\nline\nline\nline\nline\nline\nline\nline\nline\nline\nline\nline\n";

    /**
     * Roundabout way to get around DatabaseException being an inner class
     */
    public class DatabaseProxy extends Database {

        public DatabaseProxy(Logger logger) {
            super(logger);
        }

        public DatabaseException createDBEx() {
            return new Database.DatabaseException(new Exception("test"));
        }
    }

    @Before
    public void setup() {

        this.fakeSerializer = new FakeJsonSerializer();
        this.fakeConfig = mock(Config.class);
        when(fakeConfig.getEncodingKey()).thenReturn(VALIDKEY);
        this.fakeDatabase = mock(DatabaseProxy.class);
        when(fakeDatabase.createDBEx()).thenCallRealMethod();
        this.fakeContext = mock(SecurityContext.class);
        this.fakeMessageQueue = mock(MessageQueue.class);
        this.fakeTools = mock(Tools.class);
        this.fakeLogger = mock(Logger.class);
        when(fakeTools.createNewRandomUUID()).thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        when(fakeTools.textSanitizer(anyString())).thenCallRealMethod();
        this.fakeExtractor = mock(HTMLExtractor.class);
        this.fakeContentDisposition = mock(FormDataContentDisposition.class);
        logic = new TrainingLogic(fakeConfig, fakeSerializer, fakeMessageQueue, fakeExtractor, fakeDatabase, fakeTools, fakeLogger);

        when(fakeConfig.getMaxUploadSize()).thenReturn(65536L);
        when(fakeConfig.getMaxClusterLines()).thenReturn(65536);
    }

    InputStream createUpload(String content) {
        InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        try {
            when(fakeContentDisposition.getSize()).thenReturn((long)stream.available());
        } catch (IOException e) {
            // ignore!
        }
        return stream;
    }

    @Test
    public void testTrain_TextSimple() {
        InputStream stream = createUpload(SOMETEXT);
        logic.uploadFile(fakeContext, DEVID, AIID, 0, UURL, stream, fakeContentDisposition);
        api_root._myAIs apiRoot = ((api_root._myAIs)fakeSerializer.getUnserialized());
        Assert.assertEquals(200, apiRoot.status.code);
    }

    @Test
    public void testTrain_DocSimple() {
        InputStream stream = createUpload(SOMETEXT);
        logic.uploadFile(fakeContext, DEVID, AIID, 1, UURL, stream, fakeContentDisposition);
        api_root._myAIs apiRoot = ((api_root._myAIs)fakeSerializer.getUnserialized());
        Assert.assertEquals(200, apiRoot.status.code);
    }

    @Test
    public void testTrain_UrlSimple() throws HTMLExtractor.HtmlExtractionException {
        when(fakeExtractor.getTextFromUrl(anyString())).thenReturn(SOMETEXT);
        logic.uploadFile(fakeContext, DEVID, AIID, 2, UURL, null, null);
        api_root._myAIs apiRoot = ((api_root._myAIs)fakeSerializer.getUnserialized());
        Assert.assertEquals(200, apiRoot.status.code);
    }

    @Test
    public void testTrain_Text_UploadTooLarge() {
        InputStream stream = createUpload(TEXTMULTILINE);
        when(fakeConfig.getMaxUploadSize()).thenReturn((long)TEXTMULTILINE.length()-1);
        logic.uploadFile(fakeContext, DEVID, AIID, 0, UURL, stream, fakeContentDisposition);
        api_root._myAIs apiRoot = ((api_root._myAIs)fakeSerializer.getUnserialized());
        Assert.assertEquals(413, apiRoot.status.code);
    }

    @Test
    public void testTrain_Doc_UploadTooLarge() {
        InputStream stream = createUpload(TEXTMULTILINE);
        when(fakeConfig.getMaxUploadSize()).thenReturn((long)TEXTMULTILINE.length()-1);
        logic.uploadFile(fakeContext, DEVID, AIID, 1, UURL, stream, fakeContentDisposition);
        api_root._myAIs apiRoot = ((api_root._myAIs)fakeSerializer.getUnserialized());
        Assert.assertEquals(413, apiRoot.status.code);
    }

    @Test
    public void testTrain_Url_ExtractTooLarge() throws HTMLExtractor.HtmlExtractionException {
        when(fakeExtractor.getTextFromUrl(anyString())).thenReturn(TEXTMULTILINE);
        when(fakeConfig.getMaxUploadSize()).thenReturn((long)TEXTMULTILINE.length()-1);
        logic.uploadFile(fakeContext, DEVID, AIID, 2, UURL, null, null);
        api_root._myAIs apiRoot = ((api_root._myAIs)fakeSerializer.getUnserialized());
        Assert.assertEquals(413, apiRoot.status.code);
    }

    @Test
    public void testTrain_TextDBFail() throws Database.DatabaseException, HTMLExtractor.HtmlExtractionException {
        int trainingType = 0;
        makeDBFail(trainingType);
    }

    @Test
    public void testTrain_DocDBFail() throws Database.DatabaseException, HTMLExtractor.HtmlExtractionException {
        int trainingType = 1;
        makeDBFail(trainingType);
    }

    @Test
    public void testTrain_UrlDBFail() throws Database.DatabaseException, HTMLExtractor.HtmlExtractionException {
        int trainingType = 2;
        makeDBFail(trainingType);
    }

    void makeDBFail(int trainingType) throws Database.DatabaseException, HTMLExtractor.HtmlExtractionException {
        doThrow(fakeDatabase.createDBEx()).when(fakeDatabase).updateAiTrainingFile(anyString(), anyString());
        InputStream stream = createUpload(SOMETEXT);
        when(fakeExtractor.getTextFromUrl(anyString())).thenReturn(SOMETEXT);
        logic.uploadFile(fakeContext, DEVID, AIID, trainingType, UURL, stream, fakeContentDisposition);
        api_root._myAIs apiRoot = ((api_root._myAIs)fakeSerializer.getUnserialized());
        Assert.assertEquals(500, apiRoot.status.code);
    }

    @Test
    public void testTrain_TextMessageFail() throws Exception {
        int trainingType = 0;
        makeMessageQueueFail(trainingType);
    }

    @Test
    public void testTrain_DocMessageFail() throws Exception {
        int trainingType = 1;
        makeMessageQueueFail(trainingType);
    }

    @Test
    public void testTrain_UrlMessageFail() throws Exception {
        int trainingType = 2;
        makeMessageQueueFail(trainingType);
    }

    void makeMessageQueueFail(int trainingType) throws Exception {
        doThrow(new Exception("test")).when(fakeMessageQueue).pushMessageReadyForTraining(anyString(), anyString());
        doThrow(new Exception("test")).when(fakeMessageQueue).pushMessagePreprocessTrainingText(anyString(), anyString());
        InputStream stream = createUpload(SOMETEXT);
        when(fakeExtractor.getTextFromUrl(anyString())).thenReturn(SOMETEXT);
        logic.uploadFile(fakeContext, DEVID, AIID, trainingType, UURL, stream, fakeContentDisposition);
        api_root._myAIs apiRoot = ((api_root._myAIs)fakeSerializer.getUnserialized());
        Assert.assertEquals(500, apiRoot.status.code);
    }

    @Test
    public void testTrain_BadTrainingType() {
        InputStream stream = createUpload(SOMETEXT);
        logic.uploadFile(fakeContext, DEVID, AIID, -1, UURL, stream, fakeContentDisposition);
        api_root._myAIs apiRoot = ((api_root._myAIs)fakeSerializer.getUnserialized());
        Assert.assertEquals(400, apiRoot.status.code);
    }
}