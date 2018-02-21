package com.hutoma.api.validation;

import com.hutoma.api.containers.ApiError;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

abstract class ValidationBase {

    // parameter names
    protected static final String AIID = "aiid";
    protected static final String DEVID = "_developer_id";

    private static final Pattern uuidPattern =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    UUID validateUuid(final String paramName, final String param) throws ParameterValidationException {
        final String result = validatePattern(uuidPattern, paramName, param);
        try {
            return UUID.fromString(result);
        } catch (final IllegalArgumentException iae) {
            throw new ParameterValidationException("invalid characters found", paramName);
        }
    }

    /***
     * Validates a parameter against a pattern
     * @param pattern the static pattern to match to
     * @param paramName the name of the param, to use in the exception message
     * @param param the param value
     * @return trimmed parameter
     * @throws ParameterValidationException if the parameter is empty, null or invalid
     */
    String validatePattern(final Pattern pattern, final String paramName, final String param)
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

    static ApiError getValidationBadRequest(final ParameterValidationException pve) {
        String paramName = pve.getParameterName();
        String message = pve.getMessage();
        return ApiError.getBadRequest(String.format("%s%s%s",
                (paramName == null) ? "" : paramName,
                (paramName == null || message == null) ? "" : ": ",
                (message == null) ? "" : message),
                null);
    }

    /***
     * Throw a validation exception if the string is too long
     * @param maxLength
     * @param paramName
     * @param param
     * @return
     * @throws ParameterValidationException
     */
    String validateFieldLength(final int maxLength, final String paramName, final String param)
            throws ParameterValidationException {
        if ((null != param) && param.length() > maxLength) {
            throw new ParameterValidationException("parameter too long", paramName);
        }
        return param;
    }

    /***
     * Avoids null pointers when the list is null or empty
     * @param list
     * @return empty string or the first string in the list if available
     */
    String getFirst(final List<String> list) {
        return ((null == list) || (list.isEmpty())) ? "" : list.get(0);
    }

    /***
     * Gets the first parameter value, or a default value if there is none
     * @param list
     * @param defaultValue
     * @return
     */
    String getFirstOrDefault(final List<String> list, final String defaultValue) {
        return ((null == list) || (list.isEmpty())) ? defaultValue : list.get(0);
    }
}
