package walaniam.aws.signup;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateUtils {

    public static long timestampOf(String date) {
        LocalDateTime dateTime = LocalDateTime.parse(date);
        return dateTime.toInstant(OffsetDateTime.now().getOffset()).toEpochMilli();
    }

    public static String yearMonthOf(String date) {
        return Arrays.stream(date.split("-"))
                .limit(2)
                .collect(Collectors.joining("-"));
    }
}
