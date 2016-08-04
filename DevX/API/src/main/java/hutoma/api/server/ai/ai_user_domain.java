package hutoma.api.server.ai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hutoma.api.auth.Role;
import com.hutoma.api.auth.Secured;
import hutoma.api.server.db.userAIDomains;
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

import static hutoma.api.server.db.userAIDomains.get_single_userAIDomain;

/**
 * Created by Hutoma on 15/07/16.
 */
@Path("/ai/{aiid}/domains")
public class ai_user_domain {

    @POST
    @Secured({Role.ROLE_FREE,Role.ROLE_PLAN_1,Role.ROLE_PLAN_2,Role.ROLE_PLAN_3,Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public String createUserAI_Domain(
            @Context SecurityContext securityContext,
            @DefaultValue("") @HeaderParam("_developer_id") String devid,
            @DefaultValue("") @QueryParam("aiid") String aiid,
            @DefaultValue("") @QueryParam("dom_id") String dom_id,
            @DefaultValue("0") @QueryParam("active") Boolean active)
    {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        api_root._status st = new api_root._status();
        api_root._userAIDomain _ai_user_domain = new api_root._userAIDomain();
        st.code = 200;
        st.info ="success";
        _ai_user_domain.status = st;

        try {
            if(!hutoma.api.server.db.userAIDomains.create_userAIDomain(devid,aiid,dom_id,active))
            {
                st.code = 500;
                st.info = "Internal Server Error.";
            }
        }
        catch (Exception e){
            st.code = 500;
            st.info = "Error:Internal Server Error.";
        }
        return gson.toJson(_ai_user_domain);
    }


    @GET
    @Secured({Role.ROLE_FREE,Role.ROLE_PLAN_1,Role.ROLE_PLAN_2,Role.ROLE_PLAN_3,Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public String getUserAI_all_Domains(
            @Context SecurityContext securityContext,
            @DefaultValue("") @HeaderParam("_developer_id") String devid,
            @DefaultValue("") @QueryParam("aiid") String aiid )
    {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        api_root._status st = new api_root._status();
        api_root._userAIDomainList _user_ai_domains = new api_root._userAIDomainList();
        st.code = 200;
        st.info ="success";
        _user_ai_domains.status = st;

        try {

            ArrayList<api_root._userAIDomain> user_ai_listdomains = new ArrayList<>();
            user_ai_listdomains = userAIDomains.get_all_userAIDomains(devid,aiid);
            if (user_ai_listdomains.size() <= 0) {
                st.code = 500;
                st.info = "Internal Server Error.";
            } else {
                _user_ai_domains.userAIdomain_list = new ArrayList<api_root._userAIDomain>();
                _user_ai_domains.userAIdomain_list = user_ai_listdomains;
            }
        }
        catch (Exception e){
            st.code = 500;
            st.info = "Error:Internal Server Error.";
        }
        return gson.toJson(_user_ai_domains);
    }


    @GET
    @Secured({Role.ROLE_FREE,Role.ROLE_PLAN_1,Role.ROLE_PLAN_2,Role.ROLE_PLAN_3,Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public String getUserAI_single_Domain(
            @Context SecurityContext securityContext,
            @DefaultValue("") @HeaderParam("_developer_id") String devid,
            @DefaultValue("") @QueryParam("aiid") String aiid,
            @DefaultValue("") @QueryParam("dom_id") String dom_id)
    {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        api_root._status st = new api_root._status();
        api_root._userAIDomain _ai_user_domain = new api_root._userAIDomain();
        st.code = 200;
        st.info ="success";
        _ai_user_domain.status = st;

        try {
            _ai_user_domain = get_single_userAIDomain(devid,aiid,dom_id);
        } catch (Exception e){
            st.code = 500;
            st.info = "Error:Internal Server Error.";
        }

        return gson.toJson(_ai_user_domain);
    }

    @POST
    @Secured({Role.ROLE_FREE,Role.ROLE_PLAN_1,Role.ROLE_PLAN_2,Role.ROLE_PLAN_3,Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public String updateUserAI_Domain(
            @Context SecurityContext securityContext,
            @DefaultValue("") @HeaderParam("_developer_id") String devid,
            @DefaultValue("") @QueryParam("aiid") String aiid,
            @DefaultValue("") @QueryParam("dom_id") String dom_id,
            @DefaultValue("0") @QueryParam("active") Boolean active)
    {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        api_root._status st = new api_root._status();
        api_root._userAIDomain _ai_user_domain = new api_root._userAIDomain();
        st.code = 200;
        st.info ="success";
        _ai_user_domain.status = st;

        try {
            if(!hutoma.api.server.db.userAIDomains.update_userAIDomain(devid,aiid,dom_id,active))
            {
                st.code = 500;
                st.info = "Internal Server Error.";
            }
        }
        catch (Exception e){
            st.code = 500;
            st.info = "Error:Internal Server Error.";
        }
        return gson.toJson(_ai_user_domain);
    }

    @DELETE
    @Secured({Role.ROLE_FREE,Role.ROLE_PLAN_1,Role.ROLE_PLAN_2,Role.ROLE_PLAN_3,Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public String deleteUserAI_Domain(
            @Context SecurityContext securityContext,
            @DefaultValue("") @HeaderParam("_developer_id") String devid,
            @DefaultValue("") @QueryParam("aiid") String aiid,
            @DefaultValue("") @QueryParam("dom_id") String dom_id)
    {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        api_root._status st = new api_root._status();
        api_root._userAIDomain _ai_user_domain = new api_root._userAIDomain();
        st.code = 200;
        st.info ="success";
        _ai_user_domain.status = st;

        try {
            if(!hutoma.api.server.db.userAIDomains.delete_userAIDomain(devid,aiid,dom_id))
            {
                st.code = 500;
                st.info = "Internal Server Error.";
            }
        }
        catch (Exception e){
            st.code = 500;
            st.info = "Error:Internal Server Error.";
        }
        return gson.toJson(_ai_user_domain);
    }

}