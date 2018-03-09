package com.joaofigueira;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.*;

public class TransmissionHandler implements HttpHandler {

    JSONObject jsonObject;

    public TransmissionHandler(JSONObject jsonObject){
        this.jsonObject = jsonObject;
    }

    public void handle(final HttpExchange httpExchange) throws IOException {

        new Thread(){
            public void run(){
                JSONArray dealers = jsonObject.getJSONArray("dealers"); // Get the dealers
                int dealerSize = dealers.length();


                Map<String,JSONArray> transmissionVehicles = new HashMap<String, JSONArray>();
                JSONObject dealerObject = null;
                JSONArray vehicles = null;
                for (int i = 0; i< dealerSize;i++){         // For each dealer get its vehicles
                    dealerObject = dealers.getJSONObject(i);
                    vehicles = dealerObject.getJSONArray("vehicles");
                    int vehiclesSize = vehicles.length();
                    JSONObject vehicleObj= null;
                    for (int j =0; j < vehiclesSize;j++){
                        vehicleObj = vehicles.getJSONObject(j);
                        String model = (String) vehicleObj.get("transmission");
                        JSONArray transmissionArray = new JSONArray();
                        try{
                            if(transmissionVehicles.containsKey(model)){
                                transmissionVehicles.get(model).put(vehicleObj);

                            }else{
                                transmissionArray.put(vehicleObj);
                                transmissionVehicles.put(model,transmissionArray);
                            }
                        }catch (JSONException e1){
                            e1.printStackTrace();
                        }catch (NullPointerException e1){
                            e1.printStackTrace();
                        }


                    }

                }

                JSONObject outputTransmission = new JSONObject(); // Create the output JSONObject

                for (Map.Entry<String,JSONArray> entry : transmissionVehicles.entrySet()) { // Populate the Output with the HasMap information
                    outputTransmission.put(entry.getKey(),entry.getValue());
                }

                Headers headers = httpExchange.getResponseHeaders();
                String requestMethod = httpExchange.getRequestMethod().toUpperCase();
                if ("GET".equals(requestMethod)) {

                    headers.set("Content-Type","application/json;");
                    byte[] rawResponseBody = outputTransmission.toString().getBytes();
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
