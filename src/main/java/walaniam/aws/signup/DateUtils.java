package walaniam.aws.signup;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateUtils {

    public static long timestampOf(String date) {
        LocalDateTime dateTime = LocalDateTime.parse(date);
        return dateTime.toInstant(OffsetDateTime.now().getOffset()).toEpochMilli();
    }
}
