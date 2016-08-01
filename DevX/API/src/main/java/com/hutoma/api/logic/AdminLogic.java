package com.hutoma.api.logic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hutoma.api.server.AWS.msg;
import com.hutoma.api.auth.Role;
import com.hutoma.api.auth.Secured;
import hutoma.api.server.ai.api_root;
import hutoma.api.server.utils.utils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.compression.CompressionCodecs;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Service;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.util.UUID;

/**
 * Created by mauriziocibelli on 27/04/16.
 */
@Contract
@Service
public class AdminLogic {

    public AdminLogic() {
        System.out.println("AdminLogic constructor");
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

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            api_root._myAIs ai = new api_root._myAIs();
            api_root._status st = new api_root._status();
            st.code = 200;
            st.info ="success";
            ai.status =st;
            ai.dev_token="";
            try {
                String encoding_key = utils.getConfigProp("encoding_key");
                UUID guid = java.util.UUID.randomUUID();
//                String[] ECs;
//                ECs = utils.getConfigProp("wnet_instances").split(",");
//                for (String ec:ECs) hutoma.api.server.utils.remotefs.cmd(ec, "mkdir ~/ai/" + devid);
                String token = Jwts.builder()
                        .claim("ROLE", securityRole)
                        .setSubject(developerID)
                        .compressWith(CompressionCodecs.DEFLATE)
                        .signWith(SignatureAlgorithm.HS256, encoding_key)
                        .compact();

                String token_client = Jwts.builder()
                        .claim("ROLE", Role.ROLE_CLIENTONLY)
                        .setSubject(developerID)
                        .compressWith(CompressionCodecs.DEFLATE)
                        .signWith(SignatureAlgorithm.HS256, encoding_key)
                        .compact();

                ai.dev_token= token;
                ai.devid = developerID;

                if (!hutoma.api.server.db.ai.create_dev(username, email, password, passwordSalt, name, attempt, ai.dev_token, planId, ai.devid))
                {
                    st.code = 500;
                    st.info = "Internal Server Error.";
                }

                //localfs.createFolder(System.getProperty("user.home")+"/ai/" + devid);
            }
            catch (Exception e){
                st.code = 500;
                st.info = "Internal Server Error.";
            }
            return gson.toJson(ai);
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
