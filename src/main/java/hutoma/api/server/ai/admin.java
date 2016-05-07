package hutoma.api.server.ai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hutoma.api.server.AWS.msg;
import hutoma.api.server.Role;
import hutoma.api.server.Secured;
import hutoma.api.server.utils.utils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.compression.CompressionCodecs;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.util.UUID;

/**
 * Created by mauriziocibelli on 27/04/16.
 */

@Path("/api/admin/")
@Secured({Role.ROLE_ADMIN})
public class admin {

    //curl -X POST -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNqqVgry93FVsgJT8Y4uvp5-SjpKxaVJQKHElNzMPKVaAAAAAP__.e-INR1D-L_sokTh9sZ9cBnImWI0n6yXXpDCmat1ca_c" http://localhost:8080/api/admin?id=test&role=ROLE_CLIENTONLY
    @POST
    @Secured({Role.ROLE_ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public String create_dev(
            @Context SecurityContext securityContext,
            @DefaultValue("ROLE_FREE") @QueryParam("role") String role,
            @DefaultValue("") @QueryParam("devid") String devid,
            @DefaultValue("") @QueryParam("username") String username,
            @DefaultValue("") @QueryParam("email") String email,
            @DefaultValue("") @QueryParam("password") String password,
            @DefaultValue("") @QueryParam("password_salt") String password_salt,
            @DefaultValue("") @QueryParam("name")  String name,
            @DefaultValue("") @QueryParam("attempt") String attempt,
            @DefaultValue("") @QueryParam("dev_token") String dev_token,
            @DefaultValue("0") @QueryParam("plan_id") int plan_id,
            @DefaultValue("") @QueryParam("dev_id") String dev_id) {


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
                        .claim("ROLE", role)
                        .setSubject(devid)
                        .compressWith(CompressionCodecs.DEFLATE)
                        .signWith(SignatureAlgorithm.HS256, encoding_key)
                        .compact();

                String token_client = Jwts.builder()
                        .claim("ROLE", Role.ROLE_CLIENTONLY)
                        .setSubject(devid)
                        .compressWith(CompressionCodecs.DEFLATE)
                        .signWith(SignatureAlgorithm.HS256, encoding_key)
                        .compact();

                ai.dev_token= token;
                ai.devid = devid;

                if (!hutoma.api.server.db.query.create_dev(username,email,password,password_salt,name,attempt,ai.dev_token,plan_id,ai.devid))
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

            if (!hutoma.api.server.db.query.delete_dev(devid))
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

}
