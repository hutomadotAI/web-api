package hutoma.api.server.ai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hutoma.api.auth.Role;
import com.hutoma.api.auth.Secured;
import hutoma.api.server.db.userAIDomains;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;

/**
 * Created by Hutoma on 15/07/16.
 */
@Path("/ai/domain")
public class ai_user_domain {
    @GET
    @Secured({Role.ROLE_FREE,Role.ROLE_PLAN_1,Role.ROLE_PLAN_2,Role.ROLE_PLAN_3,Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public String getUserAIDomains(
            @Context SecurityContext securityContext,
            @DefaultValue("") @HeaderParam("_developer_id") String devid,
            @DefaultValue("") @QueryParam("aiid") String aiid,
            @DefaultValue("false") @QueryParam("dom_id") String dom_id)
    {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        api_root._status st = new api_root._status();
        api_root._userAIDomainList _userDomain = new api_root._userAIDomainList();
        st.code = 200;
        st.info ="success";
        _userDomain.status = st;

        try {

            ArrayList<api_root._userAIDomain> userlistdomains = new ArrayList<>();
            userlistdomains = userAIDomains.get_all_userAIDomains(devid,aiid);
            if (userlistdomains.size() <= 0) {
                st.code = 500;
                st.info = "Internal Server Error.";
            } else {
                _userDomain.userAIdomain_list = new ArrayList<api_root._userAIDomain>();
                _userDomain.userAIdomain_list = userlistdomains;
            }
        }
        catch (Exception e){
            st.code = 500;
            st.info = "Error:Internal Server Error.";
        }
        return gson.toJson(_userDomain);
    }
}