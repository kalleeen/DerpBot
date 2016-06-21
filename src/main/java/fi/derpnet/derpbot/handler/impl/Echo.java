package fi.derpnet.derpbot.handler.impl;

import fi.derpnet.derpbot.connector.IrcConnector;
import fi.derpnet.derpbot.controller.MainController;
import fi.derpnet.derpbot.handler.SimpleMessageHandler;

public class Echo implements SimpleMessageHandler {

    @Override
    public void init(MainController controller) {
    }

    @Override
    public String getHelp() {
        return "!echo - Echo a message back";
    }

    @Override
    public String handle(String sender, String recipient, String input, IrcConnector ircConnector) {
        return input.startsWith("!echo ") ? input.substring(6) : null;
    }

}
