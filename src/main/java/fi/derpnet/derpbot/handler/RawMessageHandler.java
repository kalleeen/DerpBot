package fi.derpnet.derpbot.handler;

import fi.derpnet.derpbot.bean.RawMessage;
import java.util.List;

/**
 * A handler that handles raw messages from the server
 */
public interface RawMessageHandler extends GenericHandler {

    /**
     * Handles an incoming message
     *
     * @param message the message received
     * @return the response(s) to be sent to the server, or null if this handler
     * does not handle this message
     */
    List<RawMessage> handle(RawMessage message);
}
