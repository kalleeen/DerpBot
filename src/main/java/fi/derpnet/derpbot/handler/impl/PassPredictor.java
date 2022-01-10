package fi.derpnet.derpbot.handler.impl;

import com.google.gson.Gson;
import fi.derpnet.derpbot.bean.passpredictor.Pass;
import fi.derpnet.derpbot.connector.Connector;
import fi.derpnet.derpbot.controller.MainController;
import fi.derpnet.derpbot.handler.SimpleMultiLineMessageHandler;
import fi.derpnet.derpbot.util.IrcSafeHtmlFormatter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PassPredictor implements SimpleMultiLineMessageHandler {

    private static final Logger LOG = LogManager.getLogger(PassPredictor.class);

    private static final String API_BASE_URL = "https://passpredictor.24-7.fi";
    private static final String API_URL_NEXT = API_BASE_URL + "/next";
    private static final String API_URL_24H = API_BASE_URL + "/24h";
    private static final String API_URL_PAST_24H = API_BASE_URL + "/-24h";

    private static final long ALL_COOLDOWN_MS = 600_000;

    private static final Function<Long, String> DATE_FORMAT = time -> new SimpleDateFormat("HH:mm").format(new Date(time * 1000));
    private static final Function<Long, String> HZ_FORMAT = hz -> hz > 1_000_000 ? hz / 1_000_000d + "Mhz" : hz / 1_000d + "kHz";
    private static final Function<Long, String> DURATION_FORMAT = seconds -> seconds < 60 ? seconds + " secs" : seconds / 60 + " mins " + seconds % 60 + " secs";
    private static final Function<Long, String> REMAINING_FORMAT = seconds -> seconds < 3600 ? seconds / 60 + " mins" : seconds / 3600 + " hours " + seconds % 3600 / 60 + " mins";
    private static final Function<Pass, String> FORMATTER = pass -> String.format("%s at %s (in %s) pass length %s (ends at %s) with max elevation %s (freq: %s bw: %s)",
            IrcSafeHtmlFormatter.bold(pass.getSatellite()),
            IrcSafeHtmlFormatter.bold(DATE_FORMAT.apply(pass.getBegin())),
            IrcSafeHtmlFormatter.bold(REMAINING_FORMAT.apply(pass.getBegin() - System.currentTimeMillis() / 1000)),
            IrcSafeHtmlFormatter.bold(DURATION_FORMAT.apply(pass.getEnd() - pass.getBegin())),
            IrcSafeHtmlFormatter.bold(DATE_FORMAT.apply(pass.getEnd())),
            IrcSafeHtmlFormatter.bold(pass.getMaxElev() + "º"),
            IrcSafeHtmlFormatter.bold(HZ_FORMAT.apply(pass.getFrequency())),
            IrcSafeHtmlFormatter.bold(HZ_FORMAT.apply(pass.getBandwidth())));

    private long lastAll = 0;

    @Override
    public void init(MainController controller) {
    }

    @Override
    public String getCommand() {
        return "!pass";
    }

    @Override
    public String getHelp() {
        return "Show next satellite passes. Valid parameters are 'next', 'past', 'highest', 'all' or satellite name";
    }

    @Override
    public boolean isLoud() {
        return true;
    }

    @Override
    public List<String> handle(String sender, String recipient, String message, Connector connector) {
        if (!message.startsWith("!pass")) {
            return null;
        }
        String param = message.length() > "!pass ".length() ? message.substring("!pass ".length()) : "next";
        try {
            List<String> msgs = getPasses(param).sorted(Comparator.comparingLong(Pass::getBegin)).map(FORMATTER).collect(Collectors.toList());
            if (msgs.isEmpty()) {
                return Collections.singletonList("Invalid parameter: " + param);
            } else {
                return msgs;
            }
        } catch (RuntimeException ex) {
            LOG.warn("Can't get passes", ex);
            return Collections.singletonList(ex.getClass().getSimpleName());
        }
    }

    private Stream<Pass> getPasses(String param) {
        switch (param) {
            case "next":
                return getPassesFromApi(API_URL_NEXT);
            case "past":
                return getPassesFromApi(API_URL_PAST_24H);
            case "all":
                if (System.currentTimeMillis() > lastAll + ALL_COOLDOWN_MS) {
                    lastAll = System.currentTimeMillis();
                    return getPassesFromApi(API_URL_24H);
                } else {
                    return Stream.empty();
                }
            case "highest":
                Map<String, List<Pass>> bySatellite = getPassesFromApi(API_URL_24H).collect(Collectors.groupingBy(Pass::getSatellite));
                Function<List<Pass>, Pass> findMaxElevation = l -> l.stream().max(Comparator.comparingInt(Pass::getMaxElev)).orElse(null);
                return bySatellite.values().stream().map(findMaxElevation);
            default:
                return getPassesFromApi(API_URL_24H).filter(p -> param.equals(p.getSatellite()));
        }
    }

    private Stream<Pass> getPassesFromApi(String url) {
        try (InputStream is = new URL(url).openStream()) {
            Pass[] passes = new Gson().fromJson(IOUtils.toString(is, "UTF-8"), Pass[].class);
            if (passes == null) {
                return Stream.empty();
            }
            return Arrays.stream(passes);
        } catch (IOException ex) {
            LOG.warn("Can't get passes!", ex);
            throw new IllegalStateException("Can't get passes", ex);
        }
    }
}
