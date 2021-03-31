package fi.derpnet.derpbot.adapter;

import fi.derpnet.derpbot.bean.MatrixMessage;
import fi.derpnet.derpbot.bean.Message;
import fi.derpnet.derpbot.bean.RawMessage;
import fi.derpnet.derpbot.connector.Connector;
import fi.derpnet.derpbot.controller.MainController;
import fi.derpnet.derpbot.handler.SimpleMessageHandler;
import fi.derpnet.derpbot.util.RawMessageUtils;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import fi.derpnet.derpbot.handler.MessageHandler;

/**
 * Adapter for using a SimpleMessageHandler in place of a RawMessageHandler
 */
public class SimpleMessageAdapter implements MessageHandler {

    private final SimpleMessageHandler handler;
    private final BiFunction<Message, Connector, List<Message>> handle;

    public SimpleMessageAdapter(SimpleMessageHandler handler) {
        this.handler = handler;
        handle = handler.isLoud() ? this::handleLoud : this::handleNormal;
    }

    @Override
    public List<Message> handle(Message message, Connector connector) {
        return handle.apply(message, connector);
    }

    private List<Message> handleNormal(Message message, Connector connector) {
        if (message instanceof RawMessage && ((RawMessage)message).command.equals("PRIVMSG")) {
            RawMessage rawMessage = (RawMessage) message;
            String incomingRecipient = rawMessage.parameters.get(0);
            String messageBody = rawMessage.parameters.get(1);
            String responseBody = handler.handle(rawMessage.prefix, incomingRecipient, messageBody, connector);
            if (responseBody == null) {
                return null;
            }
            String responseRecipient = RawMessageUtils.getRecipientForResponse(rawMessage);
            return Collections.singletonList(new RawMessage(null, "PRIVMSG", responseRecipient, ':' + responseBody));
        } else if (message instanceof MatrixMessage) {
            String responseBody = handler.handle(null, ((MatrixMessage) message).getRoomId(), ((MatrixMessage)message).toString(), connector);
            if (responseBody == null) {
                return null;
            }
            return Collections.singletonList(new MatrixMessage(responseBody, ((MatrixMessage)message).getRoomId()));
        } else {
            return null;
        }
    }

    private List<Message> handleLoud(Message message, Connector connector) {
        if (message instanceof RawMessage && ((RawMessage)message).command.equals("PRIVMSG")) {
            RawMessage rawMessage = (RawMessage) message;
            String incomingRecipient = rawMessage.parameters.get(0);
            String messageBody = rawMessage.parameters.get(1);
            String responseBody = handler.handle(rawMessage.prefix, incomingRecipient, messageBody, connector);
            if (responseBody == null) {
                return null;
            }
            String responseRecipient = RawMessageUtils.getRecipientForResponse(rawMessage);
            if (connector.getQuieterChannels() != null && connector.getQuieterChannels().contains(responseRecipient.toLowerCase())) {
                responseRecipient = RawMessageUtils.privMsgSender(rawMessage);
            }
            return Collections.singletonList(new RawMessage(null, "PRIVMSG", responseRecipient, ':' + responseBody));
        } else if (message instanceof MatrixMessage) {
            //TODO implement priv instead
            String responseBody = handler.handle(null, ((MatrixMessage) message).getRoomId(), ((MatrixMessage)message).toString(), connector);
            if (responseBody == null) {
                return null;
            }
            return Collections.singletonList(new MatrixMessage(responseBody, ((MatrixMessage)message).getRoomId()));
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
