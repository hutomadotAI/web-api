package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.MessageQueue;
import hutoma.api.server.ai.api_root;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.compression.CompressionCodecs;
import org.glassfish.jersey.spi.Contract;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;

/**
 * Created by mauriziocibelli on 27/04/16.
 */
@Contract
@Service
public class AdminLogic {

    Config config;
    JsonSerializer jsonSerializer;
    Database database;
    MessageQueue messageQueue;

    @Inject
    public AdminLogic(Config config, JsonSerializer jsonSerializer, Database database, MessageQueue messageQueue) {
        this.config = config;
        this.jsonSerializer = jsonSerializer;
        this.database = database;
        this.messageQueue = messageQueue;
    }

    public String createDev(
            SecurityContext securityContext,
            String securityRole,
            String developerID,
            String username,
            String email,
            String password,
            String passwordSalt,
            String name,
            String attempt,
            String developerToken,
            int planId,
            String dev_id) {

            api_root._myAIs ai = new api_root._myAIs();
            api_root._status st = new api_root._status();
            st.code = 200;
            st.info ="success";
            ai.status =st;
            ai.dev_token="";

            try {
                String encoding_key = config.getEncodingKey();

                String token = Jwts.builder()
                        .claim("ROLE", securityRole)
                        .setSubject(developerID)
                        .compressWith(CompressionCodecs.DEFLATE)
                        .signWith(SignatureAlgorithm.HS256, encoding_key)
                        .compact();

                ai.dev_token= token;
                ai.devid = developerID;

                if (!database.createDev(username, email, password, passwordSalt, name, attempt, ai.dev_token, planId, ai.devid)) {
                    st.code = 500;
                    st.info = "Internal Server Error.";
                }

            }
            catch (Exception e) {
                st.code = 500;
                st.info = "Internal Server Error.";
            }
            return jsonSerializer.serialize(ai);
    }

    public String deleteDev(
            SecurityContext securityContext,
            String devid) {

        api_root._newai ai = new api_root._newai();
        api_root._status st = new api_root._status();
        st.code = 200;
        st.info ="success";
        ai.status = st;

        try {
            if (!database.deleteDev(devid)) {
                st.code = 500;
                st.info = "Internal Server Error.";
            }
            messageQueue.pushMessageDeleteDev(devid);
        }
        catch (Exception e){
            st.code = 500;
            st.info = "Internal Server Error.";
        }
        return jsonSerializer.serialize(ai);
    }
}
