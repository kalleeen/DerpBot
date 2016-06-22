package fi.derpnet.derpbot.handler.impl;

import fi.derpnet.derpbot.connector.IrcConnector;
import fi.derpnet.derpbot.controller.MainController;
import fi.derpnet.derpbot.handler.SimpleMessageHandler;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

public class MacFinder implements SimpleMessageHandler {
    
    private static Map<String, String> macs;
    
    @Override
    public synchronized void init(MainController controller) {
        if (macs == null) {
            macs = new LinkedHashMap<>();
            InputStream is = this.getClass().getResourceAsStream("/macs.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            br.lines().forEach(line -> macs.put(line.toLowerCase(), line));
        }
    }
    
    @Override
    public String getHelp() {
        return "!mac - searches for MAC addresses and their manufacturers";
    }
    
    @Override
    public String handle(String sender, String recipient, String message, IrcConnector ircConnector) {
        if (!message.startsWith("!mac ") || message.length() < 8) {
            return null;
        }
        String search = message.substring(5).toLowerCase();
        return macs.entrySet().stream().filter(e -> e.getKey().contains(search)).map(e -> e.getValue()).findAny().orElse("Not found");
    }
}
