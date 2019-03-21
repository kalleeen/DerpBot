package fi.derpnet.derpbot.httpapi.handler;

import com.google.gson.Gson;
import fi.derpnet.derpbot.bean.RawMessage;
import fi.derpnet.derpbot.controller.MainController;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

public class MessageHandler {

    private final MainController controller;

    public MessageHandler(MainController controller) {
        this.controller = controller;
    }

    public void handle(String str) {
        MessagePayload msg = new Gson().fromJson(str, MessagePayload.class);
        if (msg != null && msg.message != null) {
            controller.getIrcConnectors().stream()
                    .filter(c -> c.networkName.equals(msg.network))
                    .findAny()
                    .ifPresent(connector -> Stream.of(msg.message.split("\n"))
                    .filter(StringUtils::isNotBlank)
                    .map(message -> new RawMessage(null, "privmsg", msg.recipient, ':' + message))
                    .forEach(connector::send));
        }
    }

    private class MessagePayload {

        String network;
        String recipient;
        String message;
    }
}
