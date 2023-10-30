package fi.derpnet.derpbot.httpapi;

import fi.derpnet.derpbot.controller.MainController;
import fi.derpnet.derpbot.httpapi.handler.MessageHandler;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.Callback;

public class HttpApiDaemon {

    private static final Logger LOG = LogManager.getLogger(HttpApiDaemon.class);

    private static final int HTTP_PORT = 8080;

    public void init(MainController controller) {
        String secret = controller.getConfig().get("http.api.secret");
        if (StringUtils.isBlank(secret)) {
            LOG.info("http.api.secret not set, http api disabled");
            return;
        }
        try {
            MessageHandler messageHandler = new MessageHandler(controller);
            Server server = new Server(new InetSocketAddress(HTTP_PORT));
            server.setHandler(new AbstractHandler() {
                @Override
                public boolean handle(Request request, Response response, Callback callback) throws IOException {
                    if (secret.equals(request.getHeaders().get("Secret"))) {
                        String requestBody = new BufferedReader(new InputStreamReader(Content.Source.asInputStream(request), StandardCharsets.UTF_8))
                                .lines().collect(Collectors.joining("\n"));
                        switch (request.getHttpURI().getPath()) {
                            case "/msg":
                                messageHandler.handle(requestBody);
                                break;
                        }
                    }
                    response.setStatus(200);
                    callback.succeeded();
                    return true;
                }
            });
            server.start();
            LOG.info("http api started on port " + HTTP_PORT);
        } catch (Exception ex) {
            LOG.error("Failed to start HttpApiDaemon", ex);
        }
    }
}
