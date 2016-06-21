package fi.derpnet.derpbot.handler;

import fi.derpnet.derpbot.bean.RawMessage;
import fi.derpnet.derpbot.connector.IrcConnector;
import fi.derpnet.derpbot.constants.IrcConstants;
import fi.derpnet.derpbot.controller.MainController;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapter for using a SimpleMultiLineMessageHandler in place of a
 * RawMessageHandler
 */
public class SimpleMultiLineMessageAdapter implements RawMessageHandler {

    private final SimpleMultiLineMessageHandler handler;

    public SimpleMultiLineMessageAdapter(SimpleMultiLineMessageHandler handler) {
        this.handler = handler;
    }

    @Override
    public List<RawMessage> handle(RawMessage message, IrcConnector ircConnector) {
        if (message.command.equals("PRIVMSG")) {
            String incomingRecipient = message.parameters.get(0);
            String messageBody = message.parameters.get(1);
            List<String> responseBodies = handler.handle(message.prefix, incomingRecipient, messageBody, ircConnector);
            if (responseBodies == null) {
                return null;
            }
            String responseRecipient;
            if (IrcConstants.validChannelPrefixes.contains(incomingRecipient.charAt(0))) {
                // message was sent to a channel, reply there
                responseRecipient = message.parameters.get(0);
            } else {
                // private message, reply to sender
                responseRecipient = message.prefix.split("!")[0];
            }
            return responseBodies.stream().map(msg -> new RawMessage(null, "PRIVMSG", Arrays.asList(responseRecipient, ':' + msg))).collect(Collectors.toList());
        } else {
            return null;
        }
    }

    @Override
    public void init(MainController controller) {
        handler.init(controller);
    }

    @Override
    public String getHelp() {
        return handler.getHelp();
    }

}
