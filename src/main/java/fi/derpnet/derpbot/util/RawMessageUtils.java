package fi.derpnet.derpbot.util;

import fi.derpnet.derpbot.bean.RawMessage;
import fi.derpnet.derpbot.constants.IrcConstants;

public class RawMessageUtils {

    public static boolean privMsgSentToChannel(RawMessage msg) {
        return (msg.command.equals("PRIVMSG") || msg.command.equals("NOTICE"))
                && IrcConstants.validChannelPrefixes.contains(msg.parameters.get(0).charAt(0));
    }
    
    public static String privMsgSender(RawMessage msg) {
        return msg.prefix.split("!")[0];
    }

    public static String getRecipientForResponse(RawMessage msg) {
        if (msg.command.equals("PRIVMSG") || msg.command.equals("NOTICE")) {
            if (privMsgSentToChannel(msg)) {
                // message was sent to a channel, reply there
                return msg.parameters.get(0);
            } else {
                // private message, reply to sender
                return privMsgSender(msg);
            }
        } else {
            return null;
        }
    }

    public static String getRecipientForResponse(String sender, String recipient) {
        if (IrcConstants.validChannelPrefixes.contains(recipient.charAt(0))) {
            // message was sent to a channel, reply there
            return recipient;
        } else {
            // private message, reply to sender
            return sender;
        }
    }
}
