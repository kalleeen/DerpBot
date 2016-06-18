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
import org.apache.log4j.Logger;

public class IrcConnector {
    
    public final String networkName;
    public final String hostname;
    public final int port;
    public final String user;
    public final String realname;
    private final MainController controller;
    private String nick;
    private ConnectionThread connectionThread;
    private static final Logger LOG = Logger.getLogger(IrcConnector.class);
    
    public IrcConnector(String networkName, String hostname, int port, String user, String realname, String nick, MainController controller) {
        this.networkName = networkName;
        this.hostname = hostname;
        this.port = port;
        this.user = user;
        this.realname = realname;
        this.nick = nick;
        this.controller = controller;
    }
    
    public void connect() throws IOException {
        LOG.info("Connecting to " + networkName + " on " + hostname + " port " + port);
        Socket socket = new Socket(hostname, port);
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
            }
        }
        if (!nick.equals(pendingNick)) {
            nick = pendingNick;
        }
        
        connectionThread = new ConnectionThread(writer, reader);
        connectionThread.start();
    }
    
    public void join(String channel) {
        LOG.info("Joining channel " + channel + " on " + networkName);
        try {
            writeMessage("JOIN " + channel);
        } catch (IOException ex) {
            LOG.error("Failed to join channel", ex);
        }
    }
    
    private void writeMessage(String rawMsg) throws IOException {
        // TODO add a synchronized queue to avoid being killed for flooding
        connectionThread.writer.write(rawMsg);
        connectionThread.writer.write("\r\n");
    }
    
    class ConnectionThread extends Thread {
        
        private final BufferedWriter writer;
        private final BufferedReader reader;
        
        public ConnectionThread(BufferedWriter writer, BufferedReader reader) {
            this.writer = writer;
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
                        writer.write("PONG :" + line.substring(6) + "\r\n");
                        writer.flush();
                    }
                    List<RawMessage> responses = controller.handleIncoming(IrcConnector.this, new RawMessage(line));
                    if (responses != null) {
                        responses.forEach(r -> {
                            try {
                                writeMessage(r.toString());
                            } catch (IOException ex) {
                                LOG.error("Failed to write message to " + networkName + " message: " + r.toString(), ex);
                            }
                        });
                        writer.flush();
                    }
                }
            } catch (IOException ex) {
                LOG.error("Writes not working for network " + networkName + ". Connection lost?", ex);
            }
        }
    }
}
