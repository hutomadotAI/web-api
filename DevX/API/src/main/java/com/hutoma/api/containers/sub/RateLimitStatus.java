package com.hutoma.api.containers.sub;

/**
 * Created by subet on 13/09/2016.
 */
public class RateLimitStatus {

    boolean rateLimited;
    double tokens;

    public RateLimitStatus(boolean rateLimited, double tokens) {
        this.rateLimited = rateLimited;
        this.tokens = tokens;
    }

    public boolean isRateLimited() {
        return rateLimited;
    }

    public double getTokens() {
        return tokens;
    }
}
