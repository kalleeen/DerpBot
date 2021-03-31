package fi.derpnet.derpbot.handler.impl;

import fi.derpnet.derpbot.connector.Connector;
import fi.derpnet.derpbot.controller.MainController;
import fi.derpnet.derpbot.handler.SimpleMessageHandler;
import fi.derpnet.derpbot.util.CommandUtils;
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
            br.lines().forEach(line -> macs.put(line.toLowerCase().replace(":", ""), line));
        }
    }

    @Override
    public String getCommand() {
        return "!mac";
    }

    @Override
    public String getHelp() {
        return "Searches for MAC addresses and their manufacturers";
    }

    @Override
    public String handle(String sender, String recipient, String message, Connector connector) {
        if (!message.startsWith("!mac") || CommandUtils.getFirstParameter(message) == null) {
            return null;
        }
        String search = CommandUtils.getFirstParameter(message).toLowerCase().replace(":", "").replace(".", "");
        String ret = macs.entrySet().stream().filter(e -> e.getKey().contains(search)).map(e -> e.getValue()).findAny().orElse(null);
        if (ret != null) {
            return ret;
        }
        String trimmedSearch = search.substring(0, 6);
        return macs.entrySet().stream().filter(e -> e.getKey().contains(trimmedSearch)).map(e -> e.getValue()).findAny().orElse("Not found");
    }
}
