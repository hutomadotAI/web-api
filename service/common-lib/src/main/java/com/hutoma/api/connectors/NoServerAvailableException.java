package com.hutoma.api.connectors;

import org.apache.logging.log4j.util.Strings;

import java.util.List;

/***
 * When the server list is empty
 * or the server does not support what we are trying to do
 * e.g. chatcapacity=0 and we are trying to chat
 */
public class NoServerAvailableException extends Exception {
    public NoServerAvailableException() {
        super("No server available to process this request");
    }

    public NoServerAvailableException(final String message) {
        super(message);
    }

    public static class ServiceTooBusyException extends NoServerAvailableException {

        private List<String> alreadyTried;

        public ServiceTooBusyException(final List<String> alreadyTried) {
            this.alreadyTried = alreadyTried;
        }

        public List<String> getAlreadyTried() {
            return alreadyTried;
        }
    }
}
