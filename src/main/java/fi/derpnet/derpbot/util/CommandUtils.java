package fi.derpnet.derpbot.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandUtils {

    /**
     * Gets a list of parameters supplied with a command. This basically splits
     * the input message at spaces and ignores the first entry (which is the
     * command itself)
     *
     * @param message message received, including the command
     * @return List of parameters
     */
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

    /**
     * Convenience method for getting the first parameter after the command
     *
     * @param message received, including the command
     * @return The first parameter, or null if there were no parameters
     */
    public static String getFirstParameter(String message) {
        return getParameters(message).iterator().next();
    }
}
