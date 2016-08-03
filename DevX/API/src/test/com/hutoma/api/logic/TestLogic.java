package com.hutoma.api.logic;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by David MG on 27/07/2016.
 */
public class TestLogic {

    @Before
    public void before() {
    }

    @Test
    public void testLogic() {
        Assert.assertNotNull(new LogicTest().testOutput("someuser"));
    }
}
