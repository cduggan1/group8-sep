package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static spark.Spark.*;

public class ApiCalls {

    public void init(CsvData accomsData, CsvData cityData) {


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
        //Gets a JSON formatted string containing the properties matching the query parameters from the webCrawler class
        get("/scrape/*/", (req, res) -> {
            res.type("application/json");

            System.out.println("Filtering Query...");
            Logger.addLog("scrape", "API Called");

            String country = req.splat()[0];
            String parentURL = "";
            String appendIndex = "";
            String filterString = "";
            HashMap<String, String> scrapeFilters = new HashMap<>();

            if (req.splat()[0].equals("ireland")) {

                //Base url used to built parentUrl
                parentURL = "https://www.daft.ie/property-for-rent/dublin-city-centre-dublin?furnishing=furnished";
                //Page parameters to be appended to the end of the url string, for the purposes of iterating through multiple pages
                appendIndex = "pageSize=20&from=";
                //BER_Query default set to All, so that if no BER_Query is passed, all properties are returned
                String BER_Query = "All";
                filterString = "&";

                String[] filters = {"facilities=", "leaseLength_from=", "numBeds_from=", "numBaths_from=", "propertyType=", "rentalPrice_to="};

                for (String filter : filters) {
                    scrapeFilters.put(filter, "");
                }

                //Building query parameters for webCrawler urls using API call query parameters
                for (String key : req.queryParams()) {
                    if (!req.queryParams(key).equals("Def") && !req.queryParams(key).equals(null)) {
                        if (key.equals("BER")) {
                            BER_Query = req.queryParams(key);
                        } else if (key.equals("leaseLength_from")) {
                            System.out.println(req.queryParams(key));
                            Matcher matcher = Pattern.compile("(\\d+)?.*?(?<!\\d)(\\d+)").matcher(req.queryParams(key));
                            matcher.find();

                            if (req.queryParams(key).contains("y") || req.queryParams(key).contains("Y")) {
                                int months = 0;
                                if (matcher.group(1) != null) {
                                    months = Integer.valueOf(matcher.group(1)) * 12;

                                    if (matcher.group(2) != null) {
                                        System.out.println(matcher.group(2));
                                        months = Integer.valueOf(matcher.group(2)) + months;
                                    }
                                } else {
                                    months = Integer.valueOf(matcher.group(2)) * 12;
                                }
                                System.out.println("Months: " + months);
                                scrapeFilters.put(key + "=", String.valueOf(months));
                                filterString = filterString + key + "=" + months + "&"; // String Concat in Loop
                            } else {
                                int months = 0;
                                try {
                                    months = Integer.valueOf(matcher.group());
                                } catch (Exception e) {
                                    months = Integer.valueOf(matcher.group(1));
                                }
                                System.out.println("Months: " + months);
                                scrapeFilters.put(key + "=", String.valueOf(months));
                                filterString = filterString + key + "=" + months + "&";
                            }
                        } else if (key.equals("facilities")) {
                                ParserML facilities = new ParserML(System.getProperty("user.dir") + "/src/main");
                                String parsedFacilities = facilities.query(req.queryParams(key), true);
                                System.out.println("Parsed Facilities: " + parsedFacilities);
                                scrapeFilters.put(key + "=", parsedFacilities);
                        } else {
                                filterString = filterString + key + "=" + req.queryParams(key) + "&";
                                scrapeFilters.put(key + "=", req.queryParams(key));
                            }
                        }
                    }

                Logger.addLog("scrape", "Map of filters for post request: " + scrapeFilters.toString());

                //Putting all the pieces of the url together
                parentURL = parentURL + filterString + appendIndex;
                Logger.addLog("scrape", "Assembled parent Url for backup crawling method: " + parentURL);

                //Getting Json response from webCrawler
                String response = webCrawler.init(parentURL, BER_Query, scrapeFilters, country, null);

                //Returning Json
                Logger.addLog("RESPONSE", response);
                return response;
            }

            //Housing Anywhere
            else {

                String[] filters = {"facilities=", "priceMax=", "categories=", "amenities=", "startDate=", "endDate= "};
                for (String filter : filters) {
                    scrapeFilters.put(filter, "");
                }

                String search = req.splat()[0];
                String city = "";

                for (String key : req.queryParams()) {
                    if (key.equals("City")) {
                        city = req.queryParams(key);
                    } else if (key.equals("priceMax")) {
                        filterString = filterString + key + "=" + Integer.valueOf(req.queryParams(key)) * 100;
                        scrapeFilters.put(key + "=", "%20AND%20minPrice%3C%3D" + req.queryParams(key));
                    } else if (key.equals("startDate") || key.equals("endDate")) {

                    } else if (key.equals("categories")) {
                        if (req.queryParams(key).contains("studio")){
                            scrapeFilters.put(key + "=", "%20AND%20propertyType%3A" + "STUDIO");
                        } else if (req.queryParams(key).contains("apartment")) {
                            scrapeFilters.put(key + "=", "%20AND%20propertyType%3A" + "APARTMENT");
                        } else if (req.queryParams(key).contains("private")) {
                            scrapeFilters.put(key + "=", "%20AND%20propertyType%3A" + "PRIVATE_ROOM");
                        } else if (req.queryParams(key).contains("shared")) {
                            scrapeFilters.put(key + "=", "%20AND%20propertyType%3A" + "SHARED_ROOM");
                        }

                        filterString = filterString + key + "=" + req.queryParams(key);

                    }
                }
                parentURL = "https://housinganywhere.com/s/" + city + "--" + cityData.value(city,"City","Country") + "?furniture=furnished&suitableFor=student&";

                String response = webCrawler.init(parentURL + "page=", "All", scrapeFilters, search, city);
                return response;
            }
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


        get("/checkCity/*",(req,res)->{
            String csvFile = "src/main/cityData.csv";
            String searchString = req.splat()[0];
            Logger.addLog("cityCheck", "Checking value: " + searchString);


            try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] columns = line.split(",");
                    if (columns.length > 0 && columns[0].equalsIgnoreCase(searchString)) {
                        Logger.addLog("cityCheck", "Value: " + searchString + " found");
                        return "{\"exists\":\"true\"}";
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            res.status(404);
            return null;
        });

        //End of API calls.



    }
}
