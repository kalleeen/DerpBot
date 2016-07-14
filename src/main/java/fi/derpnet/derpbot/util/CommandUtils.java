package fi.derpnet.derpbot.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandUtils {
    public static List<String> getParameters(String message) {
        if (message == null) {
            return Collections.EMPTY_LIST;
        }
        String[] split = message.split("\\s+");
        if (split.length <= 1) {
            return Collections.EMPTY_LIST;
        }
        // TODO: handle quoted parameters differently (don't break at spaces)
        return Arrays.asList(split).subList(1, split.length);
    }
}
