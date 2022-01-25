package walaniam.aws.signup.compiling;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;
import walaniam.aws.signup.model.SignupRecord;
import walaniam.aws.signup.model.WelcomeNotification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static walaniam.aws.signup.DateUtils.yearMonthOf;
import static walaniam.aws.signup.JsonApi.toJson;
import static walaniam.aws.signup.JsonApi.toPojo;

@Slf4j
public class SignupNotificationsHandler implements RequestHandler<SQSEvent, String> {

    private static final int NUM_OF_OTHER_SIGNUPS = 3;

    private static final WelcomeNotificationCompiler NOTIFICATION_COMPILER = new WelcomeNotificationCompiler(
            Objects.requireNonNull(System.getenv("NOTIFICATION_SENDER")),
            Objects.requireNonNull(System.getenv("NOTIFICATION_TEMPLATE"))
    );

    private final Storage storage = new Storage();
    private final SqsClient sqsClient;
    private final String targetQueueUrl;

    public SignupNotificationsHandler() {
        log.info("SignupNotificationsHandler instantiated. ENV variables: {}", toJson(System.getenv()));
        this.sqsClient = SqsClient.builder()
                .region(Region.EU_WEST_1)
                .build();
        this.targetQueueUrl = Objects.requireNonNull(System.getenv("NOTIFICATION_TARGET_QUEUE"));
    }

    @Override
    public String handleRequest(SQSEvent sqsEvent, Context context) {

        List<SignupRecord> signups = mapRecords(sqsEvent, context);
        List<SignupRecord> permutations = findForPermutations(signups);

        signups.forEach(signup -> {
            Collections.shuffle(permutations);
            List<SignupRecord> otherSignups = permutations.stream()
                    .filter(record -> record.getId() != signup.getId())
                    .limit(NUM_OF_OTHER_SIGNUPS)
                    .collect(Collectors.toList());

            WelcomeNotification welcomeNotification = NOTIFICATION_COMPILER.compile(signup, otherSignups);
            log.info("Welcome: {}", welcomeNotification);

            sendNotificationMessage(welcomeNotification);
        });

        storage.save(signups);

        log.info("Completed, awsRequestId={}", context.getAwsRequestId());
        return "200 OK";
    }

    private List<SignupRecord> findForPermutations(List<SignupRecord> signups) {

        if (signups.size() >= NUM_OF_OTHER_SIGNUPS + 1) {
            // enough to send
            return signups;
        }

        String createdAtYearMonth = yearMonthOf(signups.get(0).getCreatedAt());
        List<SignupRecord> previousSignups = storage.queryByYearMonthCreated(createdAtYearMonth);

        List<SignupRecord> result = new ArrayList<>();
        result.addAll(signups);
        result.addAll(previousSignups);

        log.info("Total for permutations: {}", result.size());

        return result;
    }

    private List<SignupRecord> mapRecords(SQSEvent sqsEvent, Context context) {
        List<SQSEvent.SQSMessage> records = sqsEvent.getRecords();
        log.info("Handling request, eventsCount={}, awsRequestId={}", records.size(), context.getAwsRequestId());

        List<SignupRecord> signups = records.stream()
                .map(SQSEvent.SQSMessage::getBody)
                .map(body -> toPojo(body, SignupRecord.class))
                .collect(Collectors.toList());

        log.debug("Records: {}", signups);
        return signups;
    }

    private void sendNotificationMessage(WelcomeNotification notification) {

        long receiverId = notification.getReceiver();

        log.info("Sending notification message for receiver={}", receiverId);

        String jsonBody = toJson(notification);

        SendMessageRequest request = SendMessageRequest.builder()
                .queueUrl(targetQueueUrl)
                .messageBody(jsonBody)
                .messageGroupId(String.valueOf(receiverId))
                .messageDeduplicationId(String.valueOf(receiverId))
                .build();

        SendMessageResponse messageResponse = sqsClient.sendMessage(request);

        log.info("Message response: {}", messageResponse);
    }
}
