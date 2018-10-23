package com.hutoma.api.containers.sub;

import java.util.Arrays;
import java.util.Optional;

public enum EntityValueType {
    LIST,       // List of strings
    REGEX,      // RegEx expression
    SYS;        // System entity

    public static EntityValueType fromString(final String s) {
        Optional<EntityValueType> opt = Arrays.stream(EntityValueType.values())
                .filter(x -> s.equals(x.name())).findFirst();
        return opt.orElse(EntityValueType.LIST);
    }
}
