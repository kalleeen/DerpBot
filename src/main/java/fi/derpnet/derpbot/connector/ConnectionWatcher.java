package fi.derpnet.derpbot.connector;

import java.io.IOException;
import java.util.TimerTask;

class ConnectionWatcher extends TimerTask {

    private final SenderThread senderThread;
    private final Runnable connectionLoss;
    private long lastMessage;

    public ConnectionWatcher(SenderThread senderThread, Runnable connectionLoss) {
        this.senderThread = senderThread;
        this.connectionLoss = connectionLoss;
        lastMessage = System.currentTimeMillis();
    }

    @Override
    public void run() {
        try {
            if (System.currentTimeMillis() - lastMessage < IrcConnector.PING_TIMEOUT_MS) {
                return;
            }
            long lastMessageReceivedBeforeSending = lastMessage;
            senderThread.ping(String.valueOf(System.currentTimeMillis()));
            long timeSent = System.currentTimeMillis();
            //No need to wait; we got a message already when sending our PING
            if (lastMessageReceivedBeforeSending != lastMessage) {
                return;
            }
            //Wait for message; poll if result came and wait for maximum of ping timeout
            while (lastMessage < timeSent && System.currentTimeMillis() - timeSent <= IrcConnector.PING_TIMEOUT_MS) {
                Thread.sleep(IrcConnector.WATCHER_POLLRATE_MS);
            }
            //No message? --> connection lost
            if (System.currentTimeMillis() - lastMessage > IrcConnector.PING_TIMEOUT_MS) {
                connectionLoss.run();
            }
        } catch (IOException ex) {
            connectionLoss.run();
        } catch (InterruptedException ex) {
        }
    }

    public void gotMessage() {
        lastMessage = System.currentTimeMillis();
    }
}
