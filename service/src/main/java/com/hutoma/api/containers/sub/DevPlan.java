package com.hutoma.api.containers.sub;

/**
 * Developer Plan
 */
public class DevPlan {
    private final int maxTrainingSecs;
    private final int maxNumAis;
    private final long maxMemory;
    private final int maxMonthlyCalls;

    /**
     * Ctor.
     * @param maxNumAis       maximum number of AIs
     * @param maxMonthlyCalls maximum number of calls per month
     * @param maxMemory       maximum memory to use
     * @param maxTrainingSecs maximum training time (in seconds)
     */
    public DevPlan(final int maxNumAis, final int maxMonthlyCalls, final long maxMemory, final int maxTrainingSecs) {
        this.maxMemory = maxMemory;
        this.maxMonthlyCalls = maxMonthlyCalls;
        this.maxNumAis = maxNumAis;
        this.maxTrainingSecs = maxTrainingSecs;
    }

    /**
     * Gets the maximum training time (in seconds).
     * @return the maximum training time (in seconds)
     */
    public int getMaxTrainingSecs() {
        return this.maxTrainingSecs;
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
