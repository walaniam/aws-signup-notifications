package walaniam.aws.signup;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.lambda.LambdaAsyncClient;
import software.amazon.awssdk.services.lambda.model.GetAccountSettingsRequest;
import software.amazon.awssdk.services.lambda.model.GetAccountSettingsResponse;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SignupNotificationsHandler implements RequestHandler<SQSEvent, String> {

    private static final Logger log = LoggerFactory.getLogger(SignupNotificationsHandler.class);

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final LambdaAsyncClient LAMBDA_CLIENT = LambdaAsyncClient.create();

    public SignupNotificationsHandler() {
        Optional<GetAccountSettingsResponse> settings = getAccountSettings();
        log.info("ACCOUNT SETTINGS: {}", settings.map(GetAccountSettingsResponse::toString).orElse("empty"));
        log.info("ENVIRONMENT VARIABLES: {}", GSON.toJson(System.getenv()));
    }

    @Override
    public String handleRequest(SQSEvent sqsEvent, Context context) {

        log.info("Handling request, eventsCount={}, awsRequestId={}", sqsEvent.getRecords().size(), context.getAwsRequestId());

        sqsEvent.getRecords().forEach(record -> log.info("Record: {}", record.getBody()));

        log.info("Completed, awsRequestId={}", context.getAwsRequestId());

        return "200 OK";
    }

    private Optional<GetAccountSettingsResponse> getAccountSettings() {
        CompletableFuture<GetAccountSettingsResponse> future = LAMBDA_CLIENT.getAccountSettings(
                GetAccountSettingsRequest.builder().build()
        );
        try {
            return Optional.ofNullable(future.get());
        } catch (Exception e) {
            log.error("Could not get account settings", e);
        }
        return Optional.empty();
    }
}
