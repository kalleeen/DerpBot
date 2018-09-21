package fi.derpnet.derpbot.handler.impl;

import fi.derpnet.derpbot.bean.reittiopas.Leg;
import fi.derpnet.derpbot.bean.reittiopas.Location;
import fi.derpnet.derpbot.connector.IrcConnector;
import fi.derpnet.derpbot.controller.MainController;
import fi.derpnet.derpbot.handler.LoudMultiLineMessageHandler;
import fi.derpnet.derpbot.util.ReittiopasAPI;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

public class Reittiopas implements LoudMultiLineMessageHandler {

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

        List<String> print = new LinkedList<>();

        String routeSearch = message.substring(12);
        String[] split = routeSearch.split("--");

        try {
            Location start = api.getLocation(split[0].trim());

            if (start == null) {
                print.add("Starting point not found!");
                return print;
            }

            Location destination = api.getLocation(split[1].trim());

            if (destination == null) {
                print.add("Destination point not found!");
                return print;
            }

            List<Leg> legs = api.getRoute(start, destination);

            if (legs == null) {
                print.add("No route found!");
                return print;
            } else {
                print.add((start.getDescription() + " - " + destination.getDescription() + ":"));
            }

            int i = 1;
            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
            for (Leg leg : legs) {
                String startTime = format.format(leg.getStartTime());
                String finishTime = format.format(leg.getFinishTime());
                String startDescription = leg.getStartLocation() != null ? leg.getStartLocation().getDescription() : "";
                String finishDescription = leg.getFinishLocation() != null ? leg.getFinishLocation().getDescription() : "";
                String startCode = leg.getStartLocation() != null ? leg.getStartLocation().getCode() : "";
                String finishCode = leg.getFinishLocation() != null ? leg.getFinishLocation().getCode() : "";
                String legMode = leg.getMode();
                String legLine = leg.getLine();

                if (legMode.equals("WALK") && startCode == null) {
                    print.add(String.format("%d. [%s] %s --> %s --> [%s] %s (%s)", i, startTime, startDescription, legMode, finishTime, finishDescription, finishCode));
                } else if (legMode.equals("WALK") && finishCode == null) {
                    print.add(String.format("%d. [%s] %s (%s) --> %s --> [%s] %s", i, startTime, startDescription, startCode, legMode, finishTime, finishDescription));
                }  else if (legMode.equals("WALK")) {
                    print.add(String.format("%d. [%s] %s (%s) --> %s --> [%s] %s (%s)", i, startTime, startDescription, startCode, legMode, finishTime, finishDescription, finishCode));
                } else {
                    print.add(String.format("%d. [%s] %s (%s) --> %s (Line: %s) --> [%s] %s (%s)", i, startTime, startDescription, startCode, legMode, legLine, finishTime, finishDescription, finishCode));
                }
                i++;
            }
        } catch (NullPointerException npe) {
            print.add("Something wrong -- right usage? !reittiopas start -- destination");
        }
        return print;
    }
}
