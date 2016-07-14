package fi.derpnet.derpbot.handler.impl;

import fi.derpnet.derpbot.connector.IrcConnector;
import fi.derpnet.derpbot.constants.AsciiFormatting;
import fi.derpnet.derpbot.controller.MainController;
import fi.derpnet.derpbot.handler.SimpleMultiLineMessageHandler;
import fi.derpnet.derpbot.util.CommandUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class Poikkeusinfo implements SimpleMultiLineMessageHandler {

    public static final String API_URL = "http://www.poikkeusinfo.fi/xml/v2/fi";
    private SAXParser parser;

    @Override
    public void init(MainController controller) {
        try {
            SAXParserFactory parserFactor = SAXParserFactory.newInstance();
            parser = parserFactor.newSAXParser();
        } catch (ParserConfigurationException | SAXException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String getCommand() {
        return "!poikkeusinfo";
    }

    @Override
    public String getHelp() {
        return "HSL Poikkeusinfo";
    }

    @Override
    public List<String> handle(String sender, String recipient, String message, IrcConnector ircConnector) {
        if (!message.startsWith("!poikkeusinfo")) {
            return null;
        }
        List<String> params = CommandUtils.getParameters(message);
        SAXHandler handler = new SAXHandler();
        try {
            if (params.contains("mock")) {
                handler.rows = createMockRows();
            } else {
                URL url = new URL(API_URL);
                try (InputStream is = url.openStream()) {
                    parser.parse(is, handler);
                }
            }
            if (handler.rows.isEmpty()) {
                return Collections.singletonList("Ei poikkeustiedotteita");
            } else if (params.contains("raw")) {
                return handler.rows;
            } else {
                return combineRows(new HashSet(handler.rows));
            }
        } catch (SAXException | IOException ex) {
            return Collections.singletonList("Error: " + ex.getMessage());
        }
    }

    private List<String> combineRows(Set<String> rows) {
        if (rows.size() == 1 && rows.iterator().next().equals("Ei poikkeusliikennetiedotteita.")) {
            return new LinkedList<>(rows);
        }
        Map<Pattern, Map<String, LineData>> groups = new HashMap<>();

        // Lähijuna (kirjain) (minne ja minne), klo **:** peruttu.(optional Syy tai muuta)
        Pattern lahijunaPattern = Pattern.compile("Lähijuna (?<destination>[^,]+), klo (?<time>[\\d:]{4,5}) peruttu.(?<extra>.*)");
        Map<String, LineData> lahijunaDestinations = new HashMap<>();
        groups.put(lahijunaPattern, lahijunaDestinations);

        // Helsingin sisäisen liikenteen linja (linja) (minne ja minne), klo **:** peruttu.(optional Syy tai muuta)
        Pattern helsinkiSisainenPattern = Pattern.compile("Helsingin sisäisen liikenteen linja (?<destination>[^,]+), klo (?<time>[\\d:]{4,5}) peruttu.(?<extra>.*)");
        Map<String, LineData> helsinkiSisainenDestinations = new HashMap<>();
        groups.put(helsinkiSisainenPattern, helsinkiSisainenDestinations);

        // Espoon sisäisen liikenteen linja (linja) (minne ja minne), klo **:** peruttu.(optional Syy tai muuta)
        Pattern espooSisainenPattern = Pattern.compile("Espoon sisäisen liikenteen linja (?<destination>[^,]+), klo (?<time>[\\d:]{4,5}) peruttu.(?<extra>.*)");
        Map<String, LineData> espooSisainenDestinations = new HashMap<>();
        groups.put(espooSisainenPattern, espooSisainenDestinations);

        // Vantaan sisäisen liikenteen linja (linja) (minne ja minne), klo **:** peruttu.(optional Syy tai muuta)
        Pattern vantaaSisainenPattern = Pattern.compile("Vantaan sisäisen liikenteen linja (?<destination>[^,]+), klo (?<time>[\\d:]{4,5}) peruttu.(?<extra>.*)");
        Map<String, LineData> vantaaSisainenDestinations = new HashMap<>();
        groups.put(vantaaSisainenPattern, vantaaSisainenDestinations);

        // Anything else
        List<String> ungrouped = new LinkedList<>();

        rows.forEach(l -> {
            Entry<Pattern, Map<String, LineData>> entry = groups.entrySet().stream().filter(e -> e.getKey().matcher(l).matches()).findAny().orElse(null);
            if (entry != null) {
                Matcher m = entry.getKey().matcher(l);
                if (m.matches()) {
                    String dest = m.group("destination");
                    LineData lineData = entry.getValue().remove(dest);
                    if (lineData == null) {
                        lineData = new LineData();
                    }
                    lineData.times.add(m.group("time"));
                    lineData.extras.add(m.group("extra"));
                    entry.getValue().put(dest, lineData);
                }
            } else {
                ungrouped.add(l);
            }
        });

        List<String> result = new LinkedList<>();
        lahijunaDestinations.forEach((destination, lineData) -> {
            result.add(formatLineData("Lähijuna %s, klo %s peruttu. %s", destination, lineData));
        });
        helsinkiSisainenDestinations.forEach((destination, lineData) -> {
            result.add(formatLineData("Helsingin sisäisen liikenteen linja %s, klo %s peruttu.%s", destination, lineData));
        });
        espooSisainenDestinations.forEach((destination, lineData) -> {
            result.add(formatLineData("Espoon sisäisen liikenteen linja %s, klo %s peruttu.%s", destination, lineData));
        });
        vantaaSisainenDestinations.forEach((destination, lineData) -> {
            result.add(formatLineData("Vantaan sisäisen liikenteen linja %s, klo %s peruttu.%s", destination, lineData));
        });
        ungrouped.forEach(l -> result.add(l + " " + AsciiFormatting.colorize("[ungrouped]", AsciiFormatting.GREY)));
        return result;
    }

    private String formatLineData(String formatString, String destination, LineData lineData) {
        String timesString = lineData.times.stream().collect(Collectors.joining(", "));
        String extrasString = lineData.extras.stream().collect(Collectors.joining(","));
        return String.format(formatString, AsciiFormatting.bold(destination), AsciiFormatting.bold(timesString), extrasString);
    }

    private class LineData {

        public Set<String> times;
        public Set<String> extras;

        public LineData() {
            this.times = new TreeSet<>((o1, o2) -> {
                String[] s1 = o1.split(":");
                String[] s2 = o2.split(":");
                if (s1.length != 2 || s2.length != 2) {
                    return Objects.compare(o1, o2, String.CASE_INSENSITIVE_ORDER);
                }
                try {
                    int h1 = Integer.parseInt(s1[0]);
                    int m1 = Integer.parseInt(s1[1]);
                    int h2 = Integer.parseInt(s2[0]);
                    int m2 = Integer.parseInt(s2[1]);
                    return h1 - h2 == 0 ? h1 - h2 : m1 - m2;
                } catch (NumberFormatException ex) {
                    return Objects.compare(o1, o2, String.CASE_INSENSITIVE_ORDER);
                }
            });
            this.extras = new HashSet<>();
        }
    }

    private class SAXHandler extends DefaultHandler {

        private String currentElement = null;
        private List<String> rows;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            currentElement = qName;
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            currentElement = null;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (currentElement != null && currentElement.equals("TEXT")) {
                rows.add(String.copyValueOf(ch, start, length).trim());
            }
        }

        @Override
        public void startDocument() {
            rows = new LinkedList<>();
        }

        @Override
        public void endDocument() {
        }
    }

    private List<String> createMockRows() {
        List<String> l = new LinkedList<>();
        l.add("Lähijuna P Helsinkiin, klo 12:30 peruttu.");
        l.add("Lähijuna I Helsinkiin, jokatoinen juna ajetaan takaperin Syy: junankääntäjät lakkoilevat");
        l.add("Lähijuna P Helsinkiin, klo 13:40 peruttu.");
        l.add("Espoon sisäisen liikenteen linja 123A Espoon takanurkkaan, klo 13:37 peruttu. Syy: kuski unohti tulla töihin");
        l.add("Espoon sisäisen liikenteen linja 123A Espoon takanurkkaan, klo 15:59 peruttu. Syy: bussi unohtui tankata");
        l.add("Lentävä matto Helsingistä Suomeen klo 25:79 on peruttu.");
        return l;
    }
}
