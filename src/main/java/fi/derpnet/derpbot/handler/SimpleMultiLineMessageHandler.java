package fi.derpnet.derpbot.handler;

import fi.derpnet.derpbot.adapter.SimpleMultiLineMessageAdapter;
import fi.derpnet.derpbot.connector.Connector;
import java.util.List;

/**
 * A handler that handles regular messages (PRIVMSG)
 */
public interface SimpleMultiLineMessageHandler extends GenericHandler {

    /**
     * Handles an incoming message
     *
     * @param sender the sender of this message in format nick!user@host
     * @param recipient the recipient (channel or this bot itself)
     * @param message the message received
     * @param connector the Connector requesting this handling. Useful for
     * retrieving the bots nickname or other network-specific info
     * @return the responses to be sent to the origin (channel or sender), or
     * null if this handler does not handle this message
     */
    List<String> handle(String sender, String recipient, String message, Connector connector);

    @Override
    default MessageHandler getRawMessageHandler() {
        return new SimpleMultiLineMessageAdapter(this);
    }
}
