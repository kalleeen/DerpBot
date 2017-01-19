package fi.derpnet.derpbot.handler;

import fi.derpnet.derpbot.connector.IrcConnector;

/**
 * A handler that handles regular messages (PRIVMSG) and sends responses to the
 * channel unless the channel is a quieter channel, in which case the response
 * is sent to the sender privately
 */
public interface LoudMessageHandler extends GenericHandler {

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
}
