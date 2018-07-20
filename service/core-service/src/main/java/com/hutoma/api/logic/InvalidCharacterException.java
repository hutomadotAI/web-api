package com.hutoma.api.logic;

import java.util.List;

class InvalidCharacterException extends Exception {

    private final List<String> linesWithErrors;

    InvalidCharacterException(final List<String> linesWithErrors) {
        this.linesWithErrors = linesWithErrors;
    }

    List<String> getLinesWithErrors() {
        return this.linesWithErrors;
    }
}
