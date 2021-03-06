package com.hutoma.api.containers.sub;

import java.net.HttpURLConnection;

/**
 * HTTP Status Block
 */
public class Status {

    private int code;
    private String info;
    private Object additionalInfo;

    public static Status getCreated() {
        return Status.getCreated("Created");
    }

    public static Status getCreated(String message) {
        return (new Status()).setCode(HttpURLConnection.HTTP_CREATED).setInfo(message);
    }

    public static Status getCreated(String message, Object additionalInfo) {
        return getCreated(message).setAdditionalInfo(additionalInfo);
    }

    public static Status getSuccess() {
        return Status.getSuccess("OK");
    }

    public static Status getSuccess(String message) {
        return (new Status()).setCode(HttpURLConnection.HTTP_OK).setInfo(message);
    }

    public static Status getSuccess(String message, Object additionalInfo) {
        return getSuccess(message).setAdditionalInfo(additionalInfo);
    }

    public int getCode() {
        return this.code;
    }

    public Status setCode(int code) {
        this.code = code;
        return this;
    }

    public String getInfo() {
        return this.info;
    }

    public Status setInfo(String info) {
        this.info = info;
        return this;
    }

    public Status setAdditionalInfo(Object additionalInfo) {
        this.additionalInfo = additionalInfo;
        return this;
    }
}
