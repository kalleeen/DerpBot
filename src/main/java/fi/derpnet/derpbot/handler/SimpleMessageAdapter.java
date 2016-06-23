package fi.derpnet.derpbot.handler;

import fi.derpnet.derpbot.bean.RawMessage;
import fi.derpnet.derpbot.connector.IrcConnector;
import fi.derpnet.derpbot.controller.MainController;
import fi.derpnet.derpbot.util.RawMessageUtils;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Adapter for using a SimpleMessageHandler in place of a RawMessageHandler
 */
public class SimpleMessageAdapter implements RawMessageHandler {

    private final SimpleMessageHandler handler;

    public SimpleMessageAdapter(SimpleMessageHandler handler) {
        this.handler = handler;
    }

    @Override
    public List<RawMessage> handle(RawMessage message, IrcConnector ircConnector) {
        if (message.command.equals("PRIVMSG")) {
            String incomingRecipient = message.parameters.get(0);
            String messageBody = message.parameters.get(1);
            String responseBody = handler.handle(message.prefix, incomingRecipient, messageBody, ircConnector);
            if (responseBody == null) {
                return null;
            }
            String responseRecipient = RawMessageUtils.getRecipientForResponse(message);
            responseBody = ':' + responseBody;
            return Collections.singletonList(new RawMessage(null, "PRIVMSG", Arrays.asList(responseRecipient, responseBody)));
        } else {
            return null;
        }
    }

    @Override
    public void init(MainController controller) {
        handler.init(controller);
    }

    @Override
    public String getCommand() {
        return handler.getCommand();
    }

    @Override
    public String getHelp() {
        return handler.getHelp();
    }

}
