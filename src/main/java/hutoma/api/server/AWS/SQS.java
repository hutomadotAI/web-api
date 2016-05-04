package hutoma.api.server.AWS;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.SendMessageRequest;


public class SQS {


    public static boolean push_msg(String message) {
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {return false;}

        AmazonSQS sqs = new AmazonSQSClient(credentials);
        Region usWest2 = Region.getRegion(Regions.US_EAST_1);
        sqs.setRegion(usWest2);

        try {
//            // Create a queue
//            System.out.println("Creating a new SQS queue called MyQueue.\n");
//            CreateQueueRequest createQueueRequest = new CreateQueueRequest("MyQueue");
//            String myQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();
//
//            // List queues
//            System.out.println("Listing all queues in your account.\n");
//            for (String queueUrl : sqs.listQueues().getQueueUrls()) {
//                System.out.println("  QueueUrl: " + queueUrl);
//            }
//            System.out.println();

            // Send a message
         //   System.out.println("Sending a message to MyQueue.\n");
            sqs.sendMessage(new SendMessageRequest("https://sqs.us-east-1.amazonaws.com/622523181677/core", message));

//            // Receive messages
//            System.out.println("Receiving messages from MyQueue.\n");
//            ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest("https://sqs.us-east-1.amazonaws.com/622523181677/core");
//            List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
//            for (Message message : messages) {
//                System.out.println("  Message");
//                System.out.println("    MessageId:     " + message.getMessageId());
//                System.out.println("    ReceiptHandle: " + message.getReceiptHandle());
//                System.out.println("    MD5OfBody:     " + message.getMD5OfBody());
//                System.out.println("    Body:          " + message.getBody());
//                for (Map.Entry<String, String> entry : message.getAttributes().entrySet()) {
//                    System.out.println("  Attribute");
//                    System.out.println("    Name:  " + entry.getKey());
//                    System.out.println("    Value: " + entry.getValue());
//                }
//            }
//            System.out.println();
//
//            // Delete a message
//            System.out.println("Deleting a message.\n");
//            String messageReceiptHandle = messages.get(0).getReceiptHandle();
//            sqs.deleteMessage(new DeleteMessageRequest("https://sqs.us-east-1.amazonaws.com/622523181677/core", messageReceiptHandle));

            // Delete a queue
//            System.out.println("Deleting the test queue.\n");
//            sqs.deleteQueue(new DeleteQueueRequest(myQueueUrl));
        } catch (AmazonServiceException ase) {
           return false;
// System.out.println("Caught an AmazonServiceException, which means your request made it " +
//                    "to Amazon SQS, but was rejected with an error response for some reason.");
//            System.out.println("Error Message:    " + ase.getMessage());
//            System.out.println("HTTP Status Code: " + ase.getStatusCode());
//            System.out.println("AWS Error Code:   " + ase.getErrorCode());
//            System.out.println("Error Type:       " + ase.getErrorType());
//            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            return false;
//            System.out.println("Caught an AmazonClientException, which means the client encountered " +
//                    "a serious internal problem while trying to communicate with SQS, such as not " +
//                    "being able to access the network.");
//            System.out.println("Error Message: " + ace.getMessage());
        }
        return true;
    }
}



