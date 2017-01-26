package com.hutoma.api.containers.sub;

/**
 * Created by subet on 13/09/2016.
 */
public class RateLimitStatus {

    boolean rateLimited;
    double tokens;
    boolean accountIsValid;

    public RateLimitStatus(boolean rateLimited, double tokens, boolean accountIsValid) {
        this.rateLimited = rateLimited;
        this.tokens = tokens;
        this.accountIsValid = accountIsValid;
    }

    public boolean isAccountValid() {
        return this.accountIsValid;
    }

    public boolean isRateLimited() {
        return this.rateLimited;
    }

    public double getTokens() {
        return this.tokens;
    }
}
