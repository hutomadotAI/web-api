package com.hutoma.api.logic;

import com.hutoma.api.auth.Role;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.IJsonSerializer;
import com.hutoma.api.common.IUuidTools;
import com.hutoma.api.connectors.Database;
import hutoma.api.server.ai.api_root;
import hutoma.api.server.utils.utils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.compression.CompressionCodecs;
import org.glassfish.jersey.spi.Contract;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;
import java.util.UUID;

/**
 * Created by mauriziocibelli on 27/04/16.
 */
@Contract
@Service
public class AdminLogic {

    Config config;
    IJsonSerializer jsonSerializer;
    Database database;

    @Inject
    public AdminLogic(Config config, IJsonSerializer jsonSerializer, Database database) {
        this.config = config;
        this.jsonSerializer = jsonSerializer;
        this.database = database;
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
            catch (Exception e){
                st.code = 500;
                st.info = "Internal Server Error.";
            }
            return jsonSerializer.serialize(ai);
    }

/*
   // curl -X DELETE -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNqqVgry93FVsgJTY4uvp5-SjpKxaVJQKHElNzMPKVaAAAAAP__.e-INR1D-L_sokTh9sZ9cBnImWI0n6yXXpDCmat1ca_c" http://localhost:8080/api/admin?id=test2
    @DELETE
    @Secured({Role.ROLE_ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public String delete_dev(
            @Context SecurityContext securityContext,
            @DefaultValue("") @QueryParam("devid") String devid) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        api_root._newai ai = new api_root._newai();
        api_root._status st = new api_root._status();
        st.code = 200;
        st.info ="success";
        ai.status =st;
        try {

            if (!hutoma.api.server.db.dev.delete_dev(devid))
            {
                st.code = 500;
                st.info = "Internal Server Error.";
            }
            hutoma.api.server.AWS.SQS.push_msg(utils.getConfigProp("core_queue"),msg.delete_dev + "|" + devid + "|000");
        }
        catch (Exception e){
            st.code = 500;
            st.info = "Internal Server Error.";
        }
        return gson.toJson(ai);
    }
*/
}
