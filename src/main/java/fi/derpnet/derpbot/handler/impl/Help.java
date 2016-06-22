package fi.derpnet.derpbot.handler.impl;

import fi.derpnet.derpbot.connector.IrcConnector;
import fi.derpnet.derpbot.controller.MainController;
import fi.derpnet.derpbot.handler.SimpleMessageHandler;

public class Help implements SimpleMessageHandler {

    private MainController controller;

    @Override
    public void init(MainController controller) {
        this.controller = controller;
    }

    @Override
    public String getCommand() {
        return "!help";
    }

    @Override
    public String getHelp() {
        return "Gets help of a command, or a list of commands if no command is specified";
    }

    @Override
    public String handle(String sender, String recipient, String message, IrcConnector ircConnector) {
        if (message.equals("!help")) {
            StringBuilder sb = new StringBuilder("Commands: ");
            controller.getRawMessageHandlers().stream().map(c -> c.getCommand()).filter(s -> s != null).forEach(s -> sb.append(s).append(", "));
            return sb.substring(0, sb.length() - 2); // -2 to remove the last comma and space
        } else if (message.startsWith("!help ") && message.length() > 7) {
            String command = message.substring(6);
            return controller.getRawMessageHandlers().stream().filter(h -> command.equals(h.getCommand())).map(h -> h.getHelp()).findAny().orElse("No help for " + command);
        } else {
            return null;
        }
    }

}
