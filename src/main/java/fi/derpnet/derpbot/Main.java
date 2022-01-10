package fi.derpnet.derpbot;

import fi.derpnet.derpbot.controller.MainController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

    public static void main(String[] args) {
        Logger LOG = LogManager.getLogger(Main.class);
        LOG.info("Strating up");
        MainController c = new MainController();
        LOG.info("Initializing");
        c.start();
        LOG.info("Finished");
    }
}
