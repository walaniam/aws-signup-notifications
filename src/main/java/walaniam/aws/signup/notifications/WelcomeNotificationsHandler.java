package walaniam.aws.signup.notifications;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import walaniam.aws.signup.model.WelcomeNotification;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static walaniam.aws.signup.JsonApi.toJson;
import static walaniam.aws.signup.JsonApi.toPojo;

@Slf4j
public class WelcomeNotificationsHandler implements RequestHandler<SQSEvent, String> {

    private static final int RETRIES = 2;
    private static final long BACKOFF_MILLIS = 2000;

    private static final MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");
    private static final OkHttpClient OK_HTTP = new OkHttpClient();

    private final String notificationsRestEndpoint;

    public WelcomeNotificationsHandler() {
        log.info("WelcomeNotificationsHandler instantiated. ENV variables: {}", toJson(System.getenv()));
        this.notificationsRestEndpoint = Objects.requireNonNull(System.getenv("NOTIFICATION_REST"));
    }

    @Override
    public String handleRequest(SQSEvent sqsEvent, Context context) {

        List<WelcomeNotification> welcomes = mapRecords(sqsEvent, context);
        log.info("Welcome notifications count={}", welcomes.size());

        welcomes.forEach(this::postWithRetry);

        return "200 OK";
    }

    private void postWithRetry(WelcomeNotification notification) {
        for (int i = 0; i <= RETRIES; i++) {
            try {
                post(notification);
                break;
            } catch (Exception e) {
                log.warn("POST failed in attempt=" + i, e);
                if (i == RETRIES) {
                    throw new RuntimeException("Failed in retries", e);
                }
                if (backoff(BACKOFF_MILLIS)) {
                    throw new RuntimeException("Backoff interrupted", e);
                }
            }
        }
    }

    private void post(WelcomeNotification notification) {

        log.info("Sending welcome notification for receiver={}", notification.getReceiver());

        String payload = toJson(notification);

        Request request = new Request.Builder()
                .url(notificationsRestEndpoint)
                .post(RequestBody.create(payload, JSON_TYPE))
                .build();

        try (Response response = OK_HTTP.newCall(request).execute()) {
            int status = response.code();
            log.info("status={}, body={}", status, bodyOf(response));
            if (!response.isSuccessful()) {
                throw new RuntimeException(
                        String.format("POST failed for id=%s with status=%s", notification.getReceiver(), status)
                );
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String bodyOf(Response response) {
        ResponseBody body = response.body();
        try {
            return (body != null) ? body.string() : "";
        } catch (IOException e) {
            log.warn("Get body failed", e);
            return "unknown";
        }
    }

    private List<WelcomeNotification> mapRecords(SQSEvent sqsEvent, Context context) {
        List<SQSEvent.SQSMessage> records = sqsEvent.getRecords();
        log.info("Handling request, eventsCount={}, awsRequestId={}", records.size(), context.getAwsRequestId());

        List<WelcomeNotification> notifications = Collections.emptyList();
        try {
            notifications = records.stream()
                    .map(SQSEvent.SQSMessage::getBody)
                    .map(body -> toPojo(body, WelcomeNotification.class))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Could not parse records", e);
        }

        log.debug("Records: {}", notifications);
        return notifications;
    }

    /**
     *
     * @param backoffMillis
     * @return true if interrupted
     */
    private static boolean backoff(long backoffMillis) {
        try {
            Thread.sleep(backoffMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return true;
        }
        return false;
    }
}
