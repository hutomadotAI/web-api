package com.hutoma.api.logic;

import com.hutoma.api.auth.Role;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.MessageQueue;
import hutoma.api.server.ai.api_root;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.compression.CompressionCodecs;

import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by mauriziocibelli on 27/04/16.
 */
public class AILogic {

    Config config;
    JsonSerializer jsonSerializer;
    Database database;
    MessageQueue messageQueue;
    Tools tools;

    @Inject
    public AILogic(Config config, JsonSerializer jsonSerializer, Database database, MessageQueue messageQueue, Tools tools) {
        this.config = config;
        this.jsonSerializer = jsonSerializer;
        this.database = database;
        this.messageQueue = messageQueue;
        this.tools = tools;
    }

    public String createAI(
            SecurityContext securityContext,
            String devid,
            String name,
            String description,
            boolean is_private,
            double deep_learning_error,
            int deep_learning_status,
            int shallow_learning_status,
            int status)
    {
        api_root._newai ai = new api_root._newai();
        api_root._status st = new api_root._status();
        st.code = 200;
        st.info ="success";
        ai.status =st;
        ai.client_token="";
        try {
            String encoding_key = config.getEncodingKey();
            UUID guid = tools.createNewRandomUUID();
            String token = Jwts.builder()
                    .claim("ROLE", Role.ROLE_CLIENTONLY)
                    .claim("AIID", guid)
                    .setSubject(devid)
                    .compressWith(CompressionCodecs.DEFLATE)
                    .signWith(SignatureAlgorithm.HS256, encoding_key)
                    .compact();
            ai.client_token= token;
            ai.aiid = guid.toString();

            if (database.createAI(ai.aiid, name, description, devid, is_private, deep_learning_error, deep_learning_status, shallow_learning_status, status, ai.client_token, "")) {
                st.code = 500;
                st.info = "Error:Internal Server Error.";
            }
        }
        catch (Exception e){
            st.code = 500;
            st.info = "Error:Internal Server Error.";
        }
        return jsonSerializer.serialize(ai);
    }

    public String getAIs(
            SecurityContext securityContext,
            String devid) {

        api_root._status st = new api_root._status();
        api_root._myAIs _ai = new api_root._myAIs();
        st.code = 200;
        st.info ="success";
        _ai.status = st;

       try {
           ArrayList<api_root._ai> myais = database.getAllAIs(devid);

           if (myais.size() <= 0) {
               st.code = 500;
               st.info = "Internal Server Error.";
           } else {
               _ai.ai_list = myais;
           }
       }
       catch (Exception e){
           st.code = 500;
           st.info = "Error:Internal Server Error.";
       }
       return jsonSerializer.serialize(_ai);
    }

    public String getSingleAI(
            SecurityContext securityContext,
            String devid,
            String aiid) {

        api_root._status st = new api_root._status();
        api_root._myAIs _myai = new api_root._myAIs();
        st.code = 200;
        st.info ="success";
        _myai.status = st;

        try {
            _myai.ai = database.getAI(aiid);
        } catch (Exception e){
            st.code = 500;
            st.info = "Error:Internal Server Error.";
        }

        return jsonSerializer.serialize(_myai);
    }

    public String deleteAI(
            SecurityContext securityContext,
            String aiid,
            String devid) {

        api_root._newai ai = new api_root._newai();
        api_root._status st = new api_root._status();
        st.code = 200;
        st.info ="success";
        ai.status =st;
        try {
            if(!database.deleteAi(aiid))
            {
                st.code = 500;
                st.info = "Internal Server Error.";
            }
            messageQueue.pushMessageDeleteAI(config, devid, aiid);
        }
        catch (Exception e){
            st.code = 500;
            st.info = "Error:Internal Server Error.";
        }
        return jsonSerializer.serialize(ai);
    }

}
