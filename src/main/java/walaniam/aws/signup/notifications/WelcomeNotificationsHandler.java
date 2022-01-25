package walaniam.aws.signup.notifications;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import lombok.extern.slf4j.Slf4j;
import walaniam.aws.signup.model.WelcomeNotification;

import java.util.List;
import java.util.stream.Collectors;

import static walaniam.aws.signup.JsonApi.toJson;
import static walaniam.aws.signup.JsonApi.toPojo;

@Slf4j
public class WelcomeNotificationsHandler implements RequestHandler<SQSEvent, String> {

    public WelcomeNotificationsHandler() {
        log.info("WelcomeNotificationsHandler instantiated. ENV variables: {}", toJson(System.getenv()));
    }

    @Override
    public String handleRequest(SQSEvent sqsEvent, Context context) {

        List<WelcomeNotification> signups = mapRecords(sqsEvent, context);

        return "200 OK";
    }

    private List<WelcomeNotification> mapRecords(SQSEvent sqsEvent, Context context) {
        List<SQSEvent.SQSMessage> records = sqsEvent.getRecords();
        log.info("Handling request, eventsCount={}, awsRequestId={}", records.size(), context.getAwsRequestId());

        List<WelcomeNotification> notifications = records.stream()
                .map(SQSEvent.SQSMessage::getBody)
                .map(body -> toPojo(body, WelcomeNotification.class))
                .collect(Collectors.toList());

        log.debug("Records: {}", notifications);
        return notifications;
    }
}
