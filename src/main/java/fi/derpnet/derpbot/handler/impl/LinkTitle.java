package fi.derpnet.derpbot.handler.impl;

import fi.derpnet.derpbot.connector.IrcConnector;
import fi.derpnet.derpbot.controller.MainController;
import fi.derpnet.derpbot.handler.SimpleMultiLineMessageHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.io.input.BoundedInputStream;

public class LinkTitle implements SimpleMultiLineMessageHandler {

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
    public List<String> handle(String sender, String recipient, String message, IrcConnector ircConnector) {
        if (!message.contains("http://") && !message.contains("https://")) {
            return null;
        }
        String[] parts = message.split("\\s+");
        List<String> responses = new LinkedList<>();
        for (String s : parts) {
            try {
                URL url = new URL(s);
                InetAddress address = InetAddress.getByName(url.getHost());
                if (address.isSiteLocalAddress()) {
                    continue;
                }
                try (BufferedReader in = new BufferedReader(new InputStreamReader(new BoundedInputStream(url.openStream(), 1024 * 1024 * 1024)))) {
                    String inputLine;
                    StringBuilder contentBuilder = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        contentBuilder.append(inputLine);
                    }
                    int start = contentBuilder.indexOf("<title>") + 7; // +7 because we don't want the title itself being caught
                    if (start == -1) {
                        continue;
                    }
                    int end = contentBuilder.indexOf("</title>");
                    if (end == -1 || end < start) {
                        continue;
                    }
                    String title = contentBuilder.substring(start, end).replaceAll("\\r\\n|\\r|\\n", " ").trim(); // Windoors uses \r\n unix uses \n and old macs use \r as linebreak because who needs common standards!
                    responses.add(title);
                }
            } catch (IOException | ClassCastException ex) {
            }
        }
        return responses.isEmpty() ? null : responses;
    }

}
