package skyblocksquad.dcbot.util;

import java.time.format.DateTimeFormatter;

public class TimeUtils {

    public static final DateTimeFormatter HH_MM_SS_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter DD_MM_YYY_HH_MM_SS = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

}
