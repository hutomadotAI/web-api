package com.hutoma.api.containers.sub;

import java.net.HttpURLConnection;

/**
 * Created by David MG on 15/08/2016.
 */
public class Status {

    int code;
    String info;

    public int getCode() {
        return code;
    }

    public static Status getSuccess() {
        return Status.getSuccess("OK");
    }

    public static Status getSuccess(String message) {
        return (new Status()).setCode(HttpURLConnection.HTTP_OK).setInfo(message);
    }

    public Status setCode(int code) {
        this.code = code;
        return this;
    }

    public Status setInfo(String info) {
        this.info = info;
        return this;
    }
}
