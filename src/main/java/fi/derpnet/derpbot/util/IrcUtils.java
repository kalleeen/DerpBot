package fi.derpnet.derpbot.util;

public class IrcUtils {

    /**
     * Gets the nick from a sender formatted as "nick!user@host"
     *
     * @param sender
     * @return nick
     */
    public static String getNickFromSender(String sender) {
        if (sender != null) {
            return sender.split("!", 2)[0];
        }
        return null;
    }
}
