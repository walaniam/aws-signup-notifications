package walaniam.aws.signup.compiling;

import org.junit.jupiter.api.Test;
import walaniam.aws.signup.model.SignupRecord;
import walaniam.aws.signup.model.WelcomeNotification;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static walaniam.aws.signup.JsonApi.toJson;

class WelcomeNotificationCompilerTest {

    @Test
    void compileWithRecentJoiners() {
        // given
        WelcomeNotificationCompiler underTest = new WelcomeNotificationCompiler(
                "me@somewhere.com",
                "SomeCompany"
        );

        SignupRecord receiver = SignupRecord.builder()
                .id(100)
                .name("Jane")
                .build();

        List<SignupRecord> otherJoiners = Arrays.asList(
                SignupRecord.builder().id(10).name("Lisa").build(),
                SignupRecord.builder().id(12).name("John").build(),
                SignupRecord.builder().id(13).name("Stephen").build()
        );

        // when
        WelcomeNotification notification = underTest.compile(receiver, otherJoiners);
        String json = toJson(notification);

        // then
        assertThat(notification.getReceiver()).isEqualTo(100);
        assertThat(notification.getRecentUserIds()).containsExactlyInAnyOrder(10l, 12l, 13l);
        assertThat(notification.getMessage())
                .isEqualTo("Hi Jane, welcome to SomeCompany. Lisa, John, Stephen also joined recently.");
        assertThat(json.contains("\"recent_user_ids\"")).isTrue();
    }

    @Test
    void compileWithNoRecentJoiners() {
        // given
        WelcomeNotificationCompiler underTest = new WelcomeNotificationCompiler(
                "me@somewhere.com",
                "SomeCompany"
        );

        SignupRecord receiver = SignupRecord.builder()
                .id(100)
                .name("Jane")
                .build();

        List<SignupRecord> otherJoiners = Collections.emptyList();

        // when
        WelcomeNotification notification = underTest.compile(receiver, otherJoiners);
        String json = toJson(notification);

        // then
        assertThat(notification.getReceiver()).isEqualTo(100);
        assertThat(notification.getRecentUserIds()).isEmpty();
        assertThat(notification.getMessage())
                .isEqualTo("Hi Jane, welcome to SomeCompany.");
        assertThat(json.contains("\"recent_user_ids\"")).isTrue();
    }
}