package walaniam.aws.signup.compiling;

import lombok.RequiredArgsConstructor;
import walaniam.aws.signup.model.SignupRecord;
import walaniam.aws.signup.model.WelcomeNotification;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class WelcomeNotificationCompiler {

    private final String senderEmail;
    private final String joinedOrganization;

    public WelcomeNotification compile(SignupRecord receiver, List<SignupRecord> recentJoiners) {
        return WelcomeNotification.builder()
                .sender(senderEmail)
                .message(messageOf(receiver.getName(), recentJoiners))
                .receiver(receiver.getId())
                .recentUserIds(recentJoiners.stream().map(SignupRecord::getId).collect(Collectors.toList()))
                .build();
    }

    private String messageOf(String receiver, List<SignupRecord> recentJoiners) {
        String hiMessage = String.format("Hi %s, welcome to %s.", receiver, joinedOrganization);
        if (!recentJoiners.isEmpty()) {
            String otherJoiners = String.format(
                    " %s also joined recently.",
                    recentJoiners.stream().map(SignupRecord::getName).collect(Collectors.joining(", "))
            );
            hiMessage += otherJoiners;
        }
        return hiMessage;
    }
}
