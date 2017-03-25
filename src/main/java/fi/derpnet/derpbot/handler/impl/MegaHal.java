package fi.derpnet.derpbot.handler.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fi.derpnet.derpbot.connector.IrcConnector;
import fi.derpnet.derpbot.controller.MainController;
import fi.derpnet.derpbot.handler.SimpleMessageHandler;
import fi.derpnet.derpbot.util.CharsetUtils;
import fi.derpnet.derpbot.util.MegaHalBrain;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Random;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class MegaHal implements SimpleMessageHandler {

    private static final Logger LOG = Logger.getLogger(MegaHal.class);

    private MegaHalBrain hal;
    private Random randomGenerator;
    private String nick;

    @Override
    public void init(MainController controller) {
        nick = controller.getConfig().get("default.nick");
        randomGenerator = new Random();
        try (BufferedReader r = new BufferedReader(new FileReader(new File("megabrain.txt")))) {
// TODO
//        try (BufferedReader r = new BufferedReader(new FileReader(new File("megabrain.json")))) {
//            Gson gson = new GsonBuilder()
//                    .enableComplexMapKeySerialization()
//                    .excludeFieldsWithoutExposeAnnotation()
//                    .registerTypeAdapter(MegaHalBrain.class, new MegaHalBrain.HalDeserializer())
//                    .create();
//            hal = gson.fromJson(r, MegaHalBrain.class);

            //TODO remove
            hal = new MegaHalBrain();
            r.lines().map(String::getBytes).map(CharsetUtils::convertToUTF8).forEach(hal::add);
        } catch (IOException ex) {
            LOG.warn("Unable to read brains, creating new ones", ex);
            hal = new MegaHalBrain();
        }
    }

    @Override
    public String getCommand() {
        return "!megahal";
    }

    @Override
    public String getHelp() {
        return "Start your message with \"" + nick + "\" to have the bot reply to you. !megahal command can be used to see MegaHAL brain statistics.";
    }

    @Override
    public String handle(String sender, String recipient, String message, IrcConnector ircConnector) {
        if (message.startsWith("!megahal")) {
            return hal.getStats();
        }
        if (!message.startsWith(nick)) {
            return null;
        }

        String input = CharsetUtils.convertToUTF8(message.split("\\s+", 2)[1].trim().getBytes());
        if (StringUtils.isNotEmpty(input)) {
            hal.add(input);
            // TODO: switch to saveBrain() when implemented
            saveSentence(input);
        }
        String[] words = input.split("\\s+");

        String response = null;
        if (words.length > 0) {
            response = hal.getSentence(words[randomGenerator.nextInt(words.length)]);
        }
        if (StringUtils.isBlank(response)) {
            response = hal.getSentence();
        }
        return response;
    }

    private void saveSentence(String sentence) {
        try (Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("megabrain.txt", true), "UTF-8"))) {
            w.write(sentence);
            w.write('\n');
            w.flush();
        } catch (IOException e) {
            LOG.error("Unable to save MegaHal, brains are not going to be saved!", e);
        }
    }

    /**
     * WORK IN PROGRESS DONT USE
     */
    private void saveBrain() {
        try (Writer w = new BufferedWriter(new FileWriter(new File("megabrain.json")))) {
            Gson gson = new GsonBuilder()
                    .enableComplexMapKeySerialization()
                    .excludeFieldsWithoutExposeAnnotation()
                    .setPrettyPrinting()
                    .registerTypeAdapter(MegaHalBrain.class, new MegaHalBrain.HalSerializer())
                    .create();
            gson.toJson(hal, MegaHalBrain.class, w);
            w.flush();
        } catch (IOException e) {
            LOG.error("Unable to save MegaHal, brains are not going to be saved!", e);
        }
    }
}
