package com.hutoma.api.common;

import com.hutoma.api.containers.sub.DeveloperInfo;

import static com.hutoma.api.common.TestDataHelper.DEVID;
import static com.hutoma.api.common.TestDataHelper.DEVID_UUID;

/**
 * Created by pedrotei on 09/01/17.
 */
public class DeveloperInfoHelper {

    public static final DeveloperInfo DEVINFO = new DeveloperInfo(
            DEVID_UUID, "name", "company", "email@email.com", "address", "post code", "city", "country", "http://web"
    );
}
