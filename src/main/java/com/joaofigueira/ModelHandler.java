package com.joaofigueira;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.*;

import org.json.*;

public class ModelHandler implements HttpHandler {

    JSONObject jsonObject;

    public ModelHandler(JSONObject jsonObject){
        this.jsonObject = jsonObject;
    }

    public void handle(final HttpExchange httpExchange) throws IOException {

        new Thread(){
          public void run(){
              JSONArray dealers = jsonObject.getJSONArray("dealers"); // Get the dealers
              int dealerSize = dealers.length();
              Map<String,JSONArray> modelVehicles = new HashMap<String, JSONArray>();
              JSONObject dealerObject = null;
              JSONArray vehicles = null;
              for (int i = 0; i< dealerSize;i++){         // For each dealer get its vehicles
                  dealerObject = dealers.getJSONObject(i);
                  vehicles = dealerObject.getJSONArray("vehicles");
                  int vehiclesSize = vehicles.length();
                  JSONObject vehicleObj= null;
                  for (int j =0; j < vehiclesSize;j++){
                      vehicleObj = vehicles.getJSONObject(j);
                      String model = (String) vehicleObj.get("model");
                      JSONArray vehiclesArray = new JSONArray();
                      try{
                          if(modelVehicles.containsKey(model)){
                              modelVehicles.get(model).put(vehicleObj);

                          }else{
                              vehiclesArray.put(vehicleObj);
                              modelVehicles.put(model,vehiclesArray);
                          }
                      }catch (JSONException e1){
                          e1.printStackTrace();
                      }catch (NullPointerException e1){
                          e1.printStackTrace();
                      }


                  }

              }

              JSONObject outputModels = new JSONObject(); // Create the output JSONObject

              for (Map.Entry<String,JSONArray> entry : modelVehicles.entrySet()) { // Populate the Output with the HasMap information
                  outputModels.put(entry.getKey(),entry.getValue());
              }

              Headers headers = httpExchange.getResponseHeaders();
              String requestMethod = httpExchange.getRequestMethod().toUpperCase();
              if ("GET".equals(requestMethod)) {

                  headers.set("Content-Type","application/json;");
                  byte[] rawResponseBody = outputModels.toString().getBytes();
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
