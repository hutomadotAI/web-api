package com.hutoma.api.validation;

import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.sub.AiBot;
import com.hutoma.api.logic.TrainingLogic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;

/**
 * Created by David MG on 06/09/2016.
 */
public class Validate {


    private static final Pattern alphaNumericDashes = Pattern.compile("^[a-zA-Z0-9_-]+$");
    private static final Pattern entityNames = Pattern.compile("^[\\.a-zA-Z0-9_-]+$");
    private static final Pattern printableAscii =
            Pattern.compile("^[\\x20-\\x7E]+$");
    private static final Pattern printableAsciiNoAt =
            Pattern.compile("^[\\x20-\\x3f\\x41-\\x7E]+$");
    private static final Pattern uuidPattern =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    private static final Pattern floatPattern = Pattern.compile("^[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?$");
    private static final Pattern alphaNumDashesSomePunctuationAndSpace = Pattern.compile("^[a-zA-Z0-9_\\.\\,\\- ]+$");
    private static final Pattern aiName = Pattern.compile("^[a-zA-Z0-9\\-_\\s]+$");

    @Inject
    public Validate() {
    }

    public static boolean isAnyNullOrEmpty(final String... params) {
        return Arrays.stream(params).anyMatch(s -> s == null || s.isEmpty());
    }

    public static Locale validateLocale(final String paramName, final String param) throws ParameterValidationException {
        if (param == null || param.isEmpty()) {
            throw new ParameterValidationException("parameter null or empty", paramName);
        }
        if (Arrays.stream(Locale.getAvailableLocales()).noneMatch(x -> x.toLanguageTag().equals(param))) {
            throw new ParameterValidationException("invalid locale: " + param, paramName);
        }
        // At this moment we know the locale is correctly formatted
        return Locale.forLanguageTag(param);
    }

    static AiBot.PublishingType validatePublishingType(final String param)
            throws ParameterValidationException {
        if (param == null || param.isEmpty()) {
            throw new ParameterValidationException("parameter null or empty", ParameterFilter.PUBLISHING_TYPE);
        }
        try {
            return AiBot.PublishingType.from(Integer.parseInt(param));
        } catch (IllegalArgumentException ex) {
            throw new ParameterValidationException("invalid publishing type: " + param,
                    ParameterFilter.PUBLISHING_TYPE);
        }
    }

    /**
     * Returns the same string with anything over char 127 or below char 32 removed
     * Also, []&lt;&gt;&amp; are removed altogether
     * Whitespaces are deduped and the string is trimmed of leading and trailing whitespaces.
     * @param input abc[]&lt;&gt;&amp;  abc
     * @return abc abc
     */
    public String filterControlAndCoalesceSpaces(final String input) {
        // null check, fast bail
        if (null == input) {
            return "";
        }

        final StringBuilder sb = new StringBuilder();
        final int n = input.length();
        boolean lastCharWasSpace = true;
        char ch;
        for (int i = 0; i < n; i++) {
            ch = input.charAt(i);
            // all whitespaces
            if (Character.isWhitespace(ch)) {
                if (!lastCharWasSpace) {
                    sb.append(' ');
                    lastCharWasSpace = true;
                }
            } else {
                // silently omit control characters
                // i.e. 00-1f, 7f-9f
                if (!Character.isISOControl(ch)) {
                    sb.append(ch);
                    lastCharWasSpace = false;
                }
            }
        }
        // removed trailing space if present
        if ((lastCharWasSpace) && (sb.length() > 0)) {
            sb.setLength((sb.length() - 1));
        }
        return sb.toString();
    }

    /***
     * Throw a validation exception if the string is too long
     * @param maxLength
     * @param paramName
     * @param param
     * @return
     * @throws ParameterValidationException
     */
    public String validateFieldLength(final int maxLength, final String paramName, final String param)
            throws ParameterValidationException {
        if ((null != param) && param.length() > maxLength) {
            throw new ParameterValidationException("parameter too long", paramName);
        }
        return param;
    }

