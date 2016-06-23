package fi.derpnet.derpbot.constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class IrcConstants {

    public static final List<Character> validChannelPrefixes = Collections.unmodifiableList(Arrays.asList('&', '#', '+', '!'));
    public static final char CTCP_CHAR = 0x1;
}
