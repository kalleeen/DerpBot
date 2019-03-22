package fi.derpnet.derpbot.adapter;

import fi.derpnet.derpbot.bean.RawMessage;
import fi.derpnet.derpbot.connector.IrcConnector;
import fi.derpnet.derpbot.controller.MainController;
import fi.derpnet.derpbot.handler.RawMessageHandler;
import fi.derpnet.derpbot.handler.SimpleMessageHandler;
import fi.derpnet.derpbot.util.RawMessageUtils;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Adapter for using a SimpleMessageHandler in place of a RawMessageHandler
 */
public class SimpleMessageAdapter implements RawMessageHandler {

    private final SimpleMessageHandler handler;
    private final BiFunction<RawMessage, IrcConnector, List<RawMessage>> handle;

    public SimpleMessageAdapter(SimpleMessageHandler handler) {
        this.handler = handler;
        handle = handler.isLoud() ? this::handleLoud : this::handleNormal;
    }

    @Override
    public List<RawMessage> handle(RawMessage message, IrcConnector ircConnector) {
        return handle.apply(message, ircConnector);
    }

    private List<RawMessage> handleNormal(RawMessage message, IrcConnector ircConnector) {
        if (message.command.equals("PRIVMSG")) {
            String incomingRecipient = message.parameters.get(0);
            String messageBody = message.parameters.get(1);
            String responseBody = handler.handle(message.prefix, incomingRecipient, messageBody, ircConnector);
            if (responseBody == null) {
                return null;
            }
            String responseRecipient = RawMessageUtils.getRecipientForResponse(message);
            return Collections.singletonList(new RawMessage(null, "PRIVMSG", responseRecipient, ':' + responseBody));
        } else {
            return null;
        }
    }

    private List<RawMessage> handleLoud(RawMessage message, IrcConnector ircConnector) {
        if (message.command.equals("PRIVMSG")) {
            String incomingRecipient = message.parameters.get(0);
            String messageBody = message.parameters.get(1);
            String responseBody = handler.handle(message.prefix, incomingRecipient, messageBody, ircConnector);
            if (responseBody == null) {
                return null;
            }
            String responseRecipient = RawMessageUtils.getRecipientForResponse(message);
            if (ircConnector.getQuieterChannels() != null && ircConnector.getQuieterChannels().contains(responseRecipient.toLowerCase())) {
                responseRecipient = RawMessageUtils.privMsgSender(message);
            }
            return Collections.singletonList(new RawMessage(null, "PRIVMSG", responseRecipient, ':' + responseBody));
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
