package com.hutoma.api.containers.sub;

/**
 * Developer Plan
 */
public class DevPlan {
    private final int maxTrainingMins;
    private final int maxNumAis;
    private final long maxMemory;
    private final int maxMonthlyCalls;

    /**
     * Ctor.
     * @param maxNumAis       maximum number of AIs
     * @param maxMonthlyCalls maximum number of calls per month
     * @param maxMemory       maximum memory to use
     * @param maxTrainingMins maximum training time (in minutes)
     */
    public DevPlan(final int maxNumAis, final int maxMonthlyCalls, final long maxMemory, final int maxTrainingMins) {
        this.maxMemory = maxMemory;
        this.maxMonthlyCalls = maxMonthlyCalls;
        this.maxNumAis = maxNumAis;
        this.maxTrainingMins = maxTrainingMins;
    }

    /**
     * Gets the maximum training time (in minutes).
     * @return the maximum training time (in minutes)
     */
    public int getMaxTrainingMins() {
        return this.maxTrainingMins;
    }

    /**
     * Gets the maximum number of AIs.
     * @return the maximum number of AIs
     */
    public int getMaxNumAis() {
        return this.maxNumAis;
    }

    /**
     * Gets the maximum memory to use.
     * @return the maximum memory to use
     */
    public long getMaxMemory() {
        return this.maxMemory;
    }

    /**
     * Gets the maximum number of calls per month.
     * @return the maximum number of calls per month
     */
    public int getMaxMonthlyCalls() {
        return this.maxMonthlyCalls;
    }
}
