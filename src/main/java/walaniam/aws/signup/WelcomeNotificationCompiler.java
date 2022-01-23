package walaniam.aws.signup;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class WelcomeNotificationCompiler {

    private final String senderEmail;
    private final String messageTemplate;

    public WelcomeNotification compile(SignupRecord receiver, List<SignupRecord> recentJoiners) {
        return WelcomeNotification.builder()
                .sender(senderEmail)
                .message(messageOf(
                        receiver.getName(),
                        recentJoiners.stream().map(SignupRecord::getName).collect(Collectors.joining(", "))
                ))
                .receiver(receiver.getId())
                .recentUserIds(recentJoiners.stream().map(SignupRecord::getId).collect(Collectors.toList()))
                .build();
    }

    private String messageOf(String receiver, String recentJoiners) {
        return String.format(messageTemplate, receiver, recentJoiners);
    }
}
