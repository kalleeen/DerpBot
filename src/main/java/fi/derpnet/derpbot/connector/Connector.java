
package fi.derpnet.derpbot.connector;

import fi.derpnet.derpbot.bean.Message;
import fi.derpnet.derpbot.bean.RawMessage;
import java.io.IOException;
import java.util.List;

public interface Connector {
    public void connect() throws IOException;
    public void disconnect();
    public void send(Message msg);
    public void setChannels(List<String> channels, boolean join);
    public List<String> getQuieterChannels();
    public void setQuieterChannels(List<String> quieterChannels);
}
