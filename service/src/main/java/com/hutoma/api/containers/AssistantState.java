package com.hutoma.api.containers;

/**
 * Created by bretc on 31/01/2017.
 */
public class AssistantState {

    private int currentAI = 1;
    private String hist = "";
    private String t = "";
    private int actionId = -1;
    private int alarmId = -1;
    private int topic = -1;
    private int flag = 0;
    private int ntries = 0;
    private int userid = (int)(Math.floor(Math.random() * 99999999) + 1);
    private int aiid = 464;

    public int getCurrentAI() {
        return currentAI;
    }

    public void setCurrentAI(final int currentAI) {
        this.currentAI = currentAI;
    }

    public String getHist() {
        return hist;
    }

    public void setHist(final String hist) {
        this.hist = hist;
    }

    public String getT() {
        return t;
    }

    public void setT(final String t) {
        this.t = t;
    }

    public int getActionId() {
        return actionId;
    }

    public void setActionId(final int actionId) {
        this.actionId = actionId;
    }

    public int getAlarmId() {
        return alarmId;
    }

    public void setAlarmId(final int alarmId) {
        this.alarmId = alarmId;
    }

    public int getTopic() {
        return topic;
    }

    public void setTopic(final int topic) {
        this.topic = topic;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(final int flag) {
        this.flag = flag;
    }

    public int getNtries() {
        return ntries;
    }

    public void setNtries(final int ntries) {
        this.ntries = ntries;
    }

    public int getUserid() {
        return userid;
    }

    public void setUserid(final int userid) {
        this.userid = userid;
    }

    public int getAiid() {
        return aiid;
    }

    public void setAiid(final int aiid) {
        this.aiid = aiid;
    }
}
