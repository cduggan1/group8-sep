package main;

import java.io.IOException;
//import java.util.*;

public class Main {


    String url = "";
    String user = "";
    String password = "";
    String query = "SELECT * FROM mytable";


    public static boolean enableLogging = true;
    public static boolean addCount = true;



    //Provided we built the initial object correctly, start
    //program and initialise API responses.
    public static void main(String[] args) throws IOException {

        System.out.println(Logger.BLUE + "Initialising...." + Logger.RESET);

        try{
            Thread.sleep(500);
        } catch (Exception f){
            f.printStackTrace();
        }
        Logger.addLog("",Logger.BLUE + "Sending Init Broadcast." + Logger.RESET);

        try{
            Thread.sleep(500);
        } catch (Exception f){
            f.printStackTrace();
        }

        CsvData accomsData = new CsvData();
        accomsData.init();
        if(accomsData.accoms==null) {
            System.out.println("Error Parsing CSV");
            Logger.addLog("Init", "CSV Error");
            //Something didn't work
            System.exit(1);
        }


        SynonymMapBuilder.init();
        UtilitiesFunction.initNonNegotiables();
        ApiCalls api = new ApiCalls();
        api.init(accomsData);
        DatabaseManager.testConnection();


    }



}