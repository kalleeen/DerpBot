/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fi.derpnet.derpbot.handler.impl;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import fi.derpnet.derpbot.bean.MatrixMessage;
import fi.derpnet.derpbot.bean.outages.Company;
import fi.derpnet.derpbot.bean.outages.Outages;
import fi.derpnet.derpbot.connector.Connector;
import fi.derpnet.derpbot.controller.MainController;
import fi.derpnet.derpbot.handler.AdvancedMessageHandler;
import java.io.IOException;
import java.util.stream.Collectors;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author thomas
 */
public class Katkot implements AdvancedMessageHandler {
    
    private static final String API_BASE_URL = "https://enerity-api.azureedge.net/outagemap/tailored/summary/";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    Logger logger = LogManager.getLogger(Katkot.class);
    
    @Override
    public void init(MainController controller) {
    }
    
    @Override
    public String getCommand() {
        return "!katkot";
    }
    
    @Override
    public String getHelp() {
        return "Show current electricity outages in Finland";
    }

    @Override
    public MatrixMessage handle(String sender, String recipient, String message, Connector connector) {
        if (!message.startsWith("!katkot")) {
            return null;
        }
        Outages outages = null;
        
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(API_BASE_URL)
                    .get()
                    .build();
            try (Response response = client.newCall(request).execute()) {
                Gson gson = new Gson();
                outages = gson.fromJson(response.body().string(), Outages.class);
            }
        } catch (IOException | JsonSyntaxException | NullPointerException ex) {
            logger.error("Getting response from Energiateollisuus failed", ex);
            return new MatrixMessage("Getting response from Energiateollisuus failed", recipient);
        }
        
        StringBuilder htmlResponse = new StringBuilder();
        StringBuilder textResponse = new StringBuilder();
        // Header
        htmlResponse.append("<table>" +
                "<tr>" +
                "<th>Yhtiö&nbsp;</th>" +
                "<th>Sähköttä&nbsp;</th>" +
                "<th>Häiriökartta</th>" +
                "</tr>");
        // Values
        outages.getCompanies().stream().filter(company -> company.getTotal() != null).forEach(company -> {
            htmlResponse.append("<tr>" +
                    "<td>" + company.getName() + "&nbsp;</td>" +
                    "<td>" + company.getTotal() + "&nbsp;</td>" +
                    "<td><a href=\"" + company.getOutagemap() + "\">Häiriökartta</a></td>" +
                    "</tr>");
            textResponse.append(company.getName() + ": " + company.getTotal() + " asiakasta sähköttä. Häiriökartta: " + company.getOutagemap() + "\n");
            }
        );
        // Footer
        htmlResponse.append("<tr>" +
                "<td>Yhteensä:</td>" +
                "<td>" + outages.getCompanies().stream().filter(company -> company.getTotal() != null).map(Company::getTotal).collect(Collectors.summingInt(Integer::intValue)) + "</td>" +
                "<td></td>" +
                "</tr>");
        htmlResponse.append("</table>");
        return new MatrixMessage(textResponse.toString(), htmlResponse.toString(), recipient);
    }
    
}
