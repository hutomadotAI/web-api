package com.hutoma.api.containers.sub;

import java.util.List;

/**
 * Created by David MG on 16/08/2016.
 */
public class ChatResult {

    double score;
    String query = "";
    String answer = "";
    long elapsed_time;
    String action;
    List<Parameter> parameters;
    String context;

    public String getAnswer() {
        return answer;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public void setElapsed_time(long elapsed_time) {
        this.elapsed_time = elapsed_time;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public double getScore() {
        return score;
    }
}
