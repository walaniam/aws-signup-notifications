package walaniam.aws.signup.notifications;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import lombok.extern.slf4j.Slf4j;

import static walaniam.aws.signup.JsonApi.toJson;

@Slf4j
public class WelcomeNotificationsHandler implements RequestHandler<SQSEvent, String> {

    public WelcomeNotificationsHandler() {
        log.info("WelcomeNotificationsHandler instantiated. ENV variables: {}", toJson(System.getenv()));
    }

    @Override
    public String handleRequest(SQSEvent input, Context context) {

        return "200 OK";
    }
}
