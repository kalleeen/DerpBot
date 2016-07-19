package fi.derpnet.derpbot.handler.impl;

import com.google.gson.Gson;
import fi.derpnet.derpbot.bean.pvm.Liputuspaiva;
import fi.derpnet.derpbot.bean.pvm.Nimipaiva;
import fi.derpnet.derpbot.bean.pvm.Pyha;
import fi.derpnet.derpbot.connector.IrcConnector;
import fi.derpnet.derpbot.constants.AsciiFormatting;
import fi.derpnet.derpbot.controller.MainController;
import fi.derpnet.derpbot.handler.SimpleMessageHandler;
import fi.derpnet.derpbot.util.IrcFormatter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;

public class Pvm implements SimpleMessageHandler {

    private static final String NIMIPAIVA_URL = "http://www.webcal.fi/cal.php?id=4&format=json&start_year=current_year&end_year=current_year&tz=Europe%2FHelsinki";
    private static final String PYHA_URL = "http://www.webcal.fi/cal.php?id=1&format=json&start_year=current_year&end_year=current_year&tz=Europe%2FHelsinki";
    private static final String LIPUTUSPAIVA_URL = "http://www.webcal.fi/cal.php?id=2&format=json&start_year=current_year&end_year=current_year&tz=Europe%2FHelsinki";

    private static final Logger LOG = Logger.getLogger(Pvm.class);
    private static final DateFormat apiDf = new SimpleDateFormat("yy-MM-dd");
    private static final DateFormat printDf = new SimpleDateFormat("E dd.MM.yyyy", new Locale("fi", "FI"));

    /**
     * Key is the request URL, the Value is an entry whose Key is the date when
     * the entry was fetched and the Value is the data from the URL
     */
    private final Map<String, Entry<Date, String>> cache;

    public Pvm() {
        cache = new ConcurrentHashMap<>();
    }

    @Override
    public void init(MainController controller) {
    }

    @Override
    public String getCommand() {
        return "!pvm";
    }

    @Override
    public String getHelp() {
        return "Kertoo päivämäärän, nimipäivät, liputuspäivät, ym";
    }

    @Override
    public String handle(String sender, String recipient, String message, IrcConnector ircConnector) {
        if (!message.startsWith("!pvm")) {
            return null;
        }
        StringBuilder sb = new StringBuilder(IrcFormatter.bold(printDf.format(new Date())));

        Pyha pyha = getPyha();
        if (pyha != null) {
            sb.append(", Pyhä: ").append(IrcFormatter.bold(pyha.getName()));
            if (pyha.getAge() != null) {
                sb.append(IrcFormatter.colorize(" (" + pyha.getAge() + " vuotta)", AsciiFormatting.GREY));
            }
        }

        Liputuspaiva liputuspaiva = getLiputuspaiva();
        if (liputuspaiva != null) {
            sb.append(", Liputuspäivä: ").append(IrcFormatter.bold(liputuspaiva.getName()));
            if (liputuspaiva.getAge() != null) {
                sb.append(IrcFormatter.colorize(" (" + liputuspaiva.getAge() + " vuotta)", AsciiFormatting.GREY));
            }
        }

        Nimipaiva nimipaiva = getNimipaiva();
        if (nimipaiva != null) {
            sb.append(", Nimipäivää viettää ").append(IrcFormatter.bold(nimipaiva.getName())).append('.');
        }

        return sb.toString();
    }

    private String getApiData(String url) {
        Entry<Date, String> cacheEntry = cache.get(url);
        if (cacheEntry != null) {
            if (DateUtils.isSameDay(cacheEntry.getKey(), new Date())) {
                return cacheEntry.getValue();
            } else {
                cache.remove(url);
            }
        }
        try (BufferedReader rd = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
            String result = rd.lines().collect(Collectors.joining("\n"));
            if (result != null && !result.trim().isEmpty()) {
                cache.put(url, new SimpleEntry(new Date(), result));
                return result;
            }
        } catch (IOException ex) {
            LOG.error("Failed to fetch data from: " + url, ex);
        }
        return null;
    }

    private Nimipaiva getNimipaiva() {
        String data = getApiData(NIMIPAIVA_URL);
        Nimipaiva[] arr = new Gson().fromJson(data, Nimipaiva[].class);
        if (arr == null) {
            return null;
        }
        for (Nimipaiva x : arr) {
            try {
                if (DateUtils.isSameDay(apiDf.parse(x.getDate()), new Date())) {
                    return x;
                }
            } catch (ParseException ex) {
                LOG.error("Failed to parse date from \"" + x.getDate() + "\"", ex);
            }
        }
        return null;
    }

    private Pyha getPyha() {
        String data = getApiData(PYHA_URL);
        Pyha[] arr = new Gson().fromJson(data, Pyha[].class);
        if (arr == null) {
            return null;
        }
        for (Pyha x : arr) {
            try {
                if (DateUtils.isSameDay(apiDf.parse(x.getDate()), new Date())) {
                    return x;
                }
            } catch (ParseException ex) {
                LOG.error("Failed to parse date from \"" + x.getDate() + "\"", ex);
            }
        }
        return null;
    }

    private Liputuspaiva getLiputuspaiva() {
        String data = getApiData(LIPUTUSPAIVA_URL);
        Liputuspaiva[] arr = new Gson().fromJson(data, Liputuspaiva[].class);
        if (arr == null) {
            return null;
        }
        for (Liputuspaiva x : arr) {
            try {
                if (DateUtils.isSameDay(apiDf.parse(x.getDate()), new Date())) {
                    return x;
                }
            } catch (ParseException ex) {
                LOG.error("Failed to parse date from \"" + x.getDate() + "\"", ex);
            }
        }
        return null;
    }
}
