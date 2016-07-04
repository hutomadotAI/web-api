package hutoma.api.server.ai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hutoma.api.server.AWS.msg;
import hutoma.api.server.Role;
import hutoma.api.server.Secured;
import hutoma.api.server.db.ai;
import hutoma.api.server.utils.utils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.compression.CompressionCodecs;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by mauriziocibelli on 27/04/16.
 */
@Path("/ai/")
public class aI_CRUD extends  api_root {

    //curl -X POST -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNqqVgry93FVsgJT8W5Brq5KOkrFpUlAkYzSkvzcRKVaAAAAAP__.kkftTodFfH_kRQANoqT1B96BslSHu1VzM5VC_p6bBcA" http://localhost:8080/api/
    @POST
    @Secured({Role.ROLE_FREE,Role.ROLE_PLAN_1,Role.ROLE_PLAN_2,Role.ROLE_PLAN_3,Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public String createAI(
            @Context SecurityContext securityContext,
            @DefaultValue("") @HeaderParam("_developer_id") String devid,
            @DefaultValue("") @QueryParam("name") String name,
            @DefaultValue("") @QueryParam("description") String description,
            @DefaultValue("false") @QueryParam("is_private") boolean is_private,
            @DefaultValue("0.0") @QueryParam("deep_learning_error") double deep_learning_error,
            @DefaultValue("0") @QueryParam("deep_learning_status") int deep_learning_status,
            @DefaultValue("0") @QueryParam("shallow_learning_status") int shallow_learning_status,
            @DefaultValue("0") @QueryParam("status") int status)

    {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        _newai ai = new _newai();
        _status st = new _status();
        st.code = 200;
        st.info ="success";
        ai.status =st;
        ai.client_token="";
        try {
            String encoding_key = utils.getConfigProp("encoding_key");
            UUID guid = java.util.UUID.randomUUID();//
            String token = Jwts.builder()
                    .claim("ROLE", Role.ROLE_CLIENTONLY)
                    .claim("AIID", guid)
                    .setSubject(devid)
                    .compressWith(CompressionCodecs.DEFLATE)
                    .signWith(SignatureAlgorithm.HS256, encoding_key)
                    .compact();
            ai.client_token= token;
            ai.aiid = ""+guid;

            if(!hutoma.api.server.db.ai.create_ai(ai.aiid, name, description, devid, is_private, deep_learning_error, deep_learning_status, shallow_learning_status, status, ai.client_token, ""))
            {
                st.code = 500;
                st.info = "Error:Internal Server Error.";
            }

            // String[] ECs;
            //ECs = utils.getConfigProp("wnet_instances").split(",");
            // for (String ec:ECs) hutoma.api.server.utils.remotefs.cmd(ec, "mkdir ~/ai/" + devid + "/" + guid);
            //localfs.createFolder(System.getProperty("user.home") + "/ai/" + devid + "/" + guid);
        }
        catch (Exception e){
            st.code = 500;
            st.info = "Error:Internal Server Error.";
        }
        return gson.toJson(ai);
    }

    //curl -X POST -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNqqVgry93FVsgJT8W5Brq5KOkrFpUlAkYzSkvzcRKVaAAAAAP__.kkftTodFfH_kRQANoqT1B96BslSHu1VzM5VC_p6bBcA" http://localhost:8080/api/
    @GET
    @Secured({Role.ROLE_FREE,Role.ROLE_PLAN_1,Role.ROLE_PLAN_2,Role.ROLE_PLAN_3,Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public String getAIs(
            @Context SecurityContext securityContext,
            @DefaultValue("") @HeaderParam("_developer_id") String devid) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        _status st = new _status();
        _myAIs _ai = new _myAIs();
        st.code = 200;
        st.info ="success";
        _ai.status = st;

       try {
           ArrayList<_ai> myais = new ArrayList<>();
           myais = ai.get_all_ai(devid);

           if (myais.size() <= 0) {
               st.code = 500;
               st.info = "Internal Server Error.";
           } else {
               _ai.ai_list = new ArrayList<_ai>();
               _ai.ai_list = myais;


           }
       }
       catch (Exception e){
           st.code = 500;
           st.info = "Error:Internal Server Error.";
       }

        return gson.toJson(_ai);
    }




    @Path("/{aiid}/")
    @GET
    @Secured({Role.ROLE_FREE,Role.ROLE_PLAN_1,Role.ROLE_PLAN_2,Role.ROLE_PLAN_3,Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public String getsingle_ai(
            @Context SecurityContext securityContext,
            @DefaultValue("") @HeaderParam("_developer_id") String devid,
            @PathParam("aiid") String aiid) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        _status st = new _status();
        _myAIs _myai = new _myAIs();
        st.code = 200;
        st.info ="success";
        _myai.status = st;

        try {
            _myai.ai = ai.get_ai(aiid);
        } catch (Exception e){
            st.code = 500;
            st.info = "Error:Internal Server Error.";
        }

        return gson.toJson(_myai);
    }

    @DELETE
    @Secured({Role.ROLE_FREE,Role.ROLE_PLAN_1,Role.ROLE_PLAN_2,Role.ROLE_PLAN_3,Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{aiid}")
    public String deleteAI(
            @Context SecurityContext securityContext,
            @PathParam("aiid") String aiid,
            @DefaultValue("") @HeaderParam("_developer_id") String devid) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        _newai ai = new _newai();
        _status st = new _status();
        st.code = 200;
        st.info ="success";
        ai.status =st;
        try {


            if(!hutoma.api.server.db.ai.delete_ai(aiid))
            {
                st.code = 500;
                st.info = "Internal Server Error.";
            }
            hutoma.api.server.AWS.SQS.push_msg(utils.getConfigProp("core_queue"),msg.delete_ai + "|" + devid + "|" + aiid);

        }
        catch (Exception e){
            st.code = 500;
            st.info = "Error:Internal Server Error.";
        }
        return gson.toJson(ai);
    }




}
