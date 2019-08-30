package fi.derpnet.derpbot.handler.impl;

import com.google.gson.Gson;
import fi.derpnet.derpbot.bean.passpredictor.Pass;
import fi.derpnet.derpbot.connector.IrcConnector;
import fi.derpnet.derpbot.controller.MainController;
import fi.derpnet.derpbot.handler.SimpleMultiLineMessageHandler;
import fi.derpnet.derpbot.util.IrcFormatter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class PassPredictor implements SimpleMultiLineMessageHandler {

    private static final Logger LOG = Logger.getLogger(PassPredictor.class);

    private static final String API_URL = "https://passpredictor.24-7.fi/next";

    private static final Function<Long, String> DATE_FORMAT = time -> new SimpleDateFormat("HH:mm").format(new Date(time * 1000));
    private static final Function<Long, String> HZ_FORMAT = hz -> hz > 1_000_000 ? hz / 1_000_000d + "Mhz" : hz / 1_000d + "kHz";
    private static final Function<Long, String> DURATION_FORMAT = seconds -> seconds < 60 ? seconds + " secs" : seconds / 60 + " mins " + seconds % 60 + " secs";
    private static final Function<Long, String> REMAINING_FORMAT = seconds -> seconds < 3600 ? seconds / 60 + " mins" : seconds / 3600 + " hours " + seconds % 3600 / 60 + " mins";
    private static final Function<Pass, String> FORMATTER = pass -> String.format("%s at %s (in %s) pass length %s (ends at %s) with max elevation %s (freq: %s bw: %s)",
            IrcFormatter.bold(pass.getSatellite()),
            IrcFormatter.bold(DATE_FORMAT.apply(pass.getBegin())),
            IrcFormatter.bold(REMAINING_FORMAT.apply(pass.getBegin() - System.currentTimeMillis() / 1000)),
            IrcFormatter.bold(DURATION_FORMAT.apply(pass.getEnd() - pass.getBegin())),
            IrcFormatter.bold(DATE_FORMAT.apply(pass.getEnd())),
            IrcFormatter.bold(pass.getMaxElev() + "º"),
            IrcFormatter.bold(HZ_FORMAT.apply(pass.getFrequency())),
            IrcFormatter.bold(HZ_FORMAT.apply(pass.getBandwidth())));

    @Override
    public void init(MainController controller) {
    }

    @Override
    public String getCommand() {
        return "!pass";
    }

    @Override
    public String getHelp() {
        return "Show next satellite passes";
    }

    @Override
    public boolean isLoud() {
        return true;
    }

    @Override
    public List<String> handle(String sender, String recipient, String message, IrcConnector ircConnector) {
        if (!message.equals("!pass")) {
            return null;
        }
        try (InputStream is = new URL(API_URL).openStream()) {
            Pass[] passes = new Gson().fromJson(IOUtils.toString(is, "UTF-8"), Pass[].class);
            if (passes == null) {
                return Collections.singletonList("No response from api");
            }
            return Arrays.stream(passes).sorted((a, b) -> a.getBegin().compareTo(b.getBegin())).map(FORMATTER).collect(Collectors.toList());
        } catch (IOException ex) {
            LOG.warn("Can't get passes!", ex);
            return Collections.singletonList(ex.getClass().getSimpleName() + " -- Check logs");
        }
    }
}
