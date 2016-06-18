package fi.derpnet.derpbot.handler;

import fi.derpnet.derpbot.controller.MainController;

/**
 * A generic handler for things.
 */
public interface GenericHandler {

    /**
     * Called upon initializing this instance for use
     *
     * @param controller the controller that will be handling this handler
     */
    void init(MainController controller);

    /**
     * Gets the help line for this handler, used in printing the help output.
     * This should include the command prefix used to invoke this handler if one exists.
     *
     * @return The help line, or null to omit from all help prints
     */
    String getHelp();
}
