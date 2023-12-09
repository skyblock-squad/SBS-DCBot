package skyblocksquad.dcbot.util;

import java.time.LocalDateTime;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LoggingFormat extends Formatter {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_WHITE = "\u001B[1m";

    @Override
    public String format(LogRecord record) {
        return switch (record.getLevel().toString()) {
            case "INFO" ->
                    ANSI_WHITE + "[" + record.getLoggerName() + "] [" + LocalDateTime.now().format(TimeUtils.getLoggingTimeFormatter()) + " INFO]: " + formatMessage(record) + ANSI_RESET + "\n";
            case "WARNING" ->
                    ANSI_YELLOW + "[" + record.getLoggerName() + "] [" + LocalDateTime.now().format(TimeUtils.getLoggingTimeFormatter()) + " WARNING]: " + formatMessage(record) + ANSI_RESET +  "\n";
            case "SEVERE" ->
                    ANSI_RED +"[" + record.getLoggerName() + "] [" + LocalDateTime.now().format(TimeUtils.getLoggingTimeFormatter()) + " SEVERE]: " + formatMessage(record) + ANSI_RESET +  "\n";
            default ->
                    "[" + record.getLoggerName() + "] [" + LocalDateTime.now().format(TimeUtils.getLoggingTimeFormatter()) + " " + record.getLevel() + "]: " + formatMessage(record) + ANSI_RESET +  "\n";
        };
    }

}
