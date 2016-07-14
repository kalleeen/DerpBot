package fi.derpnet.derpbot.handler.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fi.derpnet.derpbot.bean.RawMessage;
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
    
    public static final String BASE_URL = "https://api.ssllabs.com/api/v2/analyze?host=";
    public static final int TIMEOUT_MS = 300_000;
    
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
    public String handle(String sender, String recipient, String message, IrcConnector ircConnector) {
        if (!message.startsWith("!ssllabs ")) {
            return null;
        }
        String host = CommandUtils.getFirstParameter(message).replace("http://", "").replace("https://", "");
        if (host.indexOf('/') > 0) {
            host = host.substring(0, host.indexOf('/'));
        }
        try {
            URL url = new URL(BASE_URL + host);
            try (InputStream is = url.openStream()) {
                String response = IOUtils.toString(is, "UTF-8");
                JsonParser parser = new JsonParser();
                JsonElement elem = parser.parse(response);
                String status = elem.getAsJsonObject().get("status").getAsString();
                switch (status) {
                    case "READY":
                        JsonObject endpoint = elem.getAsJsonObject().get("endpoints").getAsJsonArray().iterator().next().getAsJsonObject();
                        try {
                            return "SSL Labs grade for " + host + ": " + endpoint.get("grade").getAsString();
                        } catch (Exception ex) {
                            return "SSL Labs analysis for " + host + ": " + endpoint.get("statusMessage").getAsString();
                        }
                    case "ERROR":
                        return "SSL Labs analysis for " + host + ": " + elem.getAsJsonObject().get("statusMessage").getAsString();
                    default:
                        new PollerThread(ircConnector, host, RawMessageUtils.getRecipientForResponse(sender, recipient)).start();
                        return "Analysis in progress for " + host;
                }
            }
        } catch (IOException ex) {
            return "Failed to get analysis for host " + host + " due to " + ex.getMessage() + ". Invalid hostname?";
        }
    }
    
    private class PollerThread extends Thread {
        
        private final IrcConnector ircConnector;
        private final String host;
        private final long start;
        private final String recipient;
        private String returnMsg;
        
        public PollerThread(IrcConnector ircConnector, String host, String recipient) {
            this.ircConnector = ircConnector;
            this.host = host;
            this.recipient = recipient;
            start = System.currentTimeMillis();
        }
        
        @Override
        public void run() {
            loop:
            while (true) {
                try {
                    URL url = new URL(BASE_URL + host);
                    try (InputStream is = url.openStream()) {
                        String response = IOUtils.toString(is, "UTF-8");
                        JsonParser parser = new JsonParser();
                        JsonElement elem = parser.parse(response);
                        String status = elem.getAsJsonObject().get("status").getAsString();
                        switch (status) {
                            case "READY":
                                JsonObject endpoint = elem.getAsJsonObject().get("endpoints").getAsJsonArray().iterator().next().getAsJsonObject();
                                try {
                                    returnMsg = "SSL Labs grade for " + host + ": " + endpoint.get("grade").getAsString();
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
            ircConnector.send(new RawMessage(null, "PRIVMSG", recipient, ':' + returnMsg));
        }
    }
}
