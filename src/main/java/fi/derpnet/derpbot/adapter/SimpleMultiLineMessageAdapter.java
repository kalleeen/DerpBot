package fi.derpnet.derpbot.adapter;

import fi.derpnet.derpbot.bean.MatrixMessage;
import fi.derpnet.derpbot.bean.Message;
import fi.derpnet.derpbot.bean.RawMessage;
import fi.derpnet.derpbot.connector.Connector;
import fi.derpnet.derpbot.controller.MainController;
import fi.derpnet.derpbot.handler.SimpleMultiLineMessageHandler;
import fi.derpnet.derpbot.util.RawMessageUtils;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import fi.derpnet.derpbot.handler.MessageHandler;
import java.util.Collections;

/**
 * Adapter for using a SimpleMultiLineMessageHandler in place of a
 * RawMessageHandler
 */
public class SimpleMultiLineMessageAdapter implements MessageHandler {

    private final SimpleMultiLineMessageHandler handler;
    private final BiFunction<Message, Connector, List<Message>> handle;

    public SimpleMultiLineMessageAdapter(SimpleMultiLineMessageHandler handler) {
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
            List<String> responseBodies = handler.handle(rawMessage.prefix, incomingRecipient, messageBody, connector);
            if (responseBodies == null) {
                return null;
            }
            String responseRecipient = RawMessageUtils.getRecipientForResponse(rawMessage);
            return responseBodies.stream().map(msg -> new RawMessage(null, "PRIVMSG", responseRecipient, ':' + msg)).collect(Collectors.toList());
        } else if (message instanceof MatrixMessage) {
            List<String> responseBodies = handler.handle(null, ((MatrixMessage)message).getRoomId(), ((MatrixMessage)message).toString(), connector);
            if (responseBodies == null) {
                return null;
            }
            String response = responseBodies.stream().collect(Collectors.joining("<br />"));
            return Collections.singletonList(new MatrixMessage(response, ((MatrixMessage)message).getRoomId()));
        } else {
            return null;
        }
    }

    private List<Message> handleLoud(Message message, Connector connector) {
        if (message instanceof RawMessage && ((RawMessage)message).command.equals("PRIVMSG")) {
            RawMessage rawMessage = (RawMessage) message;
            String incomingRecipient = rawMessage.parameters.get(0);
            String messageBody = rawMessage.parameters.get(1);
            List<String> responseBodies = handler.handle(rawMessage.prefix, incomingRecipient, messageBody, connector);
            if (responseBodies == null) {
                return null;
            }
            String responseRecipient = RawMessageUtils.getRecipientForResponse(rawMessage);
            if (connector.getQuieterChannels() != null && connector.getQuieterChannels().contains(responseRecipient.toLowerCase())) {
                responseRecipient = RawMessageUtils.privMsgSender(rawMessage);
            }
            String r = responseRecipient; // local variables referenced from a lambda expression must be final or effectively final
            return responseBodies.stream().map(msg -> new RawMessage(null, "PRIVMSG", r, ':' + msg)).collect(Collectors.toList());
        } else if (message instanceof MatrixMessage) {
            // TODO quiet
            List<String> responseBodies = handler.handle(null, ((MatrixMessage)message).getRoomId(), ((MatrixMessage)message).toString(), connector);
            if (responseBodies == null) {
                return null;
            }
            String response = responseBodies.stream().collect(Collectors.joining("<br />"));
            return Collections.singletonList(new MatrixMessage(response, ((MatrixMessage)message).getRoomId()));
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
