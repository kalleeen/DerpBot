package fi.derpnet.derpbot.handler;

import fi.derpnet.derpbot.adapter.SimpleMessageAdapter;
import fi.derpnet.derpbot.connector.IrcConnector;

/**
 * A handler that handles regular messages (PRIVMSG)
 */
public interface SimpleMessageHandler extends GenericHandler {

    /**
     * Handles an incoming message
     *
     * @param sender the sender of this message in format nick!user@host
     * @param recipient the recipient (channel or this bot itself)
     * @param message the message received
     * @param ircConnector the IrcConnector requesting this handling. Useful for
     * retrieving the bots nickname or other network-specific info
     * @return the response to be sent to the origin (channel or sender), or
     * null if this handler does not handle this message
     */
    String handle(String sender, String recipient, String message, IrcConnector ircConnector);

    @Override
    default RawMessageHandler getRawMessageHandler() {
        return new SimpleMessageAdapter(this);
    }
}
