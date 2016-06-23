package fi.derpnet.derpbot.util;

import fi.derpnet.derpbot.bean.RawMessage;
import fi.derpnet.derpbot.constants.IrcConstants;

public class RawMessageUtils {

    public static String getRecipientForResponse(RawMessage src) {
        if (src.command.equals("PRIVMSG") || src.command.equals("NOTICE")) {
            if (IrcConstants.validChannelPrefixes.contains(src.parameters.get(0).charAt(0))) {
                // message was sent to a channel, reply there
                return src.parameters.get(0);
            } else {
                // private message, reply to sender
                return src.prefix.split("!")[0];
            }
        } else {
            return null;
        }
    }
}
