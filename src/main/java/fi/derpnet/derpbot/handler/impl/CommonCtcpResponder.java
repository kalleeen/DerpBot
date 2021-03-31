package fi.derpnet.derpbot.handler.impl;

import fi.derpnet.derpbot.bean.Message;
import fi.derpnet.derpbot.bean.RawMessage;
import fi.derpnet.derpbot.connector.Connector;
import fi.derpnet.derpbot.connector.IrcConnector;
import fi.derpnet.derpbot.constants.IrcConstants;
import fi.derpnet.derpbot.controller.MainController;
import fi.derpnet.derpbot.util.RawMessageUtils;
import java.util.Collections;
import java.util.List;
import fi.derpnet.derpbot.handler.MessageHandler;

public class CommonCtcpResponder implements MessageHandler {

    @Override
    public void init(MainController controller) {
    }

    @Override
    public String getCommand() {
        return null;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public List<Message> handle(Message message, Connector connector) {
        if (!(message instanceof RawMessage)){
            return null;
        }
        RawMessage rawMessage = (RawMessage) message;
        IrcConnector ircConnector;
        if (connector instanceof IrcConnector) {
            ircConnector = (IrcConnector)connector;
        }
        else {
            return null;
        }
        if (rawMessage.parameters.size() < 2) {
            return null;
        }
        String body = rawMessage.parameters.get(1);
        if (!rawMessage.command.equals("PRIVMSG") || body.charAt(0) != IrcConstants.CTCP_CHAR || body.charAt(body.length() - 1) != IrcConstants.CTCP_CHAR) {
            return null;
        }
        body = body.substring(1, body.length() - 1);
        String[] split = body.split(" ");
        String recipient = RawMessageUtils.getRecipientForResponse(rawMessage);
        switch (split[0]) {
            case "VERSION":
                return buildResponse(recipient, "VERSION Derpbot v0.99");
            case "USERINFO":
                return buildResponse(recipient, "USERINFO " + ircConnector.realname);
            case "FINGER":
                return buildResponse(recipient, "FINGER " + ircConnector.realname);
            case "PING":
                return buildResponse(recipient, body);
            default:
                return null;
        }
    }

    private List<Message> buildResponse(String recipient, String resp) {
        return Collections.singletonList(new RawMessage(null, "NOTICE", recipient, ":" + IrcConstants.CTCP_CHAR + resp + IrcConstants.CTCP_CHAR));
    }
}
