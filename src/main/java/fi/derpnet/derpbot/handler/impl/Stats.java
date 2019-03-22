package fi.derpnet.derpbot.handler.impl;

import fi.derpnet.derpbot.connector.IrcConnector;
import fi.derpnet.derpbot.controller.MainController;
import fi.derpnet.derpbot.handler.SimpleMessageHandler;
import fi.derpnet.derpbot.util.CommandUtils;
import fi.derpnet.derpbot.util.IrcFormatter;
import fi.derpnet.derpbot.util.IrcUtils;
import fi.derpnet.derpbot.util.TimeUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Stats implements SimpleMessageHandler {

    private Map<String, Map<String, StatsData>> allStats; // <network name, <nick, stats>>
    private long startup;

    @Override
    public void init(MainController controller) {
        allStats = new ConcurrentHashMap<>();
        startup = System.currentTimeMillis();
    }

    @Override
    public String getCommand() {
        return "!stats";
    }

    @Override
    public String getHelp() {
        return "Get your or someone elses stats";
    }

    @Override
    public String handle(String sender, String recipient, String message, IrcConnector ircConnector) {
        Map<String, StatsData> statsMap = allStats.get(ircConnector.networkName);
        if (statsMap == null) {
            synchronized (allStats) {
                statsMap = allStats.get(ircConnector.networkName);
                if (statsMap == null) {
                    statsMap = new ConcurrentHashMap<>();
                    allStats.put(ircConnector.networkName, statsMap);
                }
            }
        }
        // Get the stats print first, otherwise updating the stats below will skew the result
        String statsPrint = getStatsPrint(sender, message, statsMap);

        StatsData stats = statsMap.get(sender);
        if (stats == null) {
            stats = new StatsData();
            stats.nick = IrcUtils.getNickFromSender(sender);
            statsMap.put(stats.nick, stats);
        }
        stats.messages++;
        stats.words += message.split("\\s+").length;
        stats.characters += message.length();
        stats.lastSeen = System.currentTimeMillis();

        return statsPrint;
    }

    private String getStatsPrint(String sender, String message, Map<String, StatsData> statsMap) {
        if (!message.startsWith("!stats")) {
            return null;
        }
        String who = CommandUtils.getFirstParameter(message);
        if (who == null) {
            who = IrcUtils.getNickFromSender(sender);
        }
        StatsData stats = statsMap.get(who);
        if (stats != null) {
            return stats.toString();
        }
        return "No stats for " + who;
    }

    private class StatsData {

        String nick;
        long messages = 0;
        long words = 0;
        long characters = 0;
        long lastSeen = 0;

        @Override
        public String toString() {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            long msSinceStart = System.currentTimeMillis() - startup;
            String msgPerDay = String.format("%.2f", ((double) messages) / msSinceStart * TimeUtils.MS_IN_DAY);
            String wordsPerDay = String.format("%.2f", ((double) words) / msSinceStart * TimeUtils.MS_IN_DAY);
            String charsPerDay = String.format("%.2f", ((double) characters) / msSinceStart * TimeUtils.MS_IN_DAY);
            return nick + " stats since " + sdf.format(new Date(startup)) + ": "
                    + "messages: " + IrcFormatter.bold(String.valueOf(messages)) + " (" + msgPerDay + " per day) "
                    + "words: " + IrcFormatter.bold(String.valueOf(words)) + " (" + wordsPerDay + " per day) "
                    + "characters: " + IrcFormatter.bold(String.valueOf(characters)) + " (" + charsPerDay + " per day) "
                    + "last seen: " + IrcFormatter.bold(TimeUtils.msToTime(System.currentTimeMillis() - lastSeen)) + " ago (" + sdf.format(new Date(lastSeen)) + ')';
        }
    }
}
