package com.hutoma.api.access;

import java.util.Arrays;
import java.util.Optional;

/**
 * Created by mauriziocibelli on 25/04/16.
 */
public enum Role {
    NONE(0),
    ROLE_ADMIN(9999),
    ROLE_CLIENTONLY(2),
    ROLE_FREE(1),
    ROLE_PLAN_1(10),
    ROLE_PLAN_2(11),
    ROLE_PLAN_3(12),
    ROLE_PLAN_4(13),
    ROLE_TEST(3);

    private int plan;
    Role(final int plan) {
        this.plan = plan;
    }
    public int getPlan() {
        return this.plan;
    }

    public static Role fromString(final String stringRole) throws InvalidRoleException {
        Optional<Role> optRole = Arrays.stream(Role.values()).filter(x -> x.toString().equals(stringRole)).findFirst();
        if (optRole.isPresent()) {
            return optRole.get();
        }
        throw new InvalidRoleException(String.format("Role %s is invalid", stringRole));
    }

    public static Role fromPlan(final int plan) throws InvalidRoleException {
        Optional<Role> optRole = Arrays.stream(Role.values()).filter(x -> x.getPlan() == plan).findFirst();
        if (optRole.isPresent()) {
            return optRole.get();
        }
        throw new InvalidRoleException(String.format("Role plan %d is invalid", plan));
    }
}