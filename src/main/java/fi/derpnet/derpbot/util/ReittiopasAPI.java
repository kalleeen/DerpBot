package fi.derpnet.derpbot.util;

import fi.derpnet.derpbot.bean.reittiopas.Leg;
import fi.derpnet.derpbot.bean.reittiopas.Location;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ReittiopasAPI {

    private static final String GEOCODING_API = "https://api.digitransit.fi/geocoding/v1/search";
    private static final String HELSINKI_API = "https://api.digitransit.fi/routing/v1/routers/hsl/index/graphql";
    
    private static final ThreadLocal<SimpleDateFormat> apiDateFormat = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));
    private static final ThreadLocal<SimpleDateFormat> apiTimeFormat = ThreadLocal.withInitial(() -> new SimpleDateFormat("HH:mm:ss"));

    private static final Logger LOG = LogManager.getLogger(ReittiopasAPI.class);
    
    /**
     * Find latitude + longitude and description for search term. Searches from
     * whole Finland (Be aware!)
     *
     * @param locationSearch
     * @return Location if found, or null if no matching Location was found
     */
    public Location getLocation(String locationSearch) {
        try {
            String locationEncoded = URLEncoder.encode(locationSearch, "UTF-8");
            String query = GEOCODING_API + "?text=" + locationEncoded + "&size=1&focus.point.lat=60.1715994&focus.point.lon=24.9411779";

            LOG.debug(query);

            String result;
            URL url = new URL(query);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                result = rd.lines().collect(Collectors.joining());
            }
            conn.disconnect();

            JSONObject obj = new JSONObject(result);
            JSONArray features = obj.getJSONArray("features");
            JSONArray coordinates = features.getJSONObject(0).getJSONObject("geometry").getJSONArray("coordinates");
            JSONObject properties = features.getJSONObject(0).getJSONObject("properties");

            double[] coord = new double[2];

            coord[0] = coordinates.getDouble(0);
            coord[1] = coordinates.getDouble(1);

            String name = properties.getString("name");
            String localadmin = properties.getString("localadmin");

            Location location = new Location();
            location.setCoordinates(coord);
            location.setDescription(name + ", " + localadmin);

            return location;
        } catch (IOException | JSONException ex) {
            LOG.warn("Kohdetta ei löytynyt tai ei voitu hakea", ex);
            return null;
        }
    }

    /**
     * Find route by public transport and walking between to coordinates. Only
     * works on HSL area!
     *
     * @param start
     * @param finish
     * @return The route between start and finish locations
     */
    public List<Leg> getRoute(Location start, Location finish) {
        Date currentDate = new Date();
        String plan = "plan("
                + "from: {lat: " + start.getCoordinates()[1] + ", lon: " + start.getCoordinates()[0] + "},"
                + "to: {lat: " + finish.getCoordinates()[1] + ", lon: " + finish.getCoordinates()[0] + "},"
                + "modes: \"BUS,TRAM,RAIL,SUBWAY,FERRY,WALK\","
                + "date: \"" + apiDateFormat.get().format(currentDate) + "\","
                + "time: \"" + apiTimeFormat.get().format(currentDate) + "\","
                + //"walkReluctance: 2.1," +
                //"walkBoardCost: 600," +
                //"minTransferTime: 180," +
                //"walkSpeed: 1.2," +
                ")";

        String iternaries = "{"
                + "    itineraries{"
                + "      walkDistance,"
                + "      duration,"
                + "      legs {"
                + "        mode"
                + "        route {"
                + "          id"
                + "        }"
                + "        startTime"
                + "        endTime"
                + "        from {"
                + "          lat"
                + "          lon"
                + "          name"
                + "          stop {"
                + "            code"
                + "            name"
                + "          }"
                + "        },"
                + "        to {"
                + "          lat"
                + "          lon"
                + "          name"
                + "          stop {"
                + "            code"
                + "            name"
                + "          }"
                + "        },"
                + "        agency {"
                + "          id"
                + "        },"
                + "        distance"
                + "      }"
                + "    }"
                + "  }";

        try {
            URL url = new URL(HELSINKI_API);

            String query = "{" + plan + iternaries + "}";
            byte[] postData = query.getBytes(StandardCharsets.ISO_8859_1);
            int postDataLength = postData.length;
            String result;

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/graphql");
            conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));

            try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                wr.write(postData);
            }

            try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                result = rd.lines().collect(Collectors.joining());
            }
            conn.disconnect();

            JSONObject obj = new JSONObject(result);
            JSONArray legsJson = obj.getJSONObject("data").getJSONObject("plan").getJSONArray("itineraries").getJSONObject(0).getJSONArray("legs");

            List<Leg> legs = new LinkedList<>();

            for (int i = 0; i < legsJson.length(); i++) {
                JSONObject legJson = legsJson.getJSONObject(i);
                Leg leg = new Leg();

                leg.setMode(legJson.getString("mode"));

                if (legJson.isNull("route")) {
                    leg.setLine(null);
                } else {
                    String lineBase64 = legJson.getJSONObject("route").getString("id");
                    String lineAnswer = new String(Base64.getDecoder().decode(lineBase64));

                    String[] lineSplitted = lineAnswer.split(":");
                    String lineNumber = lineSplitted[2].substring(1);

                    leg.setLine(lineNumber);
                }

                leg.setStartTime(new Date(legJson.getLong("startTime")));
                leg.setFinishTime(new Date(legJson.getLong("endTime")));

                Location startLocation = new Location();
                startLocation.setCoordinates(new double[]{legJson.getJSONObject("from").getDouble("lon"), legJson.getJSONObject("from").getDouble("lat")});
                startLocation.setDescription(legJson.getJSONObject("from").getString("name"));

                if (legJson.getJSONObject("from").isNull("stop")) {
                    startLocation.setCode(null);
                } else {
                    startLocation.setCode(legJson.getJSONObject("from").getJSONObject("stop").getString("code"));
                }

                leg.setStartLocation(startLocation);

                Location finishLocation = new Location();
                finishLocation.setCoordinates(new double[]{legJson.getJSONObject("to").getDouble("lon"), legJson.getJSONObject("to").getDouble("lat")});
                finishLocation.setDescription(legJson.getJSONObject("to").getString("name"));

                if (legJson.getJSONObject("to").isNull("stop")) {
                    finishLocation.setCode(null);
                } else {
                    finishLocation.setCode(legJson.getJSONObject("to").getJSONObject("stop").getString("code"));
                }

                leg.setFinishLocation(finishLocation);

                leg.setDistance(legJson.getDouble("distance"));

                legs.add(leg);
            }

            return legs;
        } catch (IOException | JSONException ex) {
            LOG.warn("Reittiä ei löytynyt tai ei voitu hakea", ex);
            return null;
        }
    }
}
