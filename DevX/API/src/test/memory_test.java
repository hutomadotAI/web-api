import hutoma.api.server.ai.api_intents_and_entities;
import hutoma.api.server.db.memory;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Created by mauriziocibelli on 29/07/16.
 */
public class memory_test  extends base_test{

    @Test
    public void db_test() throws InterruptedException {


        // creates a variable
        boolean res;
        res = memory.set_variable(base_test._test_devid,base_test._test_aiid,"123",1,1,"test","test variable","this is the variable value");
        assert(res==true);

        // test if the variable has been correctly stored
        api_intents_and_entities.memory_token token = new api_intents_and_entities.memory_token();
        token = memory.get_user_variable(base_test._test_devid,base_test._test_aiid,"123","test variable");
        assert (token.variable_value.equals("this is the variable value"));

        //updates the variable
        res = memory.set_variable(base_test._test_devid,base_test._test_aiid,"123",1,1,"test","test variable","this is the variable value 2");
        assert(res==true);

        //checks if the variable has been set
        token = memory.get_user_variable(base_test._test_devid,base_test._test_aiid,"123","test variable");
        assert (token.variable_value.equals("this is the variable value 2"));

        // creates a second variable
        memory.set_variable(base_test._test_devid,base_test._test_aiid,"123",1,1,"test","another variable","value");

        // check if the number of variables is 2
        ArrayList<api_intents_and_entities.memory_token> tokens = new ArrayList<>();
        tokens = memory.get_all_user_variables(base_test._test_devid,base_test._test_aiid,"123");
        assert (tokens.size()==2);

        // removes a variable and test the number of active variables
        memory.remove_variable(base_test._test_devid,base_test._test_aiid,"123","test variable");
        tokens = memory.get_all_user_variables(base_test._test_devid,base_test._test_aiid,"123");
        assert (tokens.size()==1);

        // removes
        memory.remove_all_user_variables(base_test._test_devid,base_test._test_aiid,"123");
        tokens = memory.get_all_user_variables(base_test._test_devid,base_test._test_aiid,"123");
        assert (tokens.size()==0);


        // creates a variable that should last in memory for 5 seconds
        res = memory.set_variable(base_test._test_devid,base_test._test_aiid,"123",5,1,"test","A","A");
        assert(res==true);
        memory.purge_memory(base_test._test_devid,base_test._test_aiid,"123");
        tokens = memory.get_all_user_variables(base_test._test_devid,base_test._test_aiid,"123");
        assert (tokens.size()==1);

        int wait=5;
        while (wait>=0) { Thread.sleep(1000); wait--;}

        memory.purge_memory(base_test._test_devid,base_test._test_aiid,"123");
        tokens = memory.get_all_user_variables(base_test._test_devid,base_test._test_aiid,"123");
        assert (tokens.size()==0);






    }
}
