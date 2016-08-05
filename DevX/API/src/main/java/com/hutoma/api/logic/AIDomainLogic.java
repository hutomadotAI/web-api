package com.hutoma.api.logic;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hutoma.api.auth.Role;
import com.hutoma.api.auth.Secured;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.MessageQueue;
import hutoma.api.server.ai.api_root;
import hutoma.api.server.db.domain;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;

/**
 * Created by Hutoma on 15/07/16.
 */
public class AIDomainLogic {

    Config config;
    JsonSerializer jsonSerializer;
    Database database;

    @Inject
    public AIDomainLogic(Config config, JsonSerializer jsonSerializer, Database database) {
        this.config = config;
        this.jsonSerializer = jsonSerializer;
        this.database = database;
    }

    public String getDomains(
            SecurityContext securityContext,
            String devid) {
        api_root._status st = new api_root._status();
        api_root._domainList _domain = new api_root._domainList();
        st.code = 200;
        st.info ="success";
        _domain.status = st;
        try {
            ArrayList<api_root._domain> listdomains = new ArrayList<>();
            listdomains = database.getAllDomains();
            if (listdomains.size() <= 0) {
                st.code = 500;
                st.info = "Internal Server Error.";
            } else {
                _domain.domain_list = new ArrayList<api_root._domain>();
                _domain.domain_list = listdomains;
            }
        }
        catch (Exception e){
            st.code = 500;
            st.info = "Error:Internal Server Error.";
        }
        return jsonSerializer.serialize(_domain);
    }
}