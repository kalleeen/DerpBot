package fi.derpnet.derpbot.constants;

public class AsciiFormatting {

    public static final char BOLD = 0x2;
    public static final char COLOR = 0x3;
    public static final char ITALIC = 0x1D;
    public static final char UNDERLINE = 0x1F;
    public static final char INVERT = 0x16;
    public static final char RESET_FORMATTING = 0xF;

    public static final int WHITE = 0;
    public static final int BLACK = 1;
    public static final int BLUE = 2;
    public static final int GREEN = 3;
    public static final int RED = 4;
    public static final int BROWN = 5;
    public static final int PURPLE = 6;
    public static final int ORANGE = 7;
    public static final int YELLOW = 8;
    public static final int LIGHT_GREEN = 9;
    public static final int TEAL = 10;
    public static final int LIGHT_CYAN = 11;
    public static final int LIGHT_BLUE = 12;
    public static final int PINK = 13;
    public static final int GREY = 14;
    public static final int LIGHT_GREY = 15;
    public static final int TRANSPARENT = 99;

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
