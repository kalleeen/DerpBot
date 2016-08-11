package fi.derpnet.derpbot.handler.impl;

import fi.derpnet.derpbot.connector.IrcConnector;
import fi.derpnet.derpbot.controller.MainController;
import fi.derpnet.derpbot.handler.SimpleMultiLineMessageHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.input.BoundedInputStream;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;

public class LinkTitle implements SimpleMultiLineMessageHandler {

    private static final Logger LOG = Logger.getLogger(LinkTitle.class);

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
                URL url;
                try {
                    url = new URL(s);
                } catch (IOException ex) {
                    continue;
                }
                URLConnection connection = url.openConnection();
                connection.setReadTimeout(5000);
                InetAddress address = InetAddress.getByName(url.getHost());
                if (address.isSiteLocalAddress()) {
                    continue;
                }
                try (BufferedReader in = new BufferedReader(new InputStreamReader(new BoundedInputStream(connection.getInputStream(), 1024 * 1024)))) {
                    String inputLine;
                    StringBuilder contentBuilder = new StringBuilder();
                    try {
                        while ((inputLine = in.readLine()) != null) {
                            contentBuilder.append(inputLine).append(' ');
                        }
                    } catch (SocketTimeoutException ex) {
                        LOG.warn("Timeout getting content for: " + s, ex);
                    }
                    Matcher m = Pattern.compile("(.*)<title([^>]*)>(?<title>[^<]*)</title>(.*)").matcher(contentBuilder);
                    if (m.matches()) {
                        String title = StringEscapeUtils.unescapeHtml4(m.group("title").replaceAll("\\r|\\n", " ").replaceAll("\\s+", " ").trim());
                        responses.add(title);
                    }
                }
            } catch (IOException | ClassCastException ex) {
                LOG.warn("Failed to get link title for: " + s, ex);
            }
        }
        return responses.isEmpty() ? null : responses;
    }

}
