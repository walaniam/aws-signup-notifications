package walaniam.aws.signup;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignupNotificationsHandler implements RequestHandler<SQSEvent, String> {

    private static final Logger log = LoggerFactory.getLogger(SignupNotificationsHandler.class);

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public SignupNotificationsHandler() {
        log.info("Lambda instantiated. ENV variables: {}", GSON.toJson(System.getenv()));
    }

    @Override
    public String handleRequest(SQSEvent sqsEvent, Context context) {

        log.info("Handling request, eventsCount={}, awsRequestId={}", sqsEvent.getRecords().size(), context.getAwsRequestId());

        sqsEvent.getRecords().forEach(record -> log.info("Record: {}", record.getBody()));

        log.info("Completed, awsRequestId={}", context.getAwsRequestId());

        return "200 OK";
    }
}
