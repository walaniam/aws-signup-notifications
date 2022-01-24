package walaniam.aws.signup.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WelcomeNotification {
    private String sender;
    private long receiver;
    private String message;
    private Collection<Long> recentUserIds;
}
