package com.hutoma.api.containers.sub;

import java.util.Arrays;
import java.util.Optional;

public enum ChatHandoverTarget {
    Ai("ai", 0),
    Human("human", 1),
    Other("other", 2);


    private String stringValue;
    private int intValue;
    ChatHandoverTarget(final String stringValue, final int intValue) {
        this.stringValue = stringValue;
        this.intValue = intValue;
    }

    public String getStringValue() {
        return this.stringValue;
    }

    public int getIntValue() {
        return this.intValue;
    }

    public static ChatHandoverTarget fromString(final String target) {
        Optional<ChatHandoverTarget> val = Arrays.stream(ChatHandoverTarget.values())
                .filter(x -> x.stringValue.equalsIgnoreCase(target)).findFirst();
        if (!val.isPresent()) {
            throw new IllegalArgumentException(String.format("%s is not a valid handover target", target));
        }
        return val.get();
    }

    public static ChatHandoverTarget fromInt(final int target) {
        Optional<ChatHandoverTarget> val = Arrays.stream(ChatHandoverTarget.values())
                .filter(x -> x.intValue == target).findFirst();
        if (!val.isPresent()) {
            throw new IllegalArgumentException(String.format("%d is not a valid handover target", target));
        }
        return val.get();
    }
}
