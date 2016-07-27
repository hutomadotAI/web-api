package com.hutoma.api.logic;

import com.google.inject.Inject;

/**
 * Created by David MG on 27/07/2016.
 */
public class LogicTest {

    @Inject
    public LogicTest() {
    }

    public String testOutput(String userName) {
        return "Now testing " + userName;
    }
}
