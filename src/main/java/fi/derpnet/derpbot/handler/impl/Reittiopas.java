package fi.derpnet.derpbot.handler.impl;

import fi.derpnet.derpbot.bean.Leg;
import fi.derpnet.derpbot.bean.Location;
import fi.derpnet.derpbot.connector.IrcConnector;
import fi.derpnet.derpbot.controller.MainController;
import fi.derpnet.derpbot.util.ReittiopasAPI;
import fi.derpnet.derpbot.handler.SimpleMultiLineMessageHandler;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 *
 * @author Thomas
 */
public class Reittiopas implements SimpleMultiLineMessageHandler {

    private ReittiopasAPI api;

    @Override
    public void init(MainController controller) {
        api = new ReittiopasAPI();
    }

    @Override
    public String getCommand() {
        return "!reittiopas";
    }

    @Override
    public String getHelp() {
        return "HSL Reittiopas, !reittiopas start -- destination";
    }

    @Override
    public List<String> handle(String sender, String recipient, String message, IrcConnector ircConnector) {
        if (!message.startsWith("!reittiopas ")) {
            return null;
        }

        List<String> print = new ArrayList<String>();

        String routeSearch = message.substring(12);
        String[] split = routeSearch.split("--");

        try {
            Location start = api.getLocation(split[0].trim());

            if (start == null) {
                print.add(new String("Starting point not found!"));
                return print;
            }

            Location destination = api.getLocation(split[1].trim());

            if (destination == null) {
                print.add(new String("Destination point not found!"));
                return print;
            }

            List<Leg> legs = api.getRoute(start, destination);

            if (legs == null) {
                print.add(new String("No route found!"));
                return print;
            } else {
                print.add(new String(start.getDescription() + " - " + destination.getDescription() + ":"));
            }

            int i = 1;
            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
            format.setTimeZone(TimeZone.getTimeZone("GMT+1")); //lolwut, why do i need this
            for (Leg leg : legs) {
                if (leg.getMode().equals("WALK") && leg.getStartLocation().getCode() == null) {
                    print.add(i + ". [" + format.format(leg.getStartTime()) + "] " + leg.getStartLocation().getDescription() + " "
                            + "--> " + leg.getMode() + " -->"
                            + " [" + format.format(leg.getFinishTime()) + "] " + leg.getFinishLocation().getDescription() + " (" + leg.getFinishLocation().getCode() + ")");
                } else if (leg.getMode().equals("WALK") && leg.getFinishLocation().getCode() == null) {
                    print.add(i + ". [" + format.format(leg.getStartTime()) + "] " + leg.getStartLocation().getDescription() + " (" + leg.getStartLocation().getCode() + ")"
                            + "--> " + leg.getMode() + " -->"
                            + " [" + format.format(leg.getFinishTime()) + "] " + leg.getFinishLocation().getDescription() + " ");
                } else if (leg.getMode().equals("WALK")) {
                    print.add(i + ". [" + format.format(leg.getStartTime()) + "] " + leg.getStartLocation().getDescription() + " (" + leg.getStartLocation().getCode() + ")"
                            + "--> " + leg.getMode() + " -->"
                            + " [" + format.format(leg.getFinishTime()) + "] " + leg.getFinishLocation().getDescription() + " ");
                } else {
                    print.add(i + ". [" + format.format(leg.getStartTime()) + "] " + leg.getStartLocation().getDescription() + " (" + leg.getStartLocation().getCode() + ") "
                            + "--> " + leg.getMode() + " (Line: " + leg.getLine() + ") -->"
                            + " [" + format.format(leg.getFinishTime()) + "] " + leg.getFinishLocation().getDescription() + " (" + leg.getFinishLocation().getCode() + ")");
                }
                i++;
            }
        } catch (NullPointerException npe) {
            print.add(new String("Something wrong -- right usage? !reittiopas start - destination"));
        }
        return print;
    }
}
