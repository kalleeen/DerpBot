package fi.derpnet.derpbot.adapter;

import fi.derpnet.derpbot.bean.RawMessage;
import fi.derpnet.derpbot.connector.IrcConnector;
import fi.derpnet.derpbot.controller.MainController;
import fi.derpnet.derpbot.handler.RawMessageHandler;
import fi.derpnet.derpbot.handler.LoudMultiLineMessageHandler;
import fi.derpnet.derpbot.util.RawMessageUtils;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapter for using a LoudMultiLineMessageHandler in place of a
 * RawMessageHandler
 */
public class LoudMultiLineMessageAdapter implements RawMessageHandler {

    private final LoudMultiLineMessageHandler handler;

    public LoudMultiLineMessageAdapter(LoudMultiLineMessageHandler handler) {
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
            String responseRecipient = RawMessageUtils.getRecipientForResponse(message);
            if (ircConnector.getQuieterChannels() != null && ircConnector.getQuieterChannels().contains(responseRecipient.toLowerCase())) {
                responseRecipient = RawMessageUtils.privMsgSender(message);
            }
            String r = responseRecipient; // local variables referenced from a lambda expression must be final or effectively final
            return responseBodies.stream().map(msg -> new RawMessage(null, "PRIVMSG", r, ':' + msg)).collect(Collectors.toList());
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
