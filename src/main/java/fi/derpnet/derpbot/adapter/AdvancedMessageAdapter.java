package fi.derpnet.derpbot.adapter;

import fi.derpnet.derpbot.bean.MatrixMessage;
import fi.derpnet.derpbot.bean.Message;
import fi.derpnet.derpbot.bean.RawMessage;
import fi.derpnet.derpbot.connector.Connector;
import fi.derpnet.derpbot.controller.MainController;
import fi.derpnet.derpbot.handler.AdvancedMessageHandler;
import fi.derpnet.derpbot.util.RawMessageUtils;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import fi.derpnet.derpbot.handler.MessageHandler;

public class AdvancedMessageAdapter implements MessageHandler {

    private final AdvancedMessageHandler handler;
    private final BiFunction<Message, Connector, List<Message>> handle;

    public AdvancedMessageAdapter(AdvancedMessageHandler handler) {
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
            MatrixMessage response = handler.handle(rawMessage.prefix, incomingRecipient, messageBody, connector);
            if (response == null) {
                return null;
            }
            String responseRecipient = RawMessageUtils.getRecipientForResponse(rawMessage);
            return Collections.singletonList(new RawMessage(null, "PRIVMSG", responseRecipient, ':' + response.toString()));
        } else if (message instanceof MatrixMessage) {
            return Collections.singletonList(handler.handle(null, ((MatrixMessage) message).getRoomId(), ((MatrixMessage)message).toString(), connector));
        } else {
            return null;
        }
    }

    private List<Message> handleLoud(Message message, Connector connector) {
        if (message instanceof RawMessage && ((RawMessage)message).command.equals("PRIVMSG")) {
            RawMessage rawMessage = (RawMessage) message;
            String incomingRecipient = rawMessage.parameters.get(0);
            String messageBody = rawMessage.parameters.get(1);
            MatrixMessage response = handler.handle(rawMessage.prefix, incomingRecipient, messageBody, connector);
            if (response == null) {
                return null;
            }
            String responseRecipient = RawMessageUtils.getRecipientForResponse(rawMessage);
            if (connector.getQuieterChannels() != null && connector.getQuieterChannels().contains(responseRecipient.toLowerCase())) {
                responseRecipient = RawMessageUtils.privMsgSender(rawMessage);
            }
            return Collections.singletonList(new RawMessage(null, "PRIVMSG", responseRecipient, ':' + response.toString()));
        } else if (message instanceof MatrixMessage) {
            //TODO implement priv instead
            return Collections.singletonList(handler.handle(null, ((MatrixMessage) message).getRoomId(), ((MatrixMessage)message).toString(), connector));
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