    /***
     * * Throw a validation exception if any of the strings are too long
     * @param maxLength
     * @param paramName
     * @param paramList
     * @return
     * @throws ParameterValidationException
     */
    List<String> validateFieldLengthsInList(final int maxLength, final String paramName, List<String> paramList)
            throws ParameterValidationException {
        for (String item : paramList) {
            validateFieldLength(maxLength, paramName, item);
        }
        return paramList;
    }

    /***
     * For each string in the list, filter control characters and coalesce spaces
     * @param paramList list of strings
     * @return new list of strings
     */
    public List<String> filterCoalesceSpacesInList(List<String> paramList) {
        return paramList.stream()
                .map(x -> filterControlAndCoalesceSpaces(x))
                .collect(Collectors.toList());
    }

    /***
     * Validates a parameter against a pattern
     * @param pattern the static pattern to match to
     * @param paramName the name of the param, to use in the exception message
     * @param param the param value
     * @return trimmed parameter
     * @throws ParameterValidationException if the parameter is empty, null or invalid
     */
    private String validatePattern(final Pattern pattern, final String paramName, final String param)
            throws ParameterValidationException {
        if (null == param) {
            throw new ParameterValidationException("parameter cannot be null", paramName);
        }
        final String result = param.trim();
        if (result.isEmpty()) {
            throw new ParameterValidationException("parameter cannot be empty", paramName);
        }
        if (!pattern.matcher(result).matches()) {
            throw new ParameterValidationException("invalid characters found", paramName);
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
    private String validatePatternOptionalField(final Pattern pattern, final String paramName, final String param)
            throws ParameterValidationException {
        if (null == param) {
            return "";
        }
        final String result = param.trim();
        if (result.isEmpty()) {
            return "";
        }
        if (!pattern.matcher(result).matches()) {
            throw new ParameterValidationException("invalid characters found", paramName);
        }
        return result;
    }

    /***
     * Validates a list of strings against a pattern, checking for unique entries
     * @param pattern the static pattern to match to
     * @param paramName the name of the param, to use in the exception message
     * @param paramList the list of param values
     * @return list of trimmed parameters, or empty list if it was null or empty
     * @throws ParameterValidationException
     */
    private List<String> validatePatternUniqueList(Pattern pattern, String paramName, List<String> paramList)
            throws ParameterValidationException {
        LinkedHashSet<String> results = new LinkedHashSet<>();
        if (null != paramList) {
            for (String param : paramList) {
                String validatedParam = validatePattern(pattern, paramName, param);
                if (!results.add(validatedParam)) {
                    throw new ParameterValidationException("duplicate items", paramName);
                }
            }
        }
        return new ArrayList<>(results);
    }

    public static class ParameterValidationException extends Exception {
        private final String paramName;

        public ParameterValidationException(final String message, final String paramName) {
            super(message);
            this.paramName = paramName;
        }

        public String getParameterName() {
            return this.paramName;
        }
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
    Float validateOptionalFloat(final String paramName, final float min, final float max, final float fallback,
                                final String param) throws ParameterValidationException {
        // if empty, return the fallback
        if ((null == param) || (param.isEmpty())) {
            return fallback;
        }
        return validateFloat(paramName, min, max, param);
    }

    /***
     * Interpret training source type and ensure it is valid
     * @param paramName
     * @param param
     * @return
     * @throws ParameterValidationException
     */
    TrainingLogic.TrainingType validateTrainingSourceType(final String paramName, final String param)
            throws ParameterValidationException {

        int num = validateInteger(paramName, param);
        try {
            return TrainingLogic.TrainingType.fromType(num);
        } catch (IllegalArgumentException ex) {
            throw new ParameterValidationException("invalid training type", paramName);
        }
    }

    int validateInteger(final String paramName, final String param) throws ParameterValidationException {
        if ((null == param) || (param.isEmpty())) {
            throw new ParameterValidationException("parameter null or empty", paramName);
        }
        try {
            return Integer.parseInt(param);
        } catch (NumberFormatException ex) {
            throw new ParameterValidationException("Invalid integer", paramName);
        }
    }

    int validateInteger(final String paramName, final int defaultValue, final String param)
            throws ParameterValidationException {
        if ((null == param) || (param.isEmpty())) {
            return defaultValue;
        }
        return validateInteger(paramName, param);
    }

    List<Integer> validateIntegerList(final String paramName, final List<String> params)
            throws ParameterValidationException {
        List<Integer> list = new ArrayList<>();
        List<String> paramsList = Tools.getListFromMultipeValuedParam(params);
        for (String param: paramsList) {
            list.add(validateInteger(paramName, param));
        }
        return list;
    }

    /***
     * Validates a floating point number
     * @param paramName parameter name used for exception
     * @param min valid range lowest value
     * @param max valid range highest value
     * @param param the parameter value
     * @return valid float representing the input, or fallback
     * @throws ParameterValidationException if the float was invalid or out of range
     */
    Float validateFloat(final String paramName, final float min, final float max, final String param)
            throws ParameterValidationException {
        // trim and convert commas to full-stops
        String newParam = param.trim().replace(',', '.');
        // check that it generally matches
        if (!floatPattern.matcher(newParam).matches()) {
            throw new ParameterValidationException("invalid parameter", paramName);
        }
        // parse
        final float result = Float.parseFloat(newParam);
        // just in case it's still weirdly invalid
        if (Float.isNaN(result)) {
            throw new ParameterValidationException("invalid float parameter", paramName);
        }
        // if it's out of range
        if ((result < min) || (result > max)) {
            throw new ParameterValidationException("out of range", paramName);
        }
        return result;
    }

    String validateTimezoneString(final String paramName, final String param) throws ParameterValidationException {
        if (param == null || param.isEmpty()) {
            throw new ParameterValidationException("parameter null or empty", paramName);
        }
        if (Arrays.stream(TimeZone.getAvailableIDs()).noneMatch(x -> x.equals(param))) {
            throw new ParameterValidationException("invalid timezone value: " + param, paramName);
        }
        return param;
    }

    UUID validateUuid(final String paramName, final String param) throws ParameterValidationException {
        final String result = validatePattern(uuidPattern, paramName, param);
        try {
            return UUID.fromString(result);
        } catch (final IllegalArgumentException iae) {
            throw new ParameterValidationException("invalid characters found", paramName);
        }
    }

    String validateAlphaNumPlusDashes(final String paramName, final String param) throws ParameterValidationException {
        return validatePattern(alphaNumericDashes, paramName, param);
    }

    String validateEntityName(final String paramName, final String param) throws ParameterValidationException {
        return validatePattern(entityNames, paramName, param);
    }

    String validateRequiredSanitized(final String paramName, final String param) throws ParameterValidationException {
        if (null == param) {
            throw new ParameterValidationException("missing parameter", paramName);
        }
        final String result = filterControlAndCoalesceSpaces(param);
        if (result.isEmpty()) {
            throw new ParameterValidationException("parameter cannot be empty", paramName);
        }
        return result;
    }

    String validateOptionalSanitized(final String param) throws ParameterValidationException {
        return filterControlAndCoalesceSpaces(param);
    }

    String validateAiName(final String paramName, final String param)
            throws ParameterValidationException {
        return validatePatternOptionalField(aiName, paramName, param);
    }

    String validateOptionalDescription(final String paramName, final String param)
            throws ParameterValidationException {
        return validatePatternOptionalField(printableAscii, paramName, param);
    }

    String validateOptionalSanitizeRemoveAt(final String paramName, final String param)
            throws ParameterValidationException {
        return validatePatternOptionalField(printableAsciiNoAt, paramName, param);
    }

    List<String> validateOptionalObjectValues(String paramName, List<String> paramList)
            throws ParameterValidationException {
        return validatePatternUniqueList(alphaNumDashesSomePunctuationAndSpace, paramName, paramList);
    }
}
