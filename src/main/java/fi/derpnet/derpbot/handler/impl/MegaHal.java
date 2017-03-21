package fi.derpnet.derpbot.handler.impl;

import fi.derpnet.derpbot.connector.IrcConnector;
import fi.derpnet.derpbot.controller.MainController;
import fi.derpnet.derpbot.handler.SimpleMessageHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;
import org.apache.log4j.Logger;
import org.jibble.jmegahal.*;

public class MegaHal implements SimpleMessageHandler{
    private JMegaHal hal;
    private Random randomGenerator;
    private String nick;
    
    private static final Logger LOG = Logger.getLogger(MegaHal.class);

    @Override
    public void init(MainController controller) {
        nick = controller.getConfig().get("default.nick");
        try {
            FileInputStream fileIn = new FileInputStream(new File("megahal.brn"));
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            hal = (JMegaHal) objectIn.readObject();
            fileIn.close();
            objectIn.close();
        } catch (IOException | ClassNotFoundException ex) {
            LOG.warn("Unable to read brains, creating new ones");
            hal = new JMegaHal();
        }
        randomGenerator = new Random();
    }

    @Override
    public String getCommand() {
        if (nick == null || nick.equals("")){
            return "!megahal";
        }
        else {
            return nick+":";
        }
    }

    @Override
    public String getHelp() {
        return "MegaHal, usage !megahal <chat> or nick: <chat>";
    }

    @Override
    public String handle(String sender, String recipient, String message, IrcConnector ircConnector) {
        if (!message.startsWith("!megahal ")) {
            return null;
        }
        
        String input = message.replace("!megahal ", "");
        hal.add(input);
        String[] words = input.split(" ");
        
        String response = "";
        
        if (words.length > 1){
            response = hal.getSentence(words[randomGenerator.nextInt(words.length-1)]);
        }
        if (response == null || response.equals("")){
            response = hal.getSentence();
        }
        
        try {
            FileOutputStream fileOut = new FileOutputStream(new File("megahal.brn"));
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(hal);
            objectOut.close();
            fileOut.close();
        }catch (IOException e){
            LOG.error("Unable to save MegaHal, brains are not going to be saved!",e);
        }
        
        return response;
    }

}
