package com.joaofigueira;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.*;

public class FuelTypeHandler implements HttpHandler {

    JSONObject jsonObject;

    public FuelTypeHandler(JSONObject jsonObject){
        this.jsonObject = jsonObject;
    }

    public void handle(final HttpExchange httpExchange) throws IOException {

        new Thread(){
            public void run(){
                JSONArray dealers = jsonObject.getJSONArray("dealers"); // Get the dealers
                int dealerSize = dealers.length();


                Map<String,JSONArray> fuelVehicles = new HashMap<String, JSONArray>();
                JSONObject dealerObject = null;
                JSONArray vehicles = null;
                for (int i = 0; i< dealerSize;i++){         // For each dealer get its vehicles
                    dealerObject = dealers.getJSONObject(i);
                    vehicles = dealerObject.getJSONArray("vehicles");
                    int vehiclesSize = vehicles.length();
                    JSONObject vehicleObj= null;
                    for (int j =0; j < vehiclesSize;j++){
                        vehicleObj = vehicles.getJSONObject(j);
                        String model = (String) vehicleObj.get("fuel");
                        JSONArray vehiclesArray = new JSONArray();
                        try{
                            if(fuelVehicles.containsKey(model)){
                                fuelVehicles.get(model).put(vehicleObj);

                            }else{
                                vehiclesArray.put(vehicleObj);
                                fuelVehicles.put(model,vehiclesArray);
                            }
                        }catch (JSONException e1){
                            e1.printStackTrace();
                        }catch (NullPointerException e1){
                            e1.printStackTrace();
                        }


                    }

                }

                JSONObject outputFuels = new JSONObject(); // Create the output JSONObject

                for (Map.Entry<String,JSONArray> entry : fuelVehicles.entrySet()) { // Populate the Output with the HasMap information
                    outputFuels.put(entry.getKey(),entry.getValue());
                }

                Headers headers = httpExchange.getResponseHeaders();
                String requestMethod = httpExchange.getRequestMethod().toUpperCase();
                if ("GET".equals(requestMethod)) {

                    headers.set("Content-Type","application/json;");
                    byte[] rawResponseBody = outputFuels.toString().getBytes();
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
