package com.hutoma.api.connectors;

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
}
