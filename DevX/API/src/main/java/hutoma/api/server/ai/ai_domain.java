package hutoma.api.server.ai;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hutoma.api.server.Role;
import hutoma.api.server.Secured;
import hutoma.api.server.db.domain;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
/**
 * Created by Hutoma on 15/07/16.
 */

@Path("/ai/domain")
public class ai_domain {
    @GET
    @Secured({Role.ROLE_FREE,Role.ROLE_PLAN_1,Role.ROLE_PLAN_2,Role.ROLE_PLAN_3,Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public String getDomains(
            @Context SecurityContext securityContext,
            @DefaultValue("") @HeaderParam("_developer_id") String devid) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        api_root._status st = new api_root._status();
        api_root._domainList _domain = new api_root._domainList();
        st.code = 200;
        st.info ="success";
        _domain.status = st;
        try {
            ArrayList<api_root._domain> listdomains = new ArrayList<>();
            listdomains = domain.get_all_domains();
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
        return gson.toJson(_domain);
    }
}