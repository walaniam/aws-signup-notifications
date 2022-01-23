package walaniam.aws.signup;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class SignupNotificationsHandler implements RequestHandler<SQSEvent, String> {

    private static final Logger log = LoggerFactory.getLogger(SignupNotificationsHandler.class);

    private static final JsonApi JSON_API = new JsonApi();
    private static final WelcomeNotificationCompiler NOTIFICATION_COMPILER = new WelcomeNotificationCompiler(
            System.getenv("NOTIFICATION_SENDER"),
            System.getenv("NOTIFICATION_TEMPLATE")
    );

    public SignupNotificationsHandler() {
        log.info("Lambda instantiated. ENV variables: {}", JSON_API.toJson(System.getenv()));
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
}
