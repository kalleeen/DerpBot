package fi.derpnet.derpbot.handler.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fi.derpnet.derpbot.bean.MatrixMessage;
import fi.derpnet.derpbot.bean.RawMessage;
import fi.derpnet.derpbot.connector.Connector;
import fi.derpnet.derpbot.connector.IrcConnector;
import fi.derpnet.derpbot.controller.MainController;
import fi.derpnet.derpbot.handler.SimpleMessageHandler;
import fi.derpnet.derpbot.util.CommandUtils;
import fi.derpnet.derpbot.util.RawMessageUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.apache.commons.io.IOUtils;

public class SslLabs implements SimpleMessageHandler {

    private static final String API_BASE_URL = "https://api.ssllabs.com/api/v2/analyze?host=";
    private static final String LINK_BASE_URL = "https://www.ssllabs.com/ssltest/analyze.html?latest&d=";
    private static final int TIMEOUT_MS = 300_000;

    @Override
    public void init(MainController controller) {
    }

    @Override
    public String getCommand() {
        return "!ssllabs";
    }

    @Override
    public String getHelp() {
        return "SSL Labs scan of a specific host";
    }

    @Override
    public String handle(String sender, String recipient, String message, Connector connector) {
        if (!message.startsWith("!ssllabs ")) {
            return null;
        }
        String host = CommandUtils.getFirstParameter(message).replace("http://", "").replace("https://", "");
        if (host.indexOf('/') > 0) {
            host = host.substring(0, host.indexOf('/'));
        }
        try {
            URL url = new URL(API_BASE_URL + host);
            try (InputStream is = url.openStream()) {
                String response = IOUtils.toString(is, "UTF-8");
                JsonParser parser = new JsonParser();
                JsonElement elem = parser.parse(response);
                String status = elem.getAsJsonObject().get("status").getAsString();
                switch (status) {
                    case "READY":
                        JsonObject endpoint = elem.getAsJsonObject().get("endpoints").getAsJsonArray().iterator().next().getAsJsonObject();
                        try {
                            return "SSL Labs grade for " + host + ": " + endpoint.get("grade").getAsString()+ " : " + LINK_BASE_URL + host;
                        } catch (Exception ex) {
                            return "SSL Labs analysis for " + host + ": " + endpoint.get("statusMessage").getAsString();
                        }
                    case "ERROR":
                        return "SSL Labs analysis for " + host + ": " + elem.getAsJsonObject().get("statusMessage").getAsString();
                    default:
                        if (connector instanceof IrcConnector) {
                            new PollerThread(connector, host, RawMessageUtils.getRecipientForResponse(sender, recipient), false).start();
                        }
                        else {
                            new PollerThread(connector, host, recipient, true).start();
                        }
                        
                        return "Analysis in progress for " + host;
                }
            }
        } catch (IOException ex) {
            return "Failed to get analysis for host " + host + " due to " + ex.getMessage() + ". Invalid hostname?";
        }
    }

    private class PollerThread extends Thread {

        private final Connector connector;
        private final String host;
        private final long start;
        private final String recipient;
        private String returnMsg;
        private boolean isMatrix;

        public PollerThread(Connector connector, String host, String recipient, boolean isMatrix) {
            this.connector = connector;
            this.host = host;
            this.recipient = recipient;
            this.isMatrix = isMatrix;
            start = System.currentTimeMillis();
        }

        @Override
        public void run() {
            loop:
            while (true) {
                try {
                    URL url = new URL(API_BASE_URL + host);
                    try (InputStream is = url.openStream()) {
                        String response = IOUtils.toString(is, "UTF-8");
                        JsonParser parser = new JsonParser();
                        JsonElement elem = parser.parse(response);
                        String status = elem.getAsJsonObject().get("status").getAsString();
                        switch (status) {
                            case "READY":
                                JsonObject endpoint = elem.getAsJsonObject().get("endpoints").getAsJsonArray().iterator().next().getAsJsonObject();
                                try {
                                    returnMsg = "SSL Labs grade for " + host + ": " + endpoint.get("grade").getAsString() + " : " + LINK_BASE_URL + host;
                                } catch (Exception ex) {
                                    returnMsg = "SSL Labs analysis for " + host + ": " + endpoint.get("statusMessage").getAsString();
                                }
                                break loop;
                            case "ERROR":
                                returnMsg = "SSL Labs analysis for " + host + ": " + elem.getAsJsonObject().get("statusMessage").getAsString();
                                break loop;
                            default:
                                sleep(5000);
                                break;
                        }
                        if (System.currentTimeMillis() - start > TIMEOUT_MS) {
                            returnMsg = "Timeout for SSL Labs for host " + host;
                            break;
                        }
                    }
                } catch (Exception ex) {
                    returnMsg = "Failed to get analysis for host " + host + ": " + ex.getMessage();
                    break;
                }
            }
            if (isMatrix) {
                connector.send(new MatrixMessage(returnMsg, recipient));
            }
            else {
                connector.send(new RawMessage(null, "PRIVMSG", recipient, ':' + returnMsg));
            }
        }
    }
}
