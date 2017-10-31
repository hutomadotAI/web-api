package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

/**
 * User information.
 */
public class UserInfo {
    @SerializedName("name")
    private String name;
    @SerializedName("username")
    private String username;
    @SerializedName("email")
    private String email;
    @SerializedName("created")
    private DateTime created;
    @SerializedName("valid")
    private boolean valid;
    @SerializedName("internal")
    private boolean internal;
    @SerializedName("encrypted_password")
    private final String password;
    @SerializedName("password_salt")
    private final String passwordSalt;
    @SerializedName("dev_id")
    private final String devId;
    @SerializedName("attempts")
    private final String attempts;
    @SerializedName("id")
    private final int id;
    @SerializedName("dev_token")
    private final String devToken;

    public UserInfo(final String name, final String username, final String email, final DateTime created,
                    final boolean valid, final boolean internal, final String password, final String passwordSalt,
                    final String devId, final String attempts, final int id, final String devToken) {
        this.name = name;
        this.username = username;
        this.email = email;
        this.created = created;
        this.valid = valid;
        this.internal = internal;
        this.password = password;
        this.passwordSalt = passwordSalt;
        this.devId = devId;
        this.attempts = attempts;
        this.id = id;
        this.devToken = devToken;
    }
}
