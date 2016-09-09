import hutoma.api.server.ai.api_intents_and_entities;
import hutoma.api.server.db.entity;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Created by mauriziocibelli on 29/07/16.
 */
public class entities extends  base_test {

    @Test
    public void db_test() throws InterruptedException {



        //creates a pizza topping entity

        boolean res = entity.create_entity(base_test._test_devid,"toppings","mozzarella||pepperoni||cheese","promptA||promptB||promptC||promptD",5,true);
        assert(res);


        // updares the previous entity
        entity.update_entity(base_test._test_devid,"toppings","mozzarella||basil","promptA",5,true);
        api_intents_and_entities._entity e = entity.get_entity(base_test._test_devid,"toppings");

        assert(e.entity_keys.length==2);
        assert(e.prompts.length==1);
        assert(e.entity_keys[1].equals("basil"));


        // creates a new entity
        res = entity.create_entity(base_test._test_devid,"crust","thin||filled","promptC||promptD",5,true);
        assert(res);


        // checks if the two entites are correctnly stored
        ArrayList<api_intents_and_entities._entity> ents = new ArrayList<>();
        ents = entity.get_dev_entities(base_test._test_devid);
        assert (ents.size()==2);



        // delete all entities
        entity.delete_entity(base_test._test_devid,"toppings");
        ents = entity.get_dev_entities(base_test._test_devid);
        assert (ents.size()==1);


        // delete all entities
        entity.delete_all_entities(base_test._test_devid);
        ents = entity.get_dev_entities(base_test._test_devid);
        assert (ents.size()==0);





    }

}
