package skyblocksquad.dcbot.util;

import java.time.format.DateTimeFormatter;

public class TimeUtils {

    private static final DateTimeFormatter loggingTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter dcChatConnectionDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public static DateTimeFormatter getLoggingTimeFormatter() {
        return loggingTimeFormatter;
    }

    public static DateTimeFormatter getDcLoggingFormatter() {
        return dcChatConnectionDateFormatter;
    }

}
