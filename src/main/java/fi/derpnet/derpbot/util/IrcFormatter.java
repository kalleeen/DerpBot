package fi.derpnet.derpbot.util;

import static fi.derpnet.derpbot.constants.AsciiFormatting.*;

public class IrcFormatter {

    public static String bold(String string) {
        return new StringBuilder().append(BOLD).append(string).append(BOLD).toString();
    }

    public static String italic(String string) {
        return new StringBuilder().append(ITALIC).append(string).append(ITALIC).toString();
    }

    public static String underline(String string) {
        return new StringBuilder().append(UNDERLINE).append(string).append(UNDERLINE).toString();
    }

    public static String invert(String string) {
        return new StringBuilder().append(INVERT).append(string).append(INVERT).toString();
    }

    public static String colorize(String string, int foregroundColor) {
        return colorize(string, foregroundColor, TRANSPARENT);
    }

    public static String colorize(String string, int foregroundColor, int backgroundColor) {
        return new StringBuilder().append(COLOR).append(foregroundColor).append(',').append(backgroundColor).append(string).append(COLOR).toString();
    }
}
