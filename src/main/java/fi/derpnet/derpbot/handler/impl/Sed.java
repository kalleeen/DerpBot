package fi.derpnet.derpbot.handler.impl;

import fi.derpnet.derpbot.connector.Connector;
import fi.derpnet.derpbot.connector.IrcConnector;
import fi.derpnet.derpbot.controller.MainController;
import fi.derpnet.derpbot.handler.SimpleMessageHandler;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Sed implements SimpleMessageHandler {

    private final Map<String, Map<String, String>> latestMessages = new ConcurrentHashMap<>();

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
    public String handle(String sender, String recipient, String message, Connector connector) {
        IrcConnector ircConnector;
        if (connector instanceof IrcConnector) {
            ircConnector = (IrcConnector)connector;
        }
        else {
            return null;
        }
        if (!message.startsWith("s/")) {
            Map<String, String> msgMap = latestMessages.get(ircConnector.networkName);
            if (msgMap == null) {
                synchronized (latestMessages) {
                    msgMap = latestMessages.get(ircConnector.networkName);
                    if (msgMap == null) {
                        msgMap = new ConcurrentHashMap<>();
                        latestMessages.put(ircConnector.networkName, msgMap);
                    }
                }
            }
            msgMap.put(sender, message);
            return null;
        }
        String previousMessage = latestMessages.get(ircConnector.networkName).get(sender);
        Matcher m = Pattern.compile("^s/(?<from>[^/]*)/(?<to>[^/]*)/(?<options>[g]?)$").matcher(message);
        String newMessage = null;
        if (m.matches()) {
            if ("g".equals(m.group("options"))) {
                newMessage = previousMessage.replaceAll(m.group("from"), m.group("to"));
            } else {
                newMessage = previousMessage.replaceFirst(m.group("from"), m.group("to"));
            }
        }
        if (newMessage == null || newMessage.equals(previousMessage)) {
            return null;
        } else {
            return '<' + sender.split("!", 2)[0] + "> " + newMessage;
        }
    }
}
