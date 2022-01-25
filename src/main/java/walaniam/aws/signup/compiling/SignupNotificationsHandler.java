package walaniam.aws.signup.compiling;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import lombok.extern.slf4j.Slf4j;
import walaniam.aws.signup.model.SignupRecord;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static walaniam.aws.signup.JsonApi.toJson;
import static walaniam.aws.signup.JsonApi.toPojo;

@Slf4j
public class SignupNotificationsHandler implements RequestHandler<SQSEvent, String> {

    private static final WelcomeNotificationCompiler NOTIFICATION_COMPILER = new WelcomeNotificationCompiler(
            Objects.requireNonNull(System.getenv("NOTIFICATION_SENDER")),
            Objects.requireNonNull(System.getenv("NOTIFICATION_TEMPLATE"))
    );

    private final Storage storage = new Storage();
//    private final SqsClient sqsClient;
//    private final String targetQueueUrl;

    public SignupNotificationsHandler() {
        log.info("SignupNotificationsHandler instantiated. ENV variables: {}", toJson(System.getenv()));
//        this.sqsClient = SqsClient.builder()
//                .region(Region.EU_WEST_1)
//                .build();
//        this.targetQueueUrl = Objects.requireNonNull(System.getenv("NOTIFICATION_TARGET_QUEUE"));
    }

    @Override
    public String handleRequest(SQSEvent sqsEvent, Context context) {

        List<SQSEvent.SQSMessage> records = sqsEvent.getRecords();
        log.info("Handling request, eventsCount={}, awsRequestId={}", records.size(), context.getAwsRequestId());

        List<SignupRecord> signups = records.stream()
                .map(SQSEvent.SQSMessage::getBody)
//                .peek(body -> log.debug("Body={}", body))
                .map(body -> toPojo(body, SignupRecord.class))
                .collect(Collectors.toList());

        log.info("Records: {}", signups);

        storage.save(signups);

        log.info("Completed, awsRequestId={}", context.getAwsRequestId());

        return "200 OK";
    }

//    private void sendNotificationMessage(WelcomeNotification notification) {
//
//        long receiverId = notification.getReceiver();
//
//        log.info("Sending notification message for receiver={}", receiverId);
//
//        String jsonBody = JSON_API.toJson(notification);
//
//        SendMessageRequest request = SendMessageRequest.builder()
//                .queueUrl(targetQueueUrl)
//                .messageBody(jsonBody)
//                .messageDeduplicationId(String.valueOf(receiverId))
//                .build();
//
//        sqsClient.sendMessage(request);
//    }
}
