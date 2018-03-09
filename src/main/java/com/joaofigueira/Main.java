package com.joaofigueira;




import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.*;
import sun.net.www.http.HttpClient;

import java.io.*;
import java.net.InetSocketAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) {

        if(args.length!=1){
            System.out.println("Pass as argument the file that JSON file.");
        }else{
            FileReader jsonFile = null;
            try {
                jsonFile = new FileReader(args[0]);
                BufferedReader br = new BufferedReader(jsonFile);
                String jsonString = "";
                String currentLine;
                while ((currentLine = br.readLine()) != null) {
                    jsonString = jsonString + currentLine;
                }
                JSONObject jsonObject = new JSONObject( jsonString); // After reading file convert it to JSON Object

                HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

                ExecutorService execute = Executors.newCachedThreadPool();
                server.createContext("/dealers", new DealerHandler(jsonObject));
                server.createContext("/model",new ModelHandler(jsonObject));
                server.createContext("/transmission",new TransmissionHandler(jsonObject));
                server.createContext("/fuel",new FuelTypeHandler(jsonObject));
                server.createContext("/finddealer", new FindClosestDealerHandler(jsonObject));
                server.createContext("/bookings", new BookingsHandler(jsonObject));
                server.setExecutor(execute); // default executor
                server.start();



            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
