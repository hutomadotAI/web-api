package hutoma.api.server.AWS;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.SendMessageRequest;


public class SQS {


    public static boolean push_msg(String queue, String message) {
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {return false;}

        AmazonSQS sqs = new AmazonSQSClient(credentials);
        Region usWest2 = Region.getRegion(Regions.US_EAST_1);
        sqs.setRegion(usWest2);

        try {
            sqs.sendMessage(new SendMessageRequest(queue, message));

        }  catch (Exception e) {
            System.out.print(e.getMessage());
           return  false;
        }

        return true;
    }
}



