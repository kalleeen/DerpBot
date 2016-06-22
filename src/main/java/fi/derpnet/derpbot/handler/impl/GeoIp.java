package fi.derpnet.derpbot.handler.impl;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Location;
import com.maxmind.geoip2.record.Postal;
import fi.derpnet.derpbot.connector.IrcConnector;
import fi.derpnet.derpbot.controller.MainController;
import fi.derpnet.derpbot.handler.SimpleMessageHandler;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class GeoIp implements SimpleMessageHandler {

    private DatabaseReader databaseReader;

    @Override
    public void init(MainController controller) {
        try {
            databaseReader = new DatabaseReader.Builder(this.getClass().getResourceAsStream("/GeoLite2-City.mmdb")).build();
        } catch (IOException e) {
        }
    }

    @Override
    public String getCommand() {
        return "!geoip";
    }

    @Override
    public String getHelp() {
        return "Looks up an IP address or hostname from GeoIP2 database";
    }

    @Override
    public String handle(String sender, String recipient, String message, IrcConnector ircConnector) {
        if (!message.startsWith("!geoip ")) {
            return null;
        }
        try {
            InetAddress ipAddress = InetAddress.getByName(message.substring(7));
            CityResponse response = databaseReader.city(ipAddress);

            Country country = response.getCountry();
            City city = response.getCity();
            Postal postal = response.getPostal();
            Location location = response.getLocation();
            StringBuilder sb = new StringBuilder();

            if (city != null) {
                sb.append(city.getName()).append(", ");
            }
            if (postal != null) {
                sb.append(postal.getCode()).append(", ");
            }
            if (country != null) {
                sb.append(country.getName()).append(", ");
            }
            if (location != null) {
                sb.append("Lat: ").append(location.getLatitude()).append(" Lon: ").append(location.getLongitude());
            } else if (", ".equals(sb.substring(sb.length() - 2, sb.length()))) {
                sb.delete(sb.length() - 2, sb.length());
            }

            return sb.length() > 0 ? sb.toString() : null;
        } catch (UnknownHostException ex) {
            return "Unknown host";
        } catch (IOException ex) {
            return "Lookup failed";
        } catch (GeoIp2Exception ex) {
            return "Lookup failed: " + ex.getMessage();
        }
    }
}
