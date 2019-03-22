package fi.derpnet.derpbot.connector;

import fi.derpnet.derpbot.bean.RawMessage;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import org.apache.log4j.Logger;

class ReceiverThread extends Thread {

    private static final Logger LOG = Logger.getLogger(ReceiverThread.class);

    private final BufferedReader reader;
    private final SenderThread senderThread;
    private final Runnable messageCallback;
    private final Runnable reconnectCall;
    private final Function<RawMessage, List<RawMessage>> messageFunction;

    public ReceiverThread(BufferedReader reader, SenderThread senderThread, Runnable messageCallback, Runnable reconnectCall, Function<RawMessage, List<RawMessage>> messageFunction) {
        this.reader = reader;
        this.senderThread = senderThread;
        this.messageCallback = messageCallback;
        this.reconnectCall = reconnectCall;
        this.messageFunction = messageFunction;
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(Thread.currentThread().getId() + " < " + line);
                messageCallback.run();
                RawMessage msg = new RawMessage(line);
                if (msg.command.equals("PING")) {
                    senderThread.pong(line.substring(6));
                }
                List<RawMessage> responses = messageFunction.apply(msg);
                if (responses != null) {
                    senderThread.send(responses);
                }
            }
            LOG.warn("Got EOF, socket closed? Reconnecting...");
            reconnectCall.run();
        } catch (IOException ex) {
            LOG.error("Writes not working. Connection lost?", ex);
        }
    }
}
