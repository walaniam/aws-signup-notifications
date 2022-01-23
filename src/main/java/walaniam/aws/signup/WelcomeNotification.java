package walaniam.aws.signup;

import lombok.Data;

import java.util.Collection;

@Data
public class WelcomeNotification {
    private String sender;
    private long receiver;
    private String message;
    private Collection<Long> recentUserIds;
}
