package com.hutoma.api.validation;

import javax.inject.Inject;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Created by David MG on 06/09/2016.
 */
public class Validate {

    static Pattern alphaNumericDashes = Pattern.compile("^[a-zA-Z0-9_-]+$");
    static Pattern alphaNumericAndMoreDesc = Pattern.compile("^[a-zA-Z0-9_\\.\\,\\+\\-\\(\\)\\!\\£\\$\\%\\&\\@\\? ]+$");
    static Pattern alphaNumericAndMoreNoAt = Pattern.compile("^[a-zA-Z0-9_\\.\\,\\+\\-\\(\\)\\!\\£\\$\\%\\&\\? ]+$");
    static Pattern uuidPattern = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    static Pattern floatPattern = Pattern.compile("^[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?$");

    @Inject
    public Validate() {
    }

    /***
     * Validates a parameter against a pattern
     * @param pattern the static pattern to match to
     * @param paramName the name of the param, to use in the exception message
     * @param param the param value
     * @return trimmed parameter
     * @throws ParameterValidationException if the parameter is empty, null or invalid
     */
    private String validatePattern(final Pattern pattern, final String paramName, final String param) throws ParameterValidationException {
        if (null == param) {
            throw new ParameterValidationException("missing " + paramName);
        }
        final String result = param.trim();
        if (result.isEmpty()) {
            throw new ParameterValidationException("empty " + paramName);
        }
        if (!pattern.matcher(result).matches()) {
            throw new ParameterValidationException("invalid characters in " + paramName);
        }
        return result;
    }

    /***
     * Validates an optional parameter against a pattern
     * @param pattern the static pattern to match to
     * @param paramName the name of the param, to use in the exception message
     * @param param the param value
     * @return trimmed parameter, or empty string if it was null or empty
     * @throws ParameterValidationException if the parameter invalid
     */
    private String validatePatternOptionalField(final Pattern pattern, final String paramName, final String param) throws ParameterValidationException {
        if (null == param) {
            return "";
        }
        final String result = param.trim();
        if (result.isEmpty()) {
            return "";
        }
        if (!pattern.matcher(result).matches()) {
            throw new ParameterValidationException("invalid characters in " + paramName);
        }
        return result;
    }

    /***
     * Validates an optional floating point number
     * @param paramName parameter name used for exception
     * @param min valid range lowest value
     * @param max valid range highest value
     * @param fallback return this if the field is empty or missing
     * @param param the parameter value
     * @return valid float representing the input, or fallback
     * @throws ParameterValidationException if the float was invalid or out of range
     */
    Float validateOptionalFloat(final String paramName, final float min, final float max, final float fallback, String param) throws ParameterValidationException {
        // if empty, return the fallback
        if ((null == param) || (param.isEmpty())) {
            return fallback;
        }
        // trim and convert commas to full-stops
        param = param.trim().replace(',', '.');
        // check that it generally matches
        if (!floatPattern.matcher(param).matches()) {
            throw new ParameterValidationException("invalid " + paramName);
        }
        // parse
        final float result = Float.parseFloat(param);
        // just in case it's still weirdly invalid
        if (Float.isNaN(result)) {
            throw new ParameterValidationException("invalid " + paramName);
        }
        // if it's out of range
        if ((result < min) || (result > max)) {
            throw new ParameterValidationException(paramName + " out of range");
        }
        return result;
    }

    UUID validateUuid(final String paramName, final String param) throws ParameterValidationException {
        final String result = validatePattern(uuidPattern, paramName, param);
        try {
            final UUID uuid = UUID.fromString(result);
            return uuid;
        } catch (final IllegalArgumentException iae) {
            throw new ParameterValidationException("invalid characters in " + paramName);
        }
    }

    String validateAlphaNumPlusDashes(final String paramName, final String param) throws ParameterValidationException {
        return validatePattern(alphaNumericDashes, paramName, param);
    }

    String validateRequiredSanitized(final String paramName, final String param) throws ParameterValidationException {
        if (null == param) {
            throw new ParameterValidationException("missing " + paramName);
        }
        final String result = textSanitizer(param);
        if (result.isEmpty()) {
            throw new ParameterValidationException("empty " + paramName);
        }
        return result;
    }

    String validateOptionalSanitized(final String param) throws ParameterValidationException {
        return textSanitizer(param);
    }

    String validateOptionalDescription(final String paramName, final String param) throws ParameterValidationException {
        return validatePatternOptionalField(alphaNumericAndMoreDesc, paramName, param);
    }

    String validateOptionalSanitizeRemoveAt(final String paramName, final String param) throws ParameterValidationException {
        return validatePatternOptionalField(alphaNumericAndMoreNoAt, paramName, param);
    }

    /**
     * Returns the same string with anything over char 127 or below char 32 removed
     * Also, []<>& are removed altogther
     * Whitespaces are deduped and the string is trimmed of leading and trailing whitespaces.
     * @param input abc[]<>&  abc
     * @return abc abc
     */
    public String textSanitizer(final String input) {
        // null check, fast bail
        if (null == input) {
            return "";
        }

        final StringBuilder sb = new StringBuilder();
        final int n = input.length();
        boolean lastCharWasSpace = true;
        char c;
        for (int i = 0; i < n; i++) {
            c = input.charAt(i);
            // all whitespaces
            if (Character.isWhitespace(c)) {
                if (!lastCharWasSpace) {
                    sb.append(' ');
                    lastCharWasSpace = true;
                }
            } else {
                // ignore out of range characters
                if ((c >= 32) && (c < 127)) {
                    switch (c) {
                        // characters to omit
                        case '[':
                        case ']':
                        case '<':
                        case '>':
                        case '&':
                            break;
                        // characters to retain unchanged
                        default:
                            sb.append(c);
                            lastCharWasSpace = false;
                    }
                }
            }
        }
        // removed trailing space if present
        if ((lastCharWasSpace) && (sb.length() > 0)) {
            sb.setLength((sb.length() - 1));
        }
        return sb.toString();
    }

    public static class ParameterValidationException extends Exception {
        public ParameterValidationException(final String message) {
            super(message);
        }
    }

}
