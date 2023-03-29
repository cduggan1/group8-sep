package main;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static spark.Spark.*;

public class ApiCalls {

    public void init(CsvData accomsData) {


        port(443); // Open port for Spark

        get("/killapi", (req, res)->{
            System.out.println(Logger.RED + "Request to quit API" + Logger.RESET);
            System.exit(0);
            return "done."; //Ignore
        });

        get("/s/*", (req, res)->{
            String abbreviation = req.splat()[0];
            if(abbreviation!=null)
                try{
                    Logger.addLog("Redirect", "Call for " + abbreviation);
                    res.redirect(UtilitiesFunction.getURLFromAbbreviation(accomsData.accoms, abbreviation));
                }catch(Exception e){Logger.addLog("Redirect Failed", "Error: " + e);}
            return null;
        });

        get("/fullQuery", (req, res) -> {
            System.out.println("Filtering Query...");
            Logger.addLog("fullQuery", "API Called");

            // Construct a map of filters based on the query parameters
            Map<String, String> filters = new HashMap<>();
            for (String key : req.queryParams()) {

                //District Filtering
                //Extracts a number and places it into the filters list.
                if (key.equalsIgnoreCase("District")){
                    String receivedDistrict = req.queryParams(key);
                    if(UtilitiesFunction.extractNumber(receivedDistrict)!=null)
                        try{
                            filters.put(key, UtilitiesFunction.extractNumber(receivedDistrict).toString());
                        } catch(Exception e) {
                            e.printStackTrace();
                            Logger.addLog("District Cleaning", "Null Pointer(?) : " + e);
                        }

                }

                //Distance Filtering
                //Receives a string representing a duration in hrs/mins.
                //Parses minutes from this duration
                //Adds filter with key=parameter containing "Distance" and value = mins
                else if(key.toLowerCase().contains("distance")){

                    String request = req.queryParams(key);
                    Map<String, Integer> maxDuration = UtilitiesFunction.parseTime(request,true);//True = minutes only
                    int maxMinutes=0;
                    try {
                        maxMinutes = maxDuration.get("m");

                    }catch(Exception e){e.printStackTrace();} //Ignore
                    if (maxMinutes!=0){
                        filters.put(key, Integer.toString(maxMinutes));
                    }
                }else {
                    filters.put(key, req.queryParams(key));
                }
            }


            System.out.println(filters);
            if(filters.size()>0) {
                Logger.addLog("fullQuery", "FILTERS ADDED:" + filters);
            } else {
                Logger.addLog("fullQuery", "No filters specified.");
            }

            // Filter the accoms list based on the query parameters
            // Convert the filtered list to a JSON formatted string
            List<Map<String,String>> filteredAccoms = UtilitiesFunction.filterAccoms(accomsData.accoms, filters);

            // Set the content type of the response to JSON
            res.type("application/json");

            //response = "{\"Residences\":"+response+"}";
            String response = UtilitiesFunction.convertToJsonList(filteredAccoms);

            if(Main.addCount){
                response = UtilitiesFunction.addCount(response);
            }

            System.out.println("RESPONSE" + response);
            Logger.addLog("RESPONSE:" , response);
            return response;

        });



        // Builds the URL to be processed by the webscraper, as well as the requested BER rating
        //Gets a JSON formatted string containing the properties matching the query parameters from the WebCrawler class
        get("/scrape", (req, res) -> {
            res.type("application/json");

            System.out.println("Filtering Query...");
            Logger.addLog("scrape", "API Called");

            HashMap<String,String>scrapeFilters = new HashMap<>();

            //Base url used to built parentUrl
            String parentURL = "https://www.daft.ie/property-for-rent/dublin-city-centre-dublin?furnishing=furnished";
            //Page parameters to be appended to the end of the url string, for the purposes of iterating through multiple pages
            String appendIndex = "pageSize=20&from=";
            //BER_Query default set to All, so that if no BER_Query is passed, all properties are returned
            String BER_Query = "All";
            String filterString = "&";

            String[] filters = {"facilities=", "leaseLength_from=", "numBeds_from=", "numBaths_from=", "propertyType=","rentalPrice_to="};

            for (String filter : filters) {
                scrapeFilters.put(filter, "");
            }

            //Building query parameters for WebCrawler urls using API call query parameters
            for (String key : req.queryParams()) {
                if (!req.queryParams(key).equals("Def")/* && !req.queryParams(key).equals(null)*/) {
                    if (key.equals("BER")) {
                        BER_Query = req.queryParams(key);
                    }
                    else if (key.equals("leaseLength_from")) {
                        System.out.println(req.queryParams(key));
                        Matcher matcher = Pattern.compile("(\\d+)?.*?(?<!\\d)(\\d+)").matcher(req.queryParams(key));
                        matcher.find(); // TODO figure out what this is doing

                        if (req.queryParams(key).toLowerCase().contains("y")/*|| req.queryParams(key).contains("Y")*/) { // change to shorten code -Liam
                            int months = 0;
                            if (matcher.group(1) != null) {
                                months = Integer.valueOf(matcher.group(1)) * 12;
                                if (matcher.group(2) != null) {
                                    System.out.println(matcher.group(2));
                                    months = Integer.valueOf(matcher.group(2)) + months;
                                }
                            }
                            else {
                                months = Integer.valueOf(matcher.group(2)) * 12;
                            }
                            System.out.println("Months: " + months);
                            scrapeFilters.put(key + "=", String.valueOf(months));
                            filterString = filterString + key + "=" + months + "&"; // String Concat in Loop -Liam
                        }
                        else {
                            int months = 0; // redundant initializer
                            try {
                                months = Integer.valueOf(matcher.group());
                            } catch (Exception e){
                                months = Integer.valueOf(matcher.group(1));
                            }
                            System.out.println("Months: " + months);
                            scrapeFilters.put(key + "=", String.valueOf(months));
                            filterString = filterString + key + "=" + months + "&";
                        }
                    }
                    else {
                        filterString = filterString + key + "=" + req.queryParams(key) + "&";
                        scrapeFilters.put(key + "=", req.queryParams(key));
                    }
                }
            }

            Logger.addLog("scrape","Map of filters of post request: " + scrapeFilters);

            //Putting all the pieces of the url together
            parentURL = parentURL + filterString + appendIndex;
            Logger.addLog("scrape","Assembled parent Url for backup crawling method: " + parentURL);

            //Getting Json response from WebCrawler
            String response = WebCrawler.Daft(parentURL, BER_Query, scrapeFilters);

            //Returning Json
            Logger.addLog("RESPONSE",response);
            return response;

        });


        //Receives -> ID
        //Returns -> Site at index (ID) in list.
        get("/id/:index", (req,res)->{
            Logger.addLog("ID", "API Called");
            try {
                int index = Integer.parseInt(req.params(":index"));
                if(index<accomsData.accoms.size()) {
                    System.out.println("Index requested: " + index);
                    ArrayList<Map<String,String>> holder = new ArrayList<>();
                    holder.add(accomsData.accoms.get(index));
                    String json = UtilitiesFunction.convertToJsonList(holder);
                    res.type("application/json");
                    Logger.addLog("RESPONSE" , json);
                    return json;
                }else{
                    Logger.addLog("ID", "Out Of Bounds");
                    return "Out of Bounds";
                }
            }catch(Exception e){
                Logger.addLog("ID", "Invalid input");
                return "Invalid input";
            }
        });

        //Receives -> Ask for All
        //Returns -> Every row in list.
        get("/all", (req,res)->{
            Logger.addLog("All", "API Called");
            System.out.println("Requested All");
            //Filter object
            //---NO FILTER REQUIRED AS RETURNING ALL----
            //Pass object into method to new string (json).
            String json = UtilitiesFunction.convertToJsonList(accomsData.accoms);
            res.type("application/json");
            //Return json
            if (json != null){
                System.out.println(json);
                Logger.addLog("RESPONSE", json);
            } else {
                System.out.println("json.toString() produced Null Pointer Exception");
                Logger.addLog("ERROR","json.toString() produced Null Pointer Exception");
            }

            return json;
        });

        get("/admin/log",(req, res)->{
            if(Main.enableLogging) {
                res.body(Logger.logFile.toString());
                return Logger.logFile;
            }else{
                return "Logging Disabled.";
            }
        });

        get("/admin/testTimeParser",(req, res)->{
            String request = req.queryParams("time");
            return UtilitiesFunction.parseTime(request, true);
        });

        get("/admin/csvUpdate",(req, res)->{
            if (accomsData.updateObject()){
                return "csv Updated";
            }
            return "csv Update Failed";
        });

        //End of API calls.



    }
}
