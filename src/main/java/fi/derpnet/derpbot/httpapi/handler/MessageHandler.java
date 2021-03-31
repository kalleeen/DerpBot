package fi.derpnet.derpbot.httpapi.handler;

import com.google.gson.Gson;
import fi.derpnet.derpbot.bean.MatrixMessage;
import fi.derpnet.derpbot.connector.MatrixConnector;
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
            controller.getConnectors().stream()
                    .filter(c -> c instanceof MatrixConnector)
                    .findAny()
                    .ifPresent(connector -> Stream.of(msg.message.split("\n"))
                    .filter(StringUtils::isNotBlank)
                    .map(message -> new MatrixMessage(message, msg.recipient))
                    .forEach(connector::send));
        }
    }

    private class MessagePayload {

        String recipient;
        String message;
    }
}
