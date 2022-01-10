package fi.derpnet.derpbot.connector;

import fi.derpnet.derpbot.bean.Message;
import fi.derpnet.derpbot.bean.RawMessage;
import fi.derpnet.derpbot.controller.MainController;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.Socket;
import java.util.List;
import java.util.Timer;
import javax.net.ssl.SSLSocketFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IrcConnector implements Connector {

    public static final int PING_INTERVAL_MS = 60_000;
    public static final int PING_TIMEOUT_MS = 30_000;
    public static final int WATCHER_POLLRATE_MS = 5_000;

    public final String networkName;
    public final String hostname;
    public final int port;
    public final boolean ssl;
    public final String user;
    public final String realname;
    public final int ratelimit;

    private static final Logger LOG = LogManager.getLogger(IrcConnector.class);

    private final MainController controller;
    private final Timer connectionWatcherTimer;

    private String nick;
    private Socket socket;
    private ReceiverThread receiverThread;
    private SenderThread senderThread;
    private ConnectionWatcher connectionWatcher;
    private List<String> channels;
    private List<String> quieterChannels;

    public IrcConnector(String networkName, String hostname, int port, boolean ssl, String user, String realname, String nick, int ratelimit, MainController controller) {
        this.networkName = networkName;
        this.hostname = hostname;
        this.port = port;
        this.ssl = ssl;
        this.user = user;
        this.realname = realname;
        this.nick = nick;
        this.ratelimit = ratelimit;
        this.controller = controller;
        connectionWatcherTimer = new Timer();
    }

    public void connect() throws IOException {
        boolean retry;
        BufferedWriter writer = null;
        BufferedReader reader = null;

        do {
            try {
                retry = false;
                LOG.info("Connecting to " + networkName + " on " + hostname + " port " + port + (ssl ? " using SSL" : " without SSL"));
                if (ssl) {
                    socket = SSLSocketFactory.getDefault().createSocket(hostname, port);
                } else {
                    socket = new Socket(hostname, port);
                }
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                writer.write("NICK " + nick + "\r\n");
                writer.write("USER " + user + " 8 * :" + realname + "\r\n");
                writer.flush();

                String line;
                String pendingNick = nick;
                while ((line = reader.readLine()) != null) {
                    System.out.println(Thread.currentThread().getId() + " << " + line);
                    if (line.contains("004")) {
                        // We are now logged in.
                        break;
                    } else if (line.contains("433")) {
                        //TODO prettier alt nick generation
                        pendingNick = pendingNick + "_";
                        writer.write("NICK " + pendingNick + "\r\n");
                        writer.flush();
                    } else if (line.toUpperCase().startsWith("PING ")) {
                        writer.write("PONG :" + line.substring(6) + "\r\n");
                        writer.flush();
                    }
                }

                if (!nick.equals(pendingNick)) {
                    nick = pendingNick;
                }
            } catch (IOException ex) {
                LOG.error("Got an IOExcpetion while trying to connect, trying again after a while", ex);
                try {
                    Thread.sleep(PING_TIMEOUT_MS);
                } catch (InterruptedException e) {
                }
                retry = true;
            }
        } while (retry);

        UncaughtExceptionHandler exceptionHandler = (thread, throwable) -> LOG.error(thread.getClass().getSimpleName() + " in network " + networkName + " exited with uncaught exception", throwable);

        senderThread = new SenderThread(writer, ratelimit);
        connectionWatcher = new ConnectionWatcher(senderThread, this::handleConnectionLoss);
        receiverThread = new ReceiverThread(reader, senderThread, connectionWatcher::gotMessage, this::handleConnectionLoss, msg -> controller.handleIncoming(this, msg));

        senderThread.setUncaughtExceptionHandler(exceptionHandler);
        receiverThread.setUncaughtExceptionHandler(exceptionHandler);

        senderThread.start();
        receiverThread.start();

        connectionWatcherTimer.schedule(connectionWatcher, 10000, PING_INTERVAL_MS);
    }

    public void disconnect() {
        connectionWatcher.cancel();
        connectionWatcherTimer.purge();
        try {
            socket.close();
            LOG.info("Disconnected from " + hostname);
        } catch (IOException ex) {
            LOG.error("Failed to disconnect from " + hostname + ", this connector may be in an inconsistent state!", ex);
        }
    }

    public void send(Message msg) {
        senderThread.send(msg);
    }

    public void setChannels(List<String> channels, boolean join) {
        this.channels = channels;
        if (join) {
            channels.forEach(channel -> {
                LOG.info("Joining channel " + channel + " on " + networkName);
                senderThread.send(new RawMessage(null, "JOIN", channel));
            });
        }
    }

    public List<String> getQuieterChannels() {
        return quieterChannels;
    }

    public void setQuieterChannels(List<String> quieterChannels) {
        this.quieterChannels = quieterChannels;
    }

    private void handleConnectionLoss() {
        LOG.warn("Lost connection to " + hostname + ", reconnecting...");
        disconnect();
        try {
            connect();
            channels.forEach(channel -> {
                LOG.info("Joining channel " + channel + " on " + networkName + " after a reconnect");
                senderThread.send(new RawMessage(null, "JOIN", channel));
            });
        } catch (IOException ex) {
            LOG.error("Failed to reconnect to " + hostname + ", this connector may be in an inconsistent state!", ex);
        }
    }
}
