package fi.derpnet.derpbot.httpapi;

import fi.derpnet.derpbot.controller.MainController;
import fi.derpnet.derpbot.httpapi.handler.MessageHandler;
import java.io.IOException;
import java.net.InetSocketAddress;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

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
                public void handle(String path, Request request, HttpServletRequest servletRequest, HttpServletResponse response) throws IOException, ServletException {
                    if (secret.equals(request.getHeader("Secret"))) {
                        String requestBody = IOUtils.toString(request.getReader());
                        switch (path) {
                            case "/msg":
                                messageHandler.handle(requestBody);
                                break;
                        }
                    }
                    response.setStatus(200);
                    request.setHandled(true);
                }
            });
            server.start();
            LOG.info("http api started on port " + HTTP_PORT);
        } catch (Exception ex) {
            LOG.error("Failed to start HttpApiDaemon", ex);
        }
    }
}
