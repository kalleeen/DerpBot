package fi.derpnet.derpbot.httpapi.handler;

import com.google.gson.Gson;
import fi.derpnet.derpbot.bean.MatrixMessage;
import fi.derpnet.derpbot.connector.MatrixConnector;
import fi.derpnet.derpbot.controller.MainController;

public class MessageHandler {

    private final MainController controller;

    public MessageHandler(MainController controller) {
        this.controller = controller;
    }

    public void handle(String str) {
        MessagePayload msg = new Gson().fromJson(str, MessagePayload.class);
        if (msg != null && msg.message != null) {
            controller.getConnectors().stream()
                    .filter(MatrixConnector.class::isInstance)
                    .findAny()
                    .ifPresent(connector -> connector.send(new MatrixMessage(msg.message, msg.recipient)));
        }
    }

    private class MessagePayload {

        String recipient;
        String message;
    }
}
