package fi.derpnet.derpbot.connector;

import fi.derpnet.derpbot.bean.RawMessage;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.log4j.Logger;

class SenderThread extends Thread {

    private static final Logger LOG = Logger.getLogger(SenderThread.class);

    private final BufferedWriter writer;
    private final int ratelimit;
    private final BlockingQueue<RawMessage> messageQueue;

    public SenderThread(BufferedWriter writer, int ratelimit) {
        this.writer = writer;
        this.ratelimit = ratelimit;
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
                System.out.println(Thread.currentThread().getId() + " > " + nextMsg.toString());
                sleep(ratelimit);
            } catch (InterruptedException ex) {
                break;
            } catch (IOException ex) {
                LOG.error("Writes not working. Connection lost?", ex);
            }
        }
    }

    public void send(RawMessage msg) {
        messageQueue.add(msg);
    }

    public void send(Collection<RawMessage> msgs) {
        messageQueue.addAll(msgs);
    }

    public void ping(String code) throws IOException {
        writer.write("PING :" + code + "\r\n");
        writer.flush();
    }

    public void pong(String code) throws IOException {
        writer.write("PONG :" + code + "\r\n");
        writer.flush();
    }
}
