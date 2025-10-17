package de.skyblocksquad.dcbot.util;

public class StringUtils {

    public static String capitalizeFirst(String input) {
        return Character.toUpperCase(input.charAt(0)) + input.substring(1).toLowerCase();
    }

    private StringUtils() {}

}