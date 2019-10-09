
package fi.derpnet.derpbot.handler.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import fi.derpnet.derpbot.bean.RawMessage;
import fi.derpnet.derpbot.connector.IrcConnector;
import fi.derpnet.derpbot.controller.MainController;
import fi.derpnet.derpbot.handler.SimpleMessageHandler;
import fi.derpnet.derpbot.util.CommandUtils;
import fi.derpnet.derpbot.util.RawMessageUtils;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.log4j.Logger;


public class Posti implements SimpleMessageHandler {
    
    private static final String API_BASE_URL = "https://www.posti.fi/henkiloasiakkaat/seuranta/api/shipments";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final int POLLRATE = 600; // 10 min
    
    Logger logger = Logger.getLogger(Posti.class);

    public void init(MainController controller) {
    }

    @Override
    public String getCommand() {
        return "!posti";
    }

    @Override
    public String getHelp() {
        return "Track a package with a specified tracking code";
    }

    @Override
    public String handle(String sender, String recipient, String message, IrcConnector ircConnector) {
        if (!message.startsWith("!posti ")) {
            return null;
        }
        
        String trackingCode = CommandUtils.getFirstParameter(message);
        JsonElement responseJson = getStatusFromPosti(trackingCode);
        
        if (responseJson == null) {
            return "Failed to get response from Posti";
        }
        
        JsonArray shipments = responseJson.getAsJsonObject().get("shipments").getAsJsonArray();
        
        if (shipments == null || shipments.size() < 1){
            return "No shipments found using tracking code " + trackingCode;
        }
        
        JsonObject shipment = shipments.get(0).getAsJsonObject();
        String phase = shipment.get("phase").getAsString();
        JsonArray events = shipment.get("events").getAsJsonArray();
        
        if (phase != null && phase.equals("DELIVERED")){
            return "Shipment " + shipment.get("trackingCode").getAsString() + " already delivered at "
                    + shipment.get("estimatedDeliveryTime").getAsString() + ".";
        }
        
        new PollerThread(ircConnector, trackingCode, events.size(), RawMessageUtils.getRecipientForResponse(sender, recipient)).start();
        
        if (events.size() == 0){
            return "Shipment " + shipment.get("trackingCode").getAsString() + " with destination " + shipment.get("destinationPostcode").getAsString()
                    + " " + shipment.get("destinationCity").getAsString() + " registered for tracking. No tracking events yet to show.";
        }
        else {
            JsonObject lastEvent = events.get(0).getAsJsonObject();
            return "Shipment " + shipment.get("trackingCode").getAsString() + " with destination " + shipment.get("destinationPostcode").getAsString()
                    + " " + shipment.get("destinationCity").getAsString() + " registered for tracking. Estimated delivery time "
                    + shipment.get("estimatedDeliveryTime").getAsString() + ". Last event at "
                    + lastEvent.get("timestamp").getAsString() + ": " + lastEvent.get("description").getAsJsonObject().get("en").getAsString()
                    + " Location: " + lastEvent.get("locationName").getAsString();
        }
    }
    
    protected JsonElement getStatusFromPosti(String trackingCode) {
        String payload = "{\"trackingCodes\": [\"" + trackingCode + "\"]}";
        try {
            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(payload, JSON);
            Request request = new Request.Builder()
                    .url(API_BASE_URL)
                    .post(body)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                JsonParser parser = new JsonParser();
                return parser.parse(response.body().string());
            }
        } catch (IOException | JsonSyntaxException | NullPointerException ex) {
            logger.error("Getting response from Posti failed", ex);
            return null;
        }
    }
    
    private class PollerThread extends Thread {

        private final IrcConnector ircConnector;
        private final String trackingCode;
        private int eventsNumber;
        private final String recipient;
        private String returnMsg;

        public PollerThread(IrcConnector ircConnector, String trackingCode, int eventsNumber, String recipient) {
            this.ircConnector = ircConnector;
            this.trackingCode = trackingCode;
            this.recipient = recipient;
            this.eventsNumber = eventsNumber;
        }

        @Override
        public void run() {
            loop:
            while (true) {
                try {
                    JsonElement responseJson = getStatusFromPosti(trackingCode);
                
                    if (responseJson == null) {
                        sleep(POLLRATE * 1000);
                        continue;
                    }
                    
                    JsonArray shipments = responseJson.getAsJsonObject().get("shipments").getAsJsonArray();
                    
                    if (shipments == null || shipments.size() < 1){
                        sleep(POLLRATE * 1000);
                        continue;
                    }
                    
                    JsonObject shipment = shipments.get(0).getAsJsonObject();
                    String phase = shipment.get("phase").getAsString();
                    JsonArray events = shipment.get("events").getAsJsonArray();
                    
                    if (events.size() != eventsNumber){
                        eventsNumber = events.size();
                        JsonObject lastEvent = events.get(0).getAsJsonObject();
                        returnMsg = "New tracking event for shipment " + shipment.get("trackingCode").getAsString() + " with desination "
                                + shipment.get("destinationPostcode").getAsString() + " " + shipment.get("destinationCity").getAsString() + ": "
                                + lastEvent.get("timestamp").getAsString() + ": " + lastEvent.get("description").getAsJsonObject().get("en").getAsString()
                                + ". Estimated delivery time: " + shipment.get("estimatedDeliveryTime").getAsString()
                                + " Location: " + lastEvent.get("locationName").getAsString();;
                        ircConnector.send(new RawMessage(null, "PRIVMSG", recipient, ':' + returnMsg));
                    }
                    
                    if (phase.equals("DELIVERED")){
                        returnMsg = "Shipment " + shipment.get("trackingCode").getAsString() + " marked as delivered, ending tracking.";
                        ircConnector.send(new RawMessage(null, "PRIVMSG", recipient, ':' + returnMsg));
                        break;
                    }
                    
                    sleep(POLLRATE * 1000);
                    
                } catch (Exception ex) {
                    returnMsg = "Failed to get track shipment " + trackingCode + ", error: " + ex.getMessage();
                }
            }
        }
    }
}
