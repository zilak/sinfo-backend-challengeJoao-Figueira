package com.joaofigueira;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import org.json.*;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class BookingsHandler implements HttpHandler {

    JSONArray bookings;
    JSONArray dealers;
    ReentrantLock lock = new ReentrantLock();

    public BookingsHandler(JSONObject jsonObject) {
        this.bookings = jsonObject.getJSONArray("bookings"); // Get the bookings
        dealers = jsonObject.getJSONArray("dealers");
    }

    public void handle(final HttpExchange httpExchange) throws IOException {

        new Thread(){
            public void run() {


                Headers incHeaders = httpExchange.getRequestHeaders(); // Response headers
                String requestMethod = httpExchange.getRequestMethod().toUpperCase();


                switch (requestMethod){
                    case "POST":
                        if(incHeaders.containsKey("id") && incHeaders.containsKey("vehicleId") && incHeaders.containsKey("firstName") && incHeaders.containsKey("lastName") && incHeaders.containsKey("pickupDate")){
                            System.out.println("id:"+ incHeaders.get("id").toString());
                            String id = incHeaders.get("id").toString().substring(1,incHeaders.get("id").toString().length()-1);
                            String vehicleId = incHeaders.get("vehicleId").toString().substring(1,incHeaders.get("vehicleId").toString().length()-1);
                            String firstName = incHeaders.get("firstName").toString().substring(1,incHeaders.get("firstName").toString().length()-1);
                            String lastName = incHeaders.get("lastName").toString().substring(1,incHeaders.get("lastName").toString().length()-1);
                            String pickupDate = incHeaders.get("pickupDate").toString().substring(1,incHeaders.get("pickupDate").toString().length()-1);
                            String createdAt = incHeaders.get("createdAt").toString().substring(1,incHeaders.get("createdAt").toString().length()-1);
                            lock.lock();
                            try{
                                boolean bookingExist=false;
                                JSONObject bookingObj = new JSONObject();
                                for(int i = 0; i<bookings.length();i++){  // check if there a booking with the same characteristics exists
                                    bookingObj = bookings.getJSONObject(i);


                                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                                    try{

                                        if (bookingObj.get("id").equals(id) || (bookingObj.get("vehicleId").equals(vehicleId) && bookingObj.get("pickupDate").equals(pickupDate))){

                                            Date createAtDate = format.parse(createdAt);
                                            String cancelledAt = bookingObj.get("cancelledAt").toString();

                                            Date cancelledAtDate = format.parse(cancelledAt);

                                            if(createAtDate.compareTo(cancelledAtDate)<0 && createAtDate.compareTo(cancelledAtDate) ==0){
                                                // return error booking not available !!
                                                bookingExist = true;
                                                break;
                                            }
                                        }


                                    }catch (ParseException e1){
                                        e1.printStackTrace();
                                    }catch (JSONException e2){
                                        bookingExist = true;
                                        System.out.println("canceledAt not found");
                                        break;
                                    }
                                }

                                if(!bookingExist){
                                    doBooking(id,vehicleId,firstName,lastName,pickupDate,createdAt,httpExchange);
                                }
                            }finally {
                                lock.unlock();
                            }
                        }else{
                            for (Map.Entry<String,List<String>> entry: incHeaders.entrySet()) {
                                System.out.println("Key: "+entry.getKey() + " value: "+entry.getValue());
                            }
                        }
                        break;
                    case "DELETE":
                            if(incHeaders.containsKey("id") && incHeaders.containsKey("cancelledAt") && incHeaders.containsKey("cancelledReason")){
                                String id = incHeaders.get("id").toString().substring(1,incHeaders.get("id").toString().length()-1);
                                String cancelledAt = incHeaders.get("cancelledAt").toString().substring(1,incHeaders.get("cancelledAt").toString().length()-1);
                                String cancelledReason = incHeaders.get("cancelledReason").toString().substring(1,incHeaders.get("cancelledReason").toString().length()-1);
                                lock.lock();
                                for(int i = 0; i < bookings.length();i++){
                                    JSONObject bookingObj = bookings.getJSONObject(i);
                                    try{
                                        if(bookingObj.get("id").toString().equals(id)){
                                            bookingObj.put("cancelledAt",cancelledAt);
                                            bookingObj.put("cancelledReason",cancelledReason);
                                            bookings.remove(i);
                                            bookings.put(bookingObj);
                                            break;
                                        }
                                    }catch (JSONException e2 ){

                                    }
                                }
                                lock.unlock();
                            }
                        break;
                    case "GET":
                        Headers headers = httpExchange.getResponseHeaders();
                        headers.set("Content-Type","application/json;");
                        JSONObject outputObj = new JSONObject();
                        lock.lock();
                        outputObj.put("bookings",bookings);
                        byte[] rawResponseBody = outputObj.toString().getBytes();
                        try {
                            httpExchange.sendResponseHeaders(200, rawResponseBody.length); // 200 (ok) and send the object
                            httpExchange.getResponseBody().write(rawResponseBody);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        lock.unlock();
                        break;
                    default:
                        break;
                }

            }
        }.start();
    }

    public void doBooking(String id, String vehicleId, String firstName, String lastName, String pickupDate, String createdAt , HttpExchange httpExchange){

        try {
            JSONObject newBooking = new JSONObject();
            newBooking.put("id", id);
            newBooking.put("firstName", firstName);
            newBooking.put("lastName", lastName);
            newBooking.put("vehicleId", vehicleId);
            newBooking.put("pickupDate", pickupDate);
            newBooking.put("createdAt", createdAt);

            //Check for the availability of the car, car id and the pickupDate

            int dealerSize = dealers.length();
            JSONObject dealerObject = new JSONObject();
            JSONArray vehicles;
            JSONObject vehicleObject= new JSONObject();
            boolean doBooking = false;
            mainloop:
            for (int i = 0; i< dealerSize;i++) {         // For each dealer get its vehicles
                dealerObject = dealers.getJSONObject(i);
                vehicles = dealerObject.getJSONArray("vehicles");
                int vehiclesSize = vehicles.length();
                for(int x = i ; x < vehiclesSize; x++){ // Get each vehicle and compares with the ID header
                    vehicleObject = vehicles.getJSONObject(x);
                    String vehicleObjString = vehicleObject.get("id").toString();
                    if(vehicleId.equals(vehicleObject.get("id"))){ // If the id are the same check if the pickupDate is a valid

                        JSONObject availability = (JSONObject) vehicleObject.get("availability");
                        if(checkAvailability(pickupDate,availability)){
                            doBooking = true;
                            break mainloop;
                        }
                    }
                }
            }
            if(doBooking) {
                bookings.put(newBooking);
                httpExchange.sendResponseHeaders(200, 0);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public boolean checkAvailability(String pickupDate, JSONObject jsonObject){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");


        String hourPickup = pickupDate.substring(11,13); // hours of the date
        String minutePickup = pickupDate.substring(14,16); // minutes of the date

        Date pickupDateDate = null;
        try {
            pickupDateDate = format.parse(pickupDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(pickupDateDate);
        int day = cal.get(Calendar.DAY_OF_WEEK);
        String hours= "";
        try{
            switch (day){
                case 1:
                        hours = jsonObject.get("sunday").toString();
                        if(checkDate(hours,hourPickup,minutePickup)){
                            return true;
                        }
                    break;
                case 2:
                        hours =   jsonObject.get("monday").toString();
                        if(checkDate(hours,hourPickup,minutePickup)){
                            return true;
                        }
                    break;
                case 3:
                        hours = jsonObject.get("tuesday").toString();
                        if(checkDate(hours,hourPickup,minutePickup)){
                            return true;
                        }
                    break;
                case 4:
                        hours = jsonObject.get("wednesday").toString();
                        if(checkDate(hours,hourPickup,minutePickup)){
                            return true;
                        }
                    break;
                case 5:
                        hours = jsonObject.get("thursday").toString();
                        if(checkDate(hours,hourPickup,minutePickup)){
                            return true;
                        }
                    break;
                case 6:
                        hours = jsonObject.get("friday").toString();
                        if(checkDate(hours,hourPickup,minutePickup)){
                            return true;
                        }
                    break;
                case 7:
                        hours = jsonObject.get("saturday").toString();
                        if(checkDate(hours,hourPickup,minutePickup)){
                            return true;
                        }
                    break;
                default:
                        System.out.println("error on day_of_week");
                    break;
            }
        }catch (JSONException e1){
            System.out.println("error");
            e1.printStackTrace();
        }

        return false;
    }

    public boolean checkDate(String hours,String hourPickup,String minutePickup){
        String times = hours.substring(1,hours.length()-1); // Convert ["1000","1030"] to "1000","1030"
        String[] hoursArray = times.split(","); // Split by ','

        for(int i = 0; i<hoursArray.length; i++){
            String auxHour = hoursArray[i].substring(1,hoursArray[i].length()-1); // remove "
            String pickupHour= hourPickup+minutePickup;
            if(auxHour.equals(pickupHour)){
                return true;
            }
        }
        return false;
    }
}
