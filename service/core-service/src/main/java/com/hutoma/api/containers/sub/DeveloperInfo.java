package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;
import java.util.UUID;

/**
 * Developer information (including non-public data).
 */
public class DeveloperInfo {

    @SerializedName("devid")
    private final UUID devId;
    private final String company;
    private final String website;
    private final String name;
    private final String email;
    private final String address;
    private final String postCode;
    private final String city;
    private final String country;

    public DeveloperInfo(final UUID devId, final String name, final String company, final String email,
                         final String address, final String postCode, final String city, final String country,
                         final String website) {
        this.devId = devId;
        this.company = company;
        this.website = website;
        this.name = name;
        this.email = email;
        this.address = address;
        this.postCode = postCode;
        this.city = city;
        this.country = country;
    }

    public String getName() {
        return this.name;
    }

    public String getEmail() {
        return this.email;
    }

    public String getAddress() {
        return this.address;
    }

    public String getPostCode() {
        return this.postCode;
    }

    public String getCity() {
        return this.city;
    }

    public String getCountry() {
        return this.country;
    }

    public UUID getDevId() {
        return this.devId;
    }

    public String getCompany() {
        return this.company;
    }

    public String getWebsite() {
        return this.website;
    }

    public DeveloperInfo getPublicInfo() {
        return new DeveloperInfo(this.devId, null, this.company, null, null, null, null, null, this.getWebsite());
    }
}
