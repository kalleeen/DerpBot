/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.derpnet.derpbot.handler.impl;

import fi.derpnet.derpbot.bean.bingmaps.Travel;
import fi.derpnet.derpbot.bean.reittiopas.Location;
import fi.derpnet.derpbot.connector.Connector;
import fi.derpnet.derpbot.controller.MainController;
import fi.derpnet.derpbot.handler.SimpleMessageHandler;
import fi.derpnet.derpbot.util.BingMapsAPI;
import fi.derpnet.derpbot.util.ReittiopasAPI;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

public class Maps implements SimpleMessageHandler {
    
    private ReittiopasAPI reittiopasApi;
    private BingMapsAPI bingApi;
    
    private static final Logger LOG = Logger.getLogger(Maps.class);

    @Override
    public void init(MainController controller) {
        reittiopasApi = new ReittiopasAPI();
        bingApi = new BingMapsAPI(controller.getConfig().get("bing.maps.apikey"));
    }
    
    @Override
    public String getCommand() {
        return "!maps";
    }
    
    @Override
    public String getHelp() {
        return "Maps distance and traveltime between two points, !maps start -- destination";
    }
    
    @Override
    public String handle(String sender, String recipient, String message, Connector connector) {
        if (!message.startsWith("!maps ")) {
            return null;
        }

        String routeSearch = message.substring(6);
        String[] split = routeSearch.split("--");

        try {
            Location start = reittiopasApi.getLocation(split[0].trim());

            if (start == null) {
                return "Starting point not found!";
            }

            Location destination = reittiopasApi.getLocation(split[1].trim());

            if (destination == null) {
                return "Destination point not found!";
            }
            
            Travel travel = bingApi.getTravel(start, destination);
            
            String timeFormatted;
            if (travel.getTravelDuration() < 1){
                timeFormatted = "<1 min";
            }
            else if (travel.getTravelDuration() > 60){
                int hours = (int) (travel.getTravelDuration() / 60);
                int mins = (int) Math.round(travel.getTravelDuration() - hours*60);
                timeFormatted = hours + "h " + mins + " min";
            }
            else {
                int durationRound = (int) Math.round(travel.getTravelDuration());
                timeFormatted = durationRound + " min";
            }
            
            return start.getDescription() + " - " + destination.getDescription() 
                    + ": Current estimated time " + timeFormatted + ", distance " + travel.getTravelDistance() + " km by car";
            
        }catch (NullPointerException npe) {
            LOG.error("Maps nullpointer?",npe);
            return "Something wrong -- right usage? !maps start -- destination";
        }
    }
    
}
