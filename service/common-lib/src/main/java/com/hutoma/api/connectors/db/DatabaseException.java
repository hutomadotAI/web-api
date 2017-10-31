package com.hutoma.api.connectors.db;

/***
 * General exception for database errors
 */
public class DatabaseException extends Exception {

    public DatabaseException(final Throwable cause) {
            super(cause);
        }

    public DatabaseException(final String message) {
            super(message);
        }
}
