package walaniam.aws.signup.notifications;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import lombok.extern.slf4j.Slf4j;
import walaniam.aws.signup.JsonApi;

@Slf4j
public class WelcomeNotificationsHandler implements RequestHandler<SQSEvent, String> {

    private static final JsonApi JSON_API = new JsonApi();

    public WelcomeNotificationsHandler() {
        log.info("WelcomeNotificationsHandler instantiated. ENV variables: {}", JSON_API.toJson(System.getenv()));
    }

    @Override
    public String handleRequest(SQSEvent input, Context context) {

        return "200 OK";
    }
}
