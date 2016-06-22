package fi.derpnet.derpbot.connector;

import fi.derpnet.derpbot.bean.RawMessage;
import fi.derpnet.derpbot.controller.MainController;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.net.ssl.SSLSocketFactory;
import org.apache.log4j.Logger;

public class IrcConnector {

    public static final int SEND_DELAY_MS = 2500;

    public final String networkName;
    public final String hostname;
    public final int port;
    public final boolean ssl;
    public final String user;
    public final String realname;

    private static final Logger LOG = Logger.getLogger(IrcConnector.class);

    private final MainController controller;
    private String nick;
    private ReceiverThread receiverThread;
    private SenderThread senderThread;

    public IrcConnector(String networkName, String hostname, int port, boolean ssl, String user, String realname, String nick, MainController controller) {
        this.networkName = networkName;
        this.hostname = hostname;
        this.port = port;
        this.ssl = ssl;
        this.user = user;
        this.realname = realname;
        this.nick = nick;
        this.controller = controller;
    }

    public void connect() throws IOException {
        LOG.info("Connecting to " + networkName + " on " + hostname + " port " + port + (ssl ? "using SSL" : "without SSL"));
        Socket socket;
        if (ssl) {
            socket = SSLSocketFactory.getDefault().createSocket(hostname, port);
        } else {
            socket = new Socket(hostname, port);
        }
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        writer.write("NICK " + nick + "\r\n");
        writer.write("USER " + user + " 8 * :" + realname + "\r\n");
        writer.flush();

        String line;
        String pendingNick = nick;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
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

        receiverThread = new ReceiverThread(reader);
        receiverThread.start();
        senderThread = new SenderThread(writer);
        senderThread.start();
    }

    public void join(String channel) {
        LOG.info("Joining channel " + channel + " on " + networkName);
        senderThread.messageQueue.add(new RawMessage(null, "JOIN", channel));
    }

    private class ReceiverThread extends Thread {

        private final BufferedReader reader;

        public ReceiverThread(BufferedReader reader) {
            this.reader = reader;
        }

        @Override
        public void run() {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                    if (line.toUpperCase().startsWith("PING ")) {
                        // We must respond to PINGs to avoid being disconnected.
                        // The response is written directly to avoid delay due to outbound message queue
                        senderThread.writer.write("PONG :" + line.substring(6) + "\r\n");
                        senderThread.writer.flush();
                    }
                    List<RawMessage> responses = controller.handleIncoming(IrcConnector.this, new RawMessage(line));
                    if (responses != null) {
                        senderThread.messageQueue.addAll(responses);
                    }
                }
            } catch (IOException ex) {
                LOG.error("Writes not working for network " + networkName + ". Connection lost?", ex);
            }
        }
    }

    private class SenderThread extends Thread {

        private final BufferedWriter writer;
        private final BlockingQueue<RawMessage> messageQueue;

        public SenderThread(BufferedWriter writer) {
            this.writer = writer;
            messageQueue = new LinkedBlockingQueue<>();
        }

        @Override
        public void run() {
            while (!interrupted()) {
                try {
                    RawMessage nextMsg = messageQueue.take();
                    writer.write(nextMsg.toString());
                    writer.write("\r\n");
                    writer.flush();
                    sleep(SEND_DELAY_MS);
                } catch (InterruptedException ex) {
                    break;
                } catch (IOException ex) {
                    LOG.error("Writes not working for network " + networkName + ". Connection lost?", ex);
                }
            }
        }
    }
}
