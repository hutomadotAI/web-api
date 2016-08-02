package com.hutoma.api.common;

import com.hutoma.api.common.IUuidTools;

import java.util.UUID;

/**
 * Created by David MG on 02/08/2016.
 */
public class UuidTools implements IUuidTools {

    @Override
    public UUID createNewRandomUUID() {
        return java.util.UUID.randomUUID();
    }
}
