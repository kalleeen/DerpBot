package fi.derpnet.derpbot.util;

import static fi.derpnet.derpbot.constants.HtmlFormatting.BOLD;
import static fi.derpnet.derpbot.constants.HtmlFormatting.BOLD_END;
import static fi.derpnet.derpbot.constants.HtmlFormatting.COLOR_END;
import static fi.derpnet.derpbot.constants.HtmlFormatting.ITALIC;
import static fi.derpnet.derpbot.constants.HtmlFormatting.ITALIC_END;
import static fi.derpnet.derpbot.constants.HtmlFormatting.UNDERLINE;
import static fi.derpnet.derpbot.constants.HtmlFormatting.UNDERLINE_END;

public class IrcSafeHtmlFormatter {
    public static String bold(String string) {
        return new StringBuilder().append(BOLD).append(string).append(BOLD_END).toString();
    }

    public static String italic(String string) {
        return new StringBuilder().append(ITALIC).append(string).append(ITALIC_END).toString();
    }

    public static String underline(String string) {
        return new StringBuilder().append(UNDERLINE).append(string).append(UNDERLINE_END).toString();
    }
    
    public static String colorize(String string, String foregroundColor) {
        return new StringBuilder().append(foregroundColor).append(string).append(COLOR_END).toString();
    }
}
