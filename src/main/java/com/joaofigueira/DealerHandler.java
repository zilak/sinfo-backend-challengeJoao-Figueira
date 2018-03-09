package com.joaofigueira;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.*;

public class DealerHandler implements HttpHandler {

    private JSONObject jsonObject;

    public DealerHandler(JSONObject jsonObject){
        this.jsonObject = jsonObject;
    }

    public void handle(final HttpExchange httpExchange) throws IOException {

        new Thread(){
          public void run(){
              JSONArray dealers = jsonObject.getJSONArray("dealers"); // Get the dealers
              int dealerSize = dealers.length();


              Map<String,JSONArray> dealersVehicles = new HashMap<String, JSONArray>();
              JSONObject dealerObject = null;
              JSONArray vehicles = null;
              for (int i = 0; i< dealerSize;i++){         // For each dealer get its vehicles
                  dealerObject = dealers.getJSONObject(i);
                  vehicles = dealerObject.getJSONArray("vehicles");
                  dealersVehicles.put(dealerObject.get("id").toString(),vehicles);
              }

              JSONObject outputDealers = new JSONObject(); // Create the output JSONObject

              for (Map.Entry<String,JSONArray> entry : dealersVehicles.entrySet()) { // Populate the Output with the HasMap information
                  outputDealers.put(entry.getKey(),entry.getValue());
              }

              Headers headers = httpExchange.getResponseHeaders();
              String requestMethod = httpExchange.getRequestMethod().toUpperCase();
              if ("GET".equals(requestMethod)) {

                  headers.set("Content-Type","application/json;");
                  byte[] rawResponseBody = outputDealers.toString().getBytes();
                  try {
                      httpExchange.sendResponseHeaders(200, rawResponseBody.length); // 200 (ok) and send the object
                      httpExchange.getResponseBody().write(rawResponseBody);
                  } catch (IOException e) {
                      e.printStackTrace();
                  }



              } else {
                  try {
                      httpExchange.sendResponseHeaders(405,0); // 405(error method invalid)
                  } catch (IOException e) {
                      e.printStackTrace();
                  }
              }

          }
        }.start();


    }
}
