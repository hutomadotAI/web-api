package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;

/**
 * Created by pedrotei on 03/01/17.
 */
public class DeveloperInfo {

    @SerializedName("devid")
    private final String devId;
    private final String name;
    private final String company;
    private final String email;
    private final String address;
    private final String postCode;
    private final String city;
    private final String country;
    private final String website;

    public DeveloperInfo(final String devId, final String name, final String company, final String email,
                         final String address, final String postCode, final String city, final String country,
                         final String website) {
        this.devId = devId;
        this.name = name;
        this.company = company;
        this.email = email;
        this.address = address;
        this.postCode = postCode;
        this.city = city;
        this.country = country;
        this.website = website;
    }

    public String getDevId() {
        return devId;
    }

    public String getName() {
        return name;
    }

    public String getCompany() {
        return company;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public String getPostCode() {
        return postCode;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getWebsite() {
        return website;
    }
}
