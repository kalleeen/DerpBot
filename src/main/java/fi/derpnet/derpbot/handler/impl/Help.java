package fi.derpnet.derpbot.handler.impl;

import fi.derpnet.derpbot.connector.IrcConnector;
import fi.derpnet.derpbot.controller.MainController;
import fi.derpnet.derpbot.handler.SimpleMessageHandler;
import fi.derpnet.derpbot.util.CommandUtils;

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
        if (!message.startsWith("!help")) {
            return null;
        }
        String param = CommandUtils.getFirstParameter(message);
        if (param == null) {
            StringBuilder sb = new StringBuilder("Commands: ");
            controller.getRawMessageHandlers().stream().map(c -> c.getCommand()).filter(s -> s != null).forEach(s -> sb.append(s).append(", "));
            return sb.substring(0, sb.length() - 2); // -2 to remove the last comma and space
        } else {
            String msg = controller.getRawMessageHandlers().stream().filter(h -> param.equals(h.getCommand())).map(h -> h.getHelp()).findAny().orElse(null);
            if (msg != null) {
                return msg;
            }
            String altParam = '!' + param;
            return controller.getRawMessageHandlers().stream().filter(h -> altParam.equals(h.getCommand())).map(h -> h.getHelp()).findAny().orElse("No help for " + param);
        }
    }

}
