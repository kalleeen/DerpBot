package fi.derpnet.derpbot.util;

public class TimeUtils {

    public static final int MS_IN_S = 1000;
    public static final int MS_IN_MIN = MS_IN_S * 60;
    public static final int MS_IN_HOUR = MS_IN_MIN * 60;
    public static final int MS_IN_DAY = MS_IN_HOUR * 24;

    public static String msToTime(long ms) {
        if (ms == 0) {
            return "";
        }
        if (ms < 0) {
            ms *= -1;
        }
        StringBuilder sb = new StringBuilder();
        if (ms > MS_IN_DAY) {
            sb.append(ms / MS_IN_DAY).append("d ");
            ms %= MS_IN_DAY;
        }
        if (ms > MS_IN_HOUR) {
            sb.append(ms / MS_IN_HOUR).append("h ");
            ms %= MS_IN_HOUR;
        }
        if (ms > MS_IN_MIN) {
            sb.append(ms / MS_IN_MIN).append("min ");
            ms %= MS_IN_MIN;
        }
        if (ms > MS_IN_S) {
            sb.append(ms / MS_IN_S).append("s ");
            ms %= MS_IN_S;
        }
        if (ms > 0) {
            sb.append(ms).append("ms ");
        }
        return sb.substring(0, sb.length() - 1);
    }
}
