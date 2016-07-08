package fi.derpnet.derpbot.handler.impl;

import fi.derpnet.derpbot.connector.IrcConnector;
import fi.derpnet.derpbot.controller.MainController;
import fi.derpnet.derpbot.handler.SimpleMultiLineMessageHandler;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class Poikkeusinfo implements SimpleMultiLineMessageHandler {

    public static final String API_URL = "http://www.poikkeusinfo.fi/xml/v2/fi";
    private SAXParser parser;

    @Override
    public void init(MainController controller) {
        try {
            SAXParserFactory parserFactor = SAXParserFactory.newInstance();
            parser = parserFactor.newSAXParser();
        } catch (ParserConfigurationException | SAXException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String getCommand() {
        return "!poikkeusinfo";
    }

    @Override
    public String getHelp() {
        return "HSL Poikkeusinfo";
    }

    @Override
    public List<String> handle(String sender, String recipient, String message, IrcConnector ircConnector) {
        if (!message.startsWith("!poikkeusinfo")) {
            return null;
        }
        SAXHandler handler = new SAXHandler();
        try {
            URL url = new URL(API_URL);
            try (InputStream is = url.openStream()) {
                parser.parse(is, handler);
            }
            return handler.rows.isEmpty() ? Collections.singletonList("Ei poikkeustiedotteita") : handler.rows;
        } catch (SAXException | IOException ex) {
            return Collections.singletonList("Error: " + ex.getMessage());
        }
    }

    private class SAXHandler extends DefaultHandler {

        private String currentElement = null;
        private List<String> rows;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            currentElement = qName;
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            currentElement = null;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (currentElement != null && currentElement.equals("TEXT")) {
                rows.add(String.copyValueOf(ch, start, length).trim());
            }
        }

        @Override
        public void startDocument() {
            rows = new LinkedList<>();
        }

        @Override
        public void endDocument() {
        }
    }
}
