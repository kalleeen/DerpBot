package fi.derpnet.derpbot.handler.impl;

import fi.derpnet.derpbot.connector.IrcConnector;
import fi.derpnet.derpbot.controller.MainController;
import fi.derpnet.derpbot.handler.SimpleMessageHandler;
import fi.derpnet.derpbot.util.CommandUtils;
import java.util.Random;

public class Randomizer implements SimpleMessageHandler {
    
    @Override
    public void init(MainController controller) {
    }
    
    @Override
    public String getCommand() {
        return "!random";
    }
    
    @Override
    public String getHelp() {
        return "Random number between 0 (inclusive) and specified value (exclusive)";
    }
    
    @Override
    public String handle(String sender, String recipient, String message, IrcConnector ircConnector) {
        if (!message.startsWith("!random")) {
            return null;
        }
        String value = CommandUtils.getFirstParameter(message);
        int max = Integer.MAX_VALUE;
        if (value != null && value.trim().length() > 0) {
            try {
                max = Integer.parseInt(value.trim());
                if (max <= 0) {
                    return "Max value must be greater than 0";
                }
            } catch (Exception ex) {
                return "Invalid number '" + value.trim() + '\'';
            }
        }
        Random rng = new Random();
        return String.valueOf(rng.nextInt(max));
    }
    
}
