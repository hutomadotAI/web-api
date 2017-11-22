package com.hutoma.api.validation;

public class ParameterValidationException extends Exception {
    private final String paramName;

    public ParameterValidationException(final String message, final String paramName) {
        super(message);
        this.paramName = paramName;
    }

    public String getParameterName() {
        return this.paramName;
    }
}