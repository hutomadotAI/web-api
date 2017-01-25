package com.hutoma.api.common;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

/**
 * Unit Tests for Tools.
 */
public class TestTools {
    
    private Tools tools;

    @Before
    public void setup() {
        this.tools = new Tools();
    }

    @Test
    public void testCreateNewRandomUuid() {
        UUID uuid1 = this.tools.createNewRandomUUID();
        Assert.assertNotNull(uuid1);
        UUID uuid2 = this.tools.createNewRandomUUID();
        Assert.assertNotEquals(uuid2, uuid1);
    }
}
