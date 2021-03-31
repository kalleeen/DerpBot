package fi.derpnet.derpbot.handler;

import fi.derpnet.derpbot.bean.Message;
import fi.derpnet.derpbot.bean.RawMessage;
import fi.derpnet.derpbot.connector.Connector;
import java.util.List;

/**
 * A handler that handles raw messages from the server
 */
public interface MessageHandler extends GenericHandler {

    /**
     * Handles an incoming message
     *
     * @param message the message received
     * @param connector the Connector requesting this handling. Useful for
     * retrieving the bots nickname or other network-specific info
     * @return the response(s) to be sent to the server, or null if this handler
     * does not handle this message
     */
    List<Message> handle(Message message, Connector connector);

    @Override
    default MessageHandler getRawMessageHandler() {
        return this;
    }
}
