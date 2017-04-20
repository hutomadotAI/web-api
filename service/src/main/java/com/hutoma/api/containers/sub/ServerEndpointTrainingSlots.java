package com.hutoma.api.containers.sub;

public class ServerEndpointTrainingSlots {

    private String endpoint;
    private int slotsInUse;
    private int slotsLapsed;
    private int trainingCapacity;
    private int chatCapacity;

    public ServerEndpointTrainingSlots(final String endpoint, final int slotsInUse, final int slotsLapsed) {
        this.endpoint = endpoint;
        this.slotsInUse = slotsInUse;
        this.slotsLapsed = slotsLapsed;
        this.trainingCapacity = 0;
        this.chatCapacity = 0;
    }

    /***
     * What proportion of the training capacity is in use (0.0-1.0)
     * @return
     */
    public double getLoadFactor() {
        return (this.trainingCapacity < 1) ? 1.0 :
                (double) this.slotsInUse / (double) this.trainingCapacity;
    }

    /***
     * How many slots are available.
     * @return
     */
    public double getAvailableSlotCount() {
        return Math.max(0, this.trainingCapacity - this.slotsInUse);
    }

    public String getEndpoint() {
        return this.endpoint;
    }

    public boolean hasFreeTrainingSlots() {
        return getAvailableSlotCount() > 0;
    }

    public int getTrainingCapacity() {
        return this.trainingCapacity;
    }

    public void setTrainingCapacity(final int trainingCapacity) {
        this.trainingCapacity = trainingCapacity;
    }

    public int getChatCapacity() {
        return this.chatCapacity;
    }

    public void setChatCapacity(final int chatCapacity) {
        this.chatCapacity = chatCapacity;
    }
}
