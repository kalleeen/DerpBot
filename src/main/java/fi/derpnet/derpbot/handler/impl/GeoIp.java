package fi.derpnet.derpbot.handler.impl;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Location;
import com.maxmind.geoip2.record.Postal;
import com.maxmind.geoip2.record.RepresentedCountry;
import com.maxmind.geoip2.record.Traits;
import fi.derpnet.derpbot.connector.IrcConnector;
import fi.derpnet.derpbot.controller.MainController;
import fi.derpnet.derpbot.handler.SimpleMessageHandler;
import fi.derpnet.derpbot.util.CommandUtils;
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
            String lookup = CommandUtils.getFirstParameter(message);
            InetAddress ipAddress = InetAddress.getByName(lookup);
            if (ipAddress.isSiteLocalAddress()) {
                return "Local address";
            }
            String hostname = ipAddress.getCanonicalHostName();
            String ip = ipAddress.getHostAddress();
            CityResponse response = databaseReader.city(ipAddress);

            Country country = response.getCountry();
            City city = response.getCity();
            Postal postal = response.getPostal();
            Location location = response.getLocation();
            RepresentedCountry representedCountry = response.getRepresentedCountry();
            Traits traits = response.getTraits();

            StringBuilder sb = new StringBuilder();
            if (hostname != null && !hostname.equals(ip)) {
                sb.append(hostname).append(" (").append(ip).append(") = ");
            } else {
                sb.append(ip).append(" = ");
            }

            if (city != null && city.getName() != null) {
                sb.append(city.getName()).append(", ");
            }
            if (postal != null && postal.getCode() != null) {
                sb.append(postal.getCode()).append(", ");
            }
            if (country != null && country.getName() != null) {
                sb.append(country.getName()).append(", ");
            }
            if (location != null) {
                sb.append("Lat: ").append(location.getLatitude()).append(" Lon: ").append(location.getLongitude()).append(" Accuracy radius: ").append(location.getAccuracyRadius()).append("km");
            } else if (", ".equals(sb.substring(sb.length() - 2, sb.length()))) {
                sb.delete(sb.length() - 2, sb.length());
            }
            if (representedCountry != null && representedCountry.getType() != null) {
                sb.append(" (Type: ").append(representedCountry.getType()).append(')');
            }
            if (traits != null) {
                if (traits.getAutonomousSystemNumber() != null && traits.getAutonomousSystemOrganization() != null) {
                    sb.append(" ASN: ").append(traits.getAutonomousSystemNumber()).append(" (").append(traits.getAutonomousSystemOrganization()).append(')');
                }
                if (traits.getIsp() != null) {
                    sb.append(" ISP: ").append(traits.getIsp());
                }
                if (traits.getOrganization() != null) {
                    sb.append(" Organization: ").append(traits.getOrganization());
                }
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
