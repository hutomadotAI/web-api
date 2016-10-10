package com.hutoma.api.containers.sub;

/**
 * Created by mauriziocibelli on 04/09/16.
 */

/**
 * A simple class holding a key-value pair.
 * We use this class when we need to build a chatbot response that contains intent variables
 * The might be of interest to the developer
 */

public class Variable {

    // Variable Name
    String var = "";
    // Variable Key
    String key = "";


    /***
     * Get a variable
     * @param var variable name
     * @param key variable key
     */
    public void setVar(String var, String key) {
        this.var = var;
        this.key = key;
    }

    /***
     * Returns the value of a var
     * @param var the name of the variable
     * @return the key associated to that variable
     */
    public String getVar(String var) {
        return this.key;
    }


}
