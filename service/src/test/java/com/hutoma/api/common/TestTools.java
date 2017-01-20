package com.hutoma.api.common;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Unit Tests for Tools.
 */
public class TestTools {

    private static final String SAMPLE_STREAM_CONTENT = "content";
    private static final ByteArrayInputStream SAMPLE_STREAM = new ByteArrayInputStream(SAMPLE_STREAM_CONTENT.getBytes());
    private static final long SAMPLE_STREAM_LENGHT = SAMPLE_STREAM_CONTENT.getBytes().length;

    private Tools tools;

    @Before
    public void setup() {
        this.tools = new Tools();
    }

    @Test
    public void testIsStreamSmallerThan() throws IOException {
        // Check when stream is < max_size
        Assert.assertTrue(this.tools.isStreamSmallerThan(SAMPLE_STREAM, SAMPLE_STREAM_LENGHT + 1));
        // Check stream is reset after
        Assert.assertEquals(SAMPLE_STREAM_CONTENT.getBytes().length, SAMPLE_STREAM.available());
        // Check when stream is > max_size
        Assert.assertFalse(this.tools.isStreamSmallerThan(SAMPLE_STREAM, SAMPLE_STREAM_LENGHT - 1));
        // Check stream is reset after
        Assert.assertEquals(SAMPLE_STREAM_CONTENT.getBytes().length, SAMPLE_STREAM.available());
        // Check when stream is = max_size (since it's not smaller)
        Assert.assertFalse(this.tools.isStreamSmallerThan(SAMPLE_STREAM, SAMPLE_STREAM_LENGHT));
        // Check stream is reset after
        Assert.assertEquals(SAMPLE_STREAM_CONTENT.getBytes().length, SAMPLE_STREAM.available());
    }

    @Test
    public void testCreateNewRandomUuid() {
        UUID uuid1 = this.tools.createNewRandomUUID();
        Assert.assertNotNull(uuid1);
        UUID uuid2 = this.tools.createNewRandomUUID();
        Assert.assertNotEquals(uuid2, uuid1);
    }
}
