package fi.derpnet.derpbot.connector;

import fi.derpnet.derpbot.bean.Message;
import fi.derpnet.derpbot.bean.RawMessage;
import fi.derpnet.derpbot.util.IrcFormatter;
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
    private final BlockingQueue<Message> messageQueue;

    public SenderThread(BufferedWriter writer, int ratelimit) {
        this.writer = writer;
        this.ratelimit = ratelimit;
        messageQueue = new LinkedBlockingQueue<>();
    }

    @Override
    public void run() {
        while (!interrupted()) {
            try {
                Message nextMsg = messageQueue.take();
                String formatted = IrcFormatter.convertHtml(nextMsg.toString());
                writer.write(formatted);
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

    public void send(Message msg) {
        messageQueue.add(msg);
    }

    public void send(Collection<Message> msgs) {
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
