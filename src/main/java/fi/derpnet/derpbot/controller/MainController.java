package fi.derpnet.derpbot.controller;

import fi.derpnet.derpbot.bean.RawMessage;
import fi.derpnet.derpbot.connector.IrcConnector;
import fi.derpnet.derpbot.handler.HandlerRegistry;
import fi.derpnet.derpbot.handler.SimpleMessageAdapter;
import fi.derpnet.derpbot.handler.RawMessageHandler;
import fi.derpnet.derpbot.handler.SimpleMessageHandler;
import fi.derpnet.derpbot.handler.SimpleMultiLineMessageAdapter;
import fi.derpnet.derpbot.handler.SimpleMultiLineMessageHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;

public class MainController {

    private static final Logger LOG = Logger.getLogger(MainController.class);
    private List<RawMessageHandler> rawMessageHandlers;
    private Map<String, String> config;
    private List<IrcConnector> ircConnectors;

    public void start() {
        LOG.info("Loading configuration");
        try {
            config = new HashMap<>();
            File jarLocation = new File(MainController.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile();
            File[] configFiles = jarLocation.listFiles((f) -> f.isFile() && f.getName().equals("derpbot.properties"));
            if (configFiles == null || configFiles.length == 0) {
                // look for config files in the parent directory if none found in the current directory, this is useful during development when
                // DerpBot can be run from maven target directory directly while the config file sits in the project root
                configFiles = jarLocation.getParentFile().listFiles((f) -> f.isFile() && f.getName().equals("derpbot.properties"));
            }
            if (configFiles == null || configFiles.length == 0) {
                System.err.println("No config file found!");
                LOG.error("No config file found!");
                System.exit(1);
            }
            LOG.debug("Config: " + configFiles[0]);
            Properties props = new Properties();
            props.load(new FileInputStream(configFiles[0]));
            Enumeration<?> e = props.propertyNames();
            while (e.hasMoreElements()) {
                String key = (String) e.nextElement();
                String value = props.getProperty(key);
                config.put(key, value);
                LOG.debug(key + '=' + value);
            }
        } catch (URISyntaxException | IOException ex) {
            LOG.error("Failed to read configuration!", ex);
        }

        LOG.info("Loading handlers");
        rawMessageHandlers = new LinkedList<>();
        HandlerRegistry.handlers.stream().forEach((Class c) -> {
            LOG.info("Registering message handler " + c.getSimpleName());
            if (RawMessageHandler.class.isAssignableFrom(c)) {
                try {
                    rawMessageHandlers.add((RawMessageHandler) c.newInstance());
                } catch (InstantiationException | IllegalAccessException ex) {
                    LOG.error("Error initializing handler " + c.getName(), ex);
                }
            } else if (SimpleMultiLineMessageHandler.class.isAssignableFrom(c)) {
                try {
                    rawMessageHandlers.add(new SimpleMultiLineMessageAdapter((SimpleMultiLineMessageHandler) c.newInstance()));
                } catch (InstantiationException | IllegalAccessException ex) {
                    LOG.error("Error initializing handler " + c.getName(), ex);
                }
            } else if (SimpleMessageHandler.class.isAssignableFrom(c)) {
                try {
                    rawMessageHandlers.add(new SimpleMessageAdapter((SimpleMessageHandler) c.newInstance()));
                } catch (InstantiationException | IllegalAccessException ex) {
                    LOG.error("Error initializing handler " + c.getName(), ex);
                }
            } else {
                LOG.error("Handler registry contains class " + c.getName() + " which does not implement a supported interface");
            }
        });

        LOG.info("Initializing handlers");
        rawMessageHandlers.forEach(h -> h.init(this));

        LOG.info("Connecting to IRC servers");
        ircConnectors = new LinkedList<>();
        Set<Entry<String, String>> networkEntries = config.entrySet().stream().filter(e -> e.getKey().startsWith("network.")).collect(Collectors.toSet());
        for (Entry<String, String> entry : networkEntries) {
            String networkName = entry.getKey().substring(entry.getKey().indexOf('.') + 1); // +1 because we don't want the dot
            String[] entrySplit = entry.getValue().split(":");
            String hostname = entrySplit[0];
            int port = Integer.parseInt(entrySplit[1]);
            // TODO: configurable nick, username and realname
            IrcConnector connector = new IrcConnector(networkName, hostname, port, "DerpBot", "DerpBot", "DerpBot", this);
            ircConnectors.add(connector);
            try {
                connector.connect();

            } catch (IOException ex) {
                LOG.error("Failed to connect to " + networkName + " (host: " + hostname + " Port: " + port + ")", ex);
                continue;
            }
            String channels = config.entrySet().stream().filter(e -> e.getKey().equals("channels." + networkName)).map(e -> e.getValue()).findAny().orElse(null);
            if (channels != null) {
                String[] channelss = channels.split(",");
                for (String channel : channelss) {
                    connector.join(channel);
                }
            }
        }

        LOG.info("Ready");
        Scanner s = new Scanner(System.in);
        while (true) {
            String line = s.nextLine();
            if (line.equals("exit")) {
                break;
            }
        }

        LOG.info("Disconnecting from IRC servers");
        //TODO implement
    }

    public List<RawMessage> handleIncoming(IrcConnector origin, RawMessage message) {
        return rawMessageHandlers.stream().map(h -> h.handle(message, origin)).filter(l -> l != null).flatMap(l -> l.stream()).collect(Collectors.toList());
    }
}
