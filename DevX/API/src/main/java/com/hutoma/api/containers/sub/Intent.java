package com.hutoma.api.containers.sub;

import java.util.ArrayList;

/**
 * Created by mauriziocibelli on 04/09/16.
 * <p>
 * The main Intent Variable
 */

public class Intent {


    // intent name
    String intent_name = "";
    // the intent ID
    String intent_id = "";
    // true if all required variables have been defined during the conversation
    boolean fullfilled = false;
    // what topic the intent belogs to
    String topic = "";
    // all variables defining an intent
    ArrayList<Variable> variables;


    public void setIntent_name(String intent_name) {
        this.intent_name = intent_name;
    }

    public String getIntent_name(String intent_name) {
        return this.intent_name;
    }

    public void setIntent_id(String intent_id) {
        this.intent_id = intent_id;
    }

    public String getIntent_id(String intent_id) {
        return this.intent_id;
    }

    public boolean getFullfilled() {
        return this.fullfilled;
    }

    public void setFullfilled(boolean fullfilled) {
        this.fullfilled = fullfilled;
    }

    public String getTopic() {
        return this.topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public ArrayList<Variable> getVariables() {
        return this.variables;
    }

    public void setVariables(ArrayList<Variable> variables) {
        this.variables = variables;
    }


}
