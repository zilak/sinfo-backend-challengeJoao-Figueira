package com.joaofigueira;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.*;
import sun.security.krb5.internal.EncTGSRepPart;

public class FindClosestDealerHandler implements HttpHandler {

    JSONObject jsonObject;

    public FindClosestDealerHandler(JSONObject jsonObject){
        this.jsonObject= jsonObject;
    }

    public void handle(final HttpExchange httpExchange) throws IOException {

        new Thread(){
            public void run(){
                Headers incHeaders = httpExchange.getRequestHeaders();
                Headers headers = httpExchange.getResponseHeaders();
                String requestMethod = httpExchange.getRequestMethod().toUpperCase();


                if ("GET".equals(requestMethod) && incHeaders.containsKey("lat") && incHeaders.containsKey("long")) {

                    JSONObject dealerObject = new JSONObject();
                    double latitude = 0;
                    double longitude = 0;
                    String lat = incHeaders.get("lat").toString().substring(1,incHeaders.get("lat").toString().length()-1);
                    String longi = incHeaders.get("long").toString().substring(1,incHeaders.get("long").toString().length()-1);

                    try {
                        latitude = Double.parseDouble(lat);
                        longitude = Double.parseDouble(longi);
                    }catch (NumberFormatException e) {
                        e.printStackTrace();
                    }

                    JSONArray dealers = jsonObject.getJSONArray("dealers"); // Get the dealers
                    int dealerSize = dealers.length();

                    double dealerDistance=0;

                    JSONObject dealerObjectAux;
                    for (int i = 0; i< dealerSize;i++){         // For each dealer get is latitude and longitude
                        if(i==0){
                            dealerObject = dealers.getJSONObject(i);
                            String strLatObj = dealerObject.get("latitude").toString();
                            double latObj = Double.parseDouble(strLatObj);

                            String strLongObj = dealerObject.get("longitude").toString();
                            double longObj = Double.parseDouble(strLongObj);

                            dealerDistance = distance(latitude, longitude, latObj,longObj);

                        }else{
                            dealerObjectAux = dealers.getJSONObject(i);
                            String strLatObj = dealerObjectAux.get("latitude").toString();
                            double latObj = Double.parseDouble(strLatObj);

                            String strLongObj = dealerObjectAux.get("longitude").toString();
                            double longObj = Double.parseDouble(strLongObj);
                            if(distance(latitude, longitude, latObj,longObj)<dealerDistance){
                                dealerDistance = distance(latitude, longitude, latObj,longObj);
                                dealerObject = dealerObjectAux;
                            }
                        }
                    }

                    headers.set("Content-Type","application/json;");
                    byte[] rawResponseBody = dealerObject.toString().getBytes();
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

    private double distance(double lat1, double lon1, double lat2, double lon2) { // Latitude 1, longitude 1, latitude 2, longitude 2
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;
        return (dist); // return distance in kilometers
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts decimal degrees to radians             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts radians to decimal degrees             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

}
