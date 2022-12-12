package fi.derpnet.derpbot.handler;

import fi.derpnet.derpbot.adapter.AdvancedMessageAdapter;
import fi.derpnet.derpbot.bean.MatrixMessage;
import fi.derpnet.derpbot.connector.Connector;

/**
 * A handler that handles regular messages (PRIVMSG)
 */
public interface AdvancedMessageHandler extends GenericHandler {

    /**
     * Handles an incoming message
     *
     * @param sender the sender of this message in format nick!user@host
     * @param recipient the recipient (channel or this bot itself)
     * @param message the message received
     * @param connector the Connector requesting this handling. Useful for
     * retrieving the bots nickname or other network-specific info
     * @return the response to be sent to the origin (channel or sender), or
     * null if this handler does not handle this message
     */
    MatrixMessage handle(String sender, String recipient, String message, Connector connector);

    @Override
    default MessageHandler getRawMessageHandler() {
        return new AdvancedMessageAdapter(this);
    }
}
