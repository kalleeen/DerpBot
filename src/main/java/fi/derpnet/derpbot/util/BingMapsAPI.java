/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.derpnet.derpbot.util;

import fi.derpnet.derpbot.bean.bingmaps.Travel;
import fi.derpnet.derpbot.bean.reittiopas.Location;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BingMapsAPI {
    
    private static final String DISTANCEMATRIX_API = "https://dev.virtualearth.net/REST/v1/Routes/DistanceMatrix";
    private String bingMapsAPIKey;
    
    private static final ThreadLocal<SimpleDateFormat> apiDateFormat = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
    
    private static final Logger LOG = Logger.getLogger(BingMapsAPI.class);
    
    public BingMapsAPI(String apiKey){
        this.bingMapsAPIKey = apiKey;
    }
    
    /**
     * Find travel time and distance by car between start and finish.
     *
     * @param start
     * @param finish
     * @return The travel between start and finish locations
     */
    public Travel getTravel(Location start, Location finish){
        try {
            //Get and fromat timestamp needed
            Date currentDate = new Date();
            String dateEncoded = URLEncoder.encode(apiDateFormat.get().format(currentDate), "UTF-8");
            String query = DISTANCEMATRIX_API + "?travelMode=driving&origins=" + start.getCoordinates()[1] + "," + start.getCoordinates()[0] 
                    + "&destinations=" + finish.getCoordinates()[1] + "," + finish.getCoordinates()[0] + "&startTime=" + dateEncoded
                    + "&key=" + bingMapsAPIKey;

            LOG.debug(query);

            String resultStr;
            URL url = new URL(query);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                resultStr = rd.lines().collect(Collectors.joining());
            }
            conn.disconnect();

            JSONObject obj = new JSONObject(resultStr);
            JSONArray resourceSets = obj.getJSONArray("resourceSets");
            JSONArray resources = resourceSets.getJSONObject(0).getJSONArray("resources");
            JSONArray results = resources.getJSONObject(0).getJSONArray("results");
            JSONObject result = results.getJSONObject(0);

            Travel travel = new Travel();
            
            travel.setTravelDistance(result.getDouble("travelDistance"));
            travel.setTravelDuration(result.getDouble("travelDuration"));

            return travel;
        } catch (IOException | JSONException ex) {
            LOG.warn("Kohdetta ei löytynyt tai ei voitu hakea", ex);
            return null;
        }
    }
    
}
