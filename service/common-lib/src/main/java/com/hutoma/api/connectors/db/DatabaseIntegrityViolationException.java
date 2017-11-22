package com.hutoma.api.connectors.db;

/***
 * Happens when we violate a constraint, most commonly trying to create a duplicate in a unique field
 */
public class DatabaseIntegrityViolationException extends DatabaseException {

    public DatabaseIntegrityViolationException(Throwable cause) {
        super(cause);
    }
}
