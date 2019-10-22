
package fi.derpnet.derpbot.handler.impl;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import fi.derpnet.derpbot.bean.RawMessage;
import fi.derpnet.derpbot.bean.posti.Event;
import fi.derpnet.derpbot.bean.posti.Shipment;
import fi.derpnet.derpbot.bean.posti.Shipments;
import fi.derpnet.derpbot.connector.IrcConnector;
import fi.derpnet.derpbot.controller.MainController;
import fi.derpnet.derpbot.handler.SimpleMessageHandler;
import fi.derpnet.derpbot.util.CommandUtils;
import fi.derpnet.derpbot.util.RawMessageUtils;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
        Shipments response = getStatusFromPosti(trackingCode);
        
        if (response == null) {
            return "Failed to get response from Posti";
        }
        
        List<Shipment> shipments = response.getShipments();
        
        if (shipments == null || shipments.size() < 1){
            return "No shipments found using tracking code " + trackingCode;
        }
        
        Shipment shipment = shipments.get(0);
        String phase = shipment.getPhase();
        List<Event> events = shipment.getEvents();
        
        if (phase != null && phase.equals("DELIVERED")){
            return "Shipment " + shipment.getTrackingCode() + " already delivered at "
                    + formatDate(shipment.getEstimatedDeliveryTime()) + ".";
        }
        
        new PollerThread(ircConnector, trackingCode, events.size(), RawMessageUtils.getRecipientForResponse(sender, recipient)).start();
        
        if (events.isEmpty()){
            String output =  "Shipment " + shipment.getTrackingCode(); 
            if (shipment.getDestinationPostcode() != null) {
                output += " with destination " + shipment.getDestinationPostcode() + " " + shipment.getDestinationCity() + " ";
            }
            output += "registered for tracking. No tracking events yet to show.";
            return output;
        }
        else {
            String output =  "Shipment " + shipment.getTrackingCode() + " ";
            if (shipment.getDestinationPostcode() != null) {
                output += " with destination " + shipment.getDestinationPostcode() + " " + shipment.getDestinationCity() + " ";
            }
            output += "registered for tracking.";
            if (shipment.getEstimatedDeliveryTime() != null) {
                output += " Estimated delivery time " + formatDate(shipment.getEstimatedDeliveryTime()) + ".";
            }
            if (shipment.getEvents() != null && shipment.getEvents().size() > 0){
                Event lastEvent = events.get(0);
                output += " Last event at " + formatDate(lastEvent.getTimestamp()) + ": " + lastEvent.getDescription().getEn() + " Location: " + lastEvent.getLocationName();
            }
            return output;
        }
    }
    
    protected Shipments getStatusFromPosti(String trackingCode) {
        String payload = "{\"trackingCodes\": [\"" + trackingCode + "\"]}";
        try {
            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(payload, JSON);
            Request request = new Request.Builder()
                    .url(API_BASE_URL)
                    .post(body)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                Gson gson = new Gson();
                return gson.fromJson(response.body().string(), Shipments.class);
            }
        } catch (IOException | JsonSyntaxException | NullPointerException ex) {
            logger.error("Getting response from Posti failed", ex);
            return null;
        }
    }
    
    protected String formatDate(String date){
        Instant instant = Instant.parse(date);
        LocalDateTime local =  LocalDateTime.from(instant.atZone(ZoneId.systemDefault()));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return local.format(formatter);
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
                    Shipments response = getStatusFromPosti(trackingCode);
                
                    if (response == null) {
                        sleep(POLLRATE * 1000);
                        continue;
                    }
                    
                    List<Shipment> shipments = response.getShipments();
        
                    if (shipments == null || shipments.size() < 1){
                        sleep(POLLRATE * 1000);
                        continue;
                    }
                    
                    Shipment shipment = shipments.get(0);
                    String phase = shipment.getPhase();
                    List<Event> events = shipment.getEvents();
                    
                    if (events.size() != eventsNumber){
                        eventsNumber = events.size();
                        returnMsg = "New tracking event for shipment " + shipment.getTrackingCode();
                        if (shipment.getDestinationPostcode() != null) {
                            returnMsg += " with destination " + shipment.getDestinationPostcode() + " " + shipment.getDestinationCity();
                        }
                        returnMsg += ": ";
                        Event lastEvent = events.get(0);
                        returnMsg += " Last event at " + formatDate(lastEvent.getTimestamp()) + ": " + lastEvent.getDescription().getEn() + " Location: " + lastEvent.getLocationName();
                        if (shipment.getEstimatedDeliveryTime() != null) {
                            returnMsg += " Estimated delivery time " + formatDate(shipment.getEstimatedDeliveryTime()) + ".";
                        }
                        ircConnector.send(new RawMessage(null, "PRIVMSG", recipient, ':' + returnMsg));
                    }
                    
                    if (phase.equals("DELIVERED")){
                        returnMsg = "Shipment " + shipment.getTrackingCode() + " marked as delivered, ending tracking.";
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
