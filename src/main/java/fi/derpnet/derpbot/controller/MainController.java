package fi.derpnet.derpbot.controller;

import fi.derpnet.derpbot.bean.RawMessage;
import fi.derpnet.derpbot.connector.IrcConnector;
import fi.derpnet.derpbot.handler.HandlerRegistry;
import fi.derpnet.derpbot.adapter.SimpleMessageAdapter;
import fi.derpnet.derpbot.handler.SimpleMessageHandler;
import fi.derpnet.derpbot.adapter.SimpleMultiLineMessageAdapter;
import fi.derpnet.derpbot.bean.Message;
import fi.derpnet.derpbot.connector.Connector;
import fi.derpnet.derpbot.connector.MatrixConnector;
import fi.derpnet.derpbot.handler.GenericHandler;
import fi.derpnet.derpbot.httpapi.HttpApiDaemon;
import fi.derpnet.derpbot.handler.SimpleMultiLineMessageHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.log4j.Logger;
import fi.derpnet.derpbot.handler.MessageHandler;

public class MainController {

    private static final Logger LOG = Logger.getLogger(MainController.class);
    private List<MessageHandler> messageHandlers;
    private Map<String, String> config;
    private List<Connector> connectors;
    private HttpApiDaemon httpApi;

    public void start() {
        httpApi = new HttpApiDaemon();
        loadConfiguration();
        loadHandlers();
        connectToServers();
        httpApi.init(this);

        LOG.info("Ready");
        Scanner s = new Scanner(System.in);
        while (true) {
            String line = s.nextLine();
            if (line.equals("exit")) {
                break;
            }
        }

        LOG.info("Disconnecting from IRC servers");
        connectors.forEach(Connector::disconnect);
    }

    private void loadConfiguration() {
        LOG.info("Loading configuration");
        try {
            config = new HashMap<>();
            File jarLocation = new File(MainController.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile();
            File[] configFiles = jarLocation.listFiles(f -> f.isFile() && f.getName().equals("derpbot.properties"));
            if (configFiles == null || configFiles.length == 0) {
                // look for config files in the parent directory if none found in the current directory, this is useful during development when
                // DerpBot can be run from maven target directory directly while the config file sits in the project root
                configFiles = jarLocation.getParentFile().listFiles(f -> f.isFile() && f.getName().equals("derpbot.properties"));
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
    }

    private void loadHandlers() {
        LOG.info("Loading handlers");
        messageHandlers = new LinkedList<>();
        HandlerRegistry.handlers.forEach(c -> {
            LOG.info("Registering message handler " + c.getSimpleName());
            try {
                messageHandlers.add(c.newInstance().getRawMessageHandler());
            } catch (InstantiationException | IllegalAccessException ex) {
                LOG.error("Error initializing handler " + c.getName(), ex);
            }
        });

        LOG.info("Initializing handlers");
        messageHandlers.forEach(h -> h.init(this));
    }

    private void connectToServers() {
        LOG.info("Connecting to IRC servers");
        connectors = new LinkedList<>();
        Set<Entry<String, String>> networkEntries = config.entrySet().stream().filter(e -> e.getKey().startsWith("network.") && e.getKey().endsWith(".host")).collect(Collectors.toSet());
        for (Entry<String, String> entry : networkEntries) {
            String networkName = entry.getKey().substring(entry.getKey().indexOf('.') + 1, entry.getKey().length() - 5); // +1 because we don't want the dot, and strip the .host from the end
            String[] entrySplit = entry.getValue().split(":");
            String hostname;
            int port = 0;
            boolean ssl;
            String nick = config.get("network." + networkName + ".nick");
            if (nick == null) {
                nick = config.get("default.nick");
                if (nick == null) {
                    nick = "DerpBot";
                }
            }
            String user = config.get("network." + networkName + ".user");
            if (user == null) {
                user = config.get("default.user");
                if (user == null) {
                    user = "DerpBot";
                }
            }
            int ratelimit;
            try {
                ratelimit = Integer.parseInt(config.get("network." + networkName + ".ratelimit"));
            } catch (NullPointerException | NumberFormatException ex) {
                ratelimit = 2500;
            }
            Connector connector;
            String protocol = config.get("network." + networkName + ".protocol");
            switch (protocol) {
                case "matrix":
                    hostname = entry.getValue();
                    String login = config.get("network." + networkName + ".login");
                    String password = config.get("network." + networkName + ".password");
                    connector = new MatrixConnector(hostname, login, password, user, this);
                    break;
                
                default:
                case "irc":
                    if (entrySplit.length == 3 && entrySplit[0].equalsIgnoreCase("ssl")) {
                        hostname = entrySplit[1];
                        port = Integer.parseInt(entrySplit[2]);
                        ssl = true;
                    } else {
                        hostname = entrySplit[0];
                        port = Integer.parseInt(entrySplit[1]);
                        ssl = false;
                    }
                    String realname = config.get("network." + networkName + ".realname");
                    if (realname == null) {
                        realname = config.get("default.realname");
                        if (realname == null) {
                            realname = "DerpBot";
                        }
                    }
                    
                    connector = new IrcConnector(networkName, hostname, port, ssl, user, realname, nick, ratelimit, this);
            }
            connectors.add(connector);
            try {
                connector.connect();
            } catch (IOException ex) {
                LOG.error("Failed to connect to " + networkName + " (host: " + hostname + " Port: " + port + ")", ex);
                continue;
            }
            String channels = config.get("network." + networkName + ".channels");
            if (channels != null) {
                connector.setChannels(Arrays.asList(channels.split(",")), true);
            }
            String quieterChannels = config.get("network." + networkName + ".channels.quieter");
            if (quieterChannels != null) {
                connector.setQuieterChannels(Arrays.asList(quieterChannels.split(",")).stream().map(String::toLowerCase).collect(Collectors.toList()));
            }
        }
    }

    public List<Message> handleIncoming(Connector origin, Message message) {
        Stream<List<Message>> handledStream = messageHandlers.stream().map((h) -> {
            try {
                return h.handle(message, origin);
            } catch (Exception ex) {
                LOG.warn("Handler " + h.getClass().getName() + " threw an exception while handling a message", ex);
                return null;
            }
        });
        return handledStream.filter(Objects::nonNull).flatMap(List::stream).collect(Collectors.toList());
    }

    public List<MessageHandler> getRawMessageHandlers() {
        return messageHandlers;
    }

    public Map<String, String> getConfig() {
        return config;
    }

    public List<Connector> getConnectors() {
        return connectors;
    }
}
