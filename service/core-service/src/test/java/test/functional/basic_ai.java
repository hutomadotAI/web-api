package test.functional;

import org.junit.Test;



/**
 * Created by mauriziocibelli on 22/06/16.
 */
public class basic_ai extends base {


    @Test
    public void test_basic_ai() throws InterruptedException {

        //create an AI
        String aiid = super.createAI();
        assert (!aiid.isEmpty());

        //upload training set
        assert (super.uploadTrainigFile(aiid).equals("200"));

        //check that training is done
        assert (super.testTrainingStatus(aiid));

        // send a random chat string
        assert(super.chat(aiid, "Good morning", "0", "", "",false).equals("hey how are you?\n"));

        //checks if the RNN is training
        assert(super.getRNNError(aiid));

        // force an RNN only chat
        // assert(!super.chat(aiid, "Good morning", "0", "", "",true).isEmpty());

    }
}





