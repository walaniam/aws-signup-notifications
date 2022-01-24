package walaniam.aws.signup.compiling;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import walaniam.aws.signup.JsonApi;
import walaniam.aws.signup.model.SignupRecord;
import walaniam.aws.signup.model.WelcomeNotification;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class SignupNotificationsHandler implements RequestHandler<SQSEvent, String> {

    private static final JsonApi JSON_API = new JsonApi();
    private static final WelcomeNotificationCompiler NOTIFICATION_COMPILER = new WelcomeNotificationCompiler(
            Objects.requireNonNull(System.getenv("NOTIFICATION_SENDER")),
            Objects.requireNonNull(System.getenv("NOTIFICATION_TEMPLATE"))
    );

    private final SqsClient sqsClient;
    private final String targetQueueUrl;

    public SignupNotificationsHandler() {
        log.info("SignupNotificationsHandler instantiated. ENV variables: {}", JSON_API.toJson(System.getenv()));
        this.sqsClient = SqsClient.builder()
                .region(Region.EU_WEST_1)
                .build();
        this.targetQueueUrl = Objects.requireNonNull(System.getenv("NOTIFICATION_TARGET_QUEUE"));
    }

    @Override
    public String handleRequest(SQSEvent sqsEvent, Context context) {

        log.info("Handling request, eventsCount={}, awsRequestId={}", sqsEvent.getRecords().size(), context.getAwsRequestId());

        List<SignupRecord> records = sqsEvent.getRecords().stream()
                .map(SQSEvent.SQSMessage::getBody)
                .peek(body -> log.info("Body={}", body))
                .map(body -> JSON_API.toPojo(body, SignupRecord.class))
                .collect(Collectors.toList());

        log.info("Records: {}", records);
        log.info("Completed, awsRequestId={}", context.getAwsRequestId());

        return "200 OK";
    }

    private void sendNotificationMessage(WelcomeNotification notification) {

        log.info("Sending notification message for receiver={}", notification.getReceiver());

        String jsonBody = JSON_API.toJson(notification);

        SendMessageRequest request = SendMessageRequest.builder()
                .queueUrl(targetQueueUrl)
                .messageBody(jsonBody)
                .messageDeduplicationId(String.valueOf(notification.getReceiver()))
                .build();

        sqsClient.sendMessage(request);
    }
}
