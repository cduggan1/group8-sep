package main;

import java.io.IOException;
//import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static spark.Spark.*;

//import com.github.alexdlaird.ngrok.NgrokClient;
//import com.github.alexdlaird.ngrok.protocol.CreateTunnel;
//import com.github.alexdlaird.ngrok.protocol.Proto;
//import com.github.alexdlaird.ngrok.protocol.Tunnel;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {


    String url = "";
    String user = "";
    String password = "";
    String query = "SELECT * FROM mytable";

    public static boolean tunnelNgrok = false;
    public static boolean enableLogging = true;
    public static boolean addCount = true;



    private static final ArrayList<String> nonNegotiable = new ArrayList<String>();

   // Initiate All Non-Negotiables
    public static void initNonNegotiables(){
        nonNegotiable.add("Brand");
        nonNegotiable.add("Site");
        nonNegotiable.add("HasEnsuite");
        nonNegotiable.add("HasStudio");
        nonNegotiable.add("HasTwin");
        nonNegotiable.add("Disability_Access");
    }

    //Provided we built the initial object correctly, start
    //program and initialise API responses.
    public static void main(String[] args) throws IOException {

        System.out.println(Logger.BLUE + "Initialising...." + Logger.RESET);
        try{Thread.sleep(500);}catch(Exception f){f.printStackTrace();}
        Logger.addLog("",Logger.BLUE + "Sending Init Broadcast." + Logger.RESET);

        try{Thread.sleep(500);}catch(Exception f){f.printStackTrace();}

        csvData.init();
        synonymMapBuilder.init();
        initNonNegotiables();

        DatabaseManager.testConnection();
        port(443);

        if(csvData.accoms==null) {
            System.out.println("Error Parsing CSV");
            Logger.addLog("Init", "CSV Error");
            //Something didn't work
            System.exit(1);
        }

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
                    res.redirect(getURLFromAbbreviation(csvData.accoms, abbreviation));
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
                    if(extractNumber(receivedDistrict)!=null)
                        try{
                            filters.put(key, extractNumber(receivedDistrict).toString());
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
                    Map<String, Integer> maxDuration = parseTime(request,true);//True = minutes only
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


            System.out.println(filters.toString());
            if(filters.size()>0) {
                Logger.addLog("fullQuery", "FILTERS ADDED:" + filters.toString());
            } else {
                Logger.addLog("fullQuery", "No filters specified.");
            }

            // Filter the accoms list based on the query parameters
            // Convert the filtered list to a JSON formatted string
            List<Map<?,?>> filteredAccoms = filterAccoms(csvData.accoms, filters);

            // Set the content type of the response to JSON
            res.type("application/json");

            //response = "{\"Residences\":"+response+"}";
            String response = convertToJsonList(filteredAccoms);

            if(addCount){
                response = addCount(response);
            }

            System.out.println("RESPONSE" + response);
            Logger.addLog("RESPONSE:" , response);
            return response;

        });



        // Builds the URL to be processed by the webscraper, as well as the requested BER rating
        //Gets a JSON formatted string containing the properties matching the query parameters from the webCrawler class
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

            //Building query parameters for webCrawler urls using API call query parameters
            for (String key : req.queryParams()) {
                if (!req.queryParams(key).equals("Def") && !req.queryParams(key).equals(null)) {
                    if (key.equals("BER")) {
                        BER_Query = req.queryParams(key);
                    }
                    else if (key.equals("leaseLength_from")) {
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
                            }
                            else {
                                    months = Integer.valueOf(matcher.group(2)) * 12;
                            }
                            System.out.println("Months: " + months);
                            scrapeFilters.put(key + "=", String.valueOf(months));
                            filterString = filterString + key + "=" + months + "&"; // String Concat in Loop
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

            Logger.addLog("scrape","Map of filters fo post request: " + scrapeFilters.toString());

            //Putting all the pieces of the url together
            parentURL = parentURL + filterString + appendIndex;
            Logger.addLog("scrape","Assembled parent Url for backup crawling method: " + parentURL);

            //Getting Json response from webCrawler
            String response = webCrawler.Daft(parentURL, BER_Query, scrapeFilters);

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
                if(index<csvData.accoms.size()) {
                    System.out.println("Index requested: " + index);
                    String json = convertToJson(csvData.accoms.get(index));
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
            String json = convertToJsonList(csvData.accoms);
            res.type("application/json");
            //Return json
            if (json != null){
                System.out.println(json.toString());
                Logger.addLog("RESPONSE", json);
            } else {
                System.out.println("json.toString() produced Null Pointer Exception");
                Logger.addLog("ERROR","json.toString() produced Null Pointer Exception");
            }

            return json;
        });

        get("/admin/log",(req, res)->{
            if(enableLogging) {
                res.body(Logger.logFile.toString());
                return Logger.logFile;
            }else{
                return "Logging Disabled.";
            }
                });

        get("/admin/testTimeParser",(req, res)->{
            String request = req.queryParams("time");
            return parseTime(request, true);
        });
        //End of API calls.

        //Assign temp URL using ngrok
        if(tunnelNgrok) {
            ngrokTunnel.startNgrok("");
        }
    }

    public static Integer extractNumber(String str) {
        System.out.println("Number Extractor Received " + str);
        Logger.addLog("extractNumber Received: ",str);
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            String numberStr = matcher.group();
            System.out.println("Number Extractor Returned " + numberStr);
            Logger.addLog("extractNumber Received: ",numberStr);
            return Integer.parseInt(numberStr);
        } else {
            System.out.println("Number Extractor Returned null");
            Logger.addLog("extractNumber", "Returned null");
            return null;
        }
    }

    public static Map<String, Integer> parseTime(String input, boolean minutesOnly) {
        System.out.println("Time received: " + input);
        Map<String, Integer> timeMap = new HashMap<>();
        input = input.replace(" ","");//remove spaces
        Integer h = null;
        Integer m = null;

        int hIndex = input.indexOf("h");
        int mIndex = input.indexOf("m");

        if (hIndex >= 0) {
            int i = hIndex - 1;
            while (i >= 0 && Character.isDigit(input.charAt(i))) {
                i--;
            }
            if (i != hIndex - 1) {
                h = Integer.parseInt(input.substring(i + 1, hIndex));
            }
        }

        if (mIndex >= 0) {
            int i = mIndex - 1;
            while (i >= 0 && Character.isDigit(input.charAt(i))) {
                i--;
            }
            if (i != mIndex - 1) {
                m = Integer.parseInt(input.substring(i + 1, mIndex));
            }
        }
        //Correct too many minutes
        while(m>60){
            h++;
            m=m-60;
        }
        timeMap.put("h", h);
        timeMap.put("m", m);

        if (minutesOnly) {
            if(h==null || h==0) {
                timeMap.put("m", m);
            }
            else {
                timeMap.remove("h");
                timeMap.put("m", m + (h * 60));
            }
        }
        System.out.println("Returning" + timeMap);

        return timeMap;
    }



    //Create Filter Map
    public static List<Map<?,?>> filterAccoms(List<Map<?,?>> accoms, Map<String,String> filters){
        ArrayList<Map<?,?>> filteredAccoms = new ArrayList<>();

        boolean noFail = true; // Shifts false on failed query

        // DONE ljdzed make this one function that takes a list<map<string,List<string>>> and sorts intelligently (maybe) ex. of map in this list :  <"TV_Room", {"tv", "television"}> to allow for better filtering

        if (filters.containsKey("Amenities")){

            Logger.addLog("Amenities Query", filters.get("Amenities"));
            String amenitiesList = "";
            String query = filters.get("Amenities").toLowerCase();
            filters.remove("Amenities");

            for (Map.Entry<String, ArrayList<String>> entry : synonymMapBuilder.amenitiesSynonym.entrySet()){
                //entry.getKey();
                boolean matchSyn = false;
                for (String synonym : entry.getValue()){
                    if (query.contains(synonym)){
                        matchSyn = true;
                        break;
                    }
                }
                if (matchSyn){
                    amenitiesList += entry.getKey() + " ";
                    filters.put(entry.getKey(), entry.getKey().replace("_", " "));
                }
            }

            Logger.addLog("Processed Amenities Query", amenitiesList);
        }
        // after to show accurate amount of filters
        ArrayList<ArrayList<Map<?,?>>> filterAccomsStrikeList = new ArrayList<>();
        int limitStrikeListSize = filters.size();
        if (filters.keySet().contains("HighestPrice")){
            limitStrikeListSize++;
        }
        for (int i = 0; i < limitStrikeListSize; i++){
            ArrayList<Map<?,?>> tmpFilter = new ArrayList<Map<?,?>>();
            try{
               filterAccomsStrikeList.add(i, tmpFilter);
            } catch (Exception e){
                e.printStackTrace();
                Logger.addLog("Strike List","Failed StrikeList Creation");
            }
        }

        int strikes; // reflect number of fails to sort by
        int maxStrikes = filters.size(); //

        // Define all non negotiables (do not care about strike system)

        for (Map<?,?> building : accoms){       // iterate through every student residence as a Map<>
            strikes = 0;
            for (String column : filters.keySet()){     // iterate through every filter key as a String


                if (column.equals("HighestPrice")){
                    String priceHigh = building.get(column).toString();
                    if (!priceHigh.equals("")){
                        priceHigh = priceHigh.replace("€", "");
                        float costHigh = Float.parseFloat(priceHigh);
                        if (Float.parseFloat(filters.get(column)) < costHigh ){
                            strikes++;
                            System.out.println("priceHigh:" + priceHigh);
                        }
                    } else {
                        strikes++;
                    }
                    String priceLow = building.get("LowestPrice").toString();

                    if (!priceLow.equals("")){
                        priceLow = priceLow.replace("€", "");

                        float costLow = Float.parseFloat(priceLow);
                        if (Float.parseFloat(filters.get(column)) < costLow ){
                            strikes++;
                            System.out.println("priceLow:" + priceLow);
                        }
                    } else {
                        strikes++;
                    }


                } else if (building.containsKey(column) && !filters.get(column).equals("")){   // If the filter is not "" and the building map contains the key of hte filler, continue
                    if (building.get(column).toString().equalsIgnoreCase(filters.get(column).toString())){      // If the values of both the filter and building map are equal, continue
                        System.out.println("Query for " + column +": " + building.get(column)+ " Passed");      // Output for monitoring API calls
                        //Logger.addLog("filterMap", "Query for " + column +": " + building.get(column)+ " Passed");             // adds to logger
                    } else {
                        if(nonNegotiable.contains(column)){
                            System.out.println("Query for " + column + ": " + building.get(column)+ " Failed" );    // Output for monitoring API calls
                            System.out.println("Wanted " + column + ": " + filters.get(column));    // Output for monitoring API calls
                            //Logger.addLog("filterMap", "Query for " + column +": " + building.get(column)+ " Failed. Wanted "+ column + ": " + filters.get(column));             // adds to logger
                            noFail = false;    // Fail this residence
                            break; // Exit for loop upon failure
                        } else {
                            if (strikes < maxStrikes){
                                strikes++; // increment the strikes against this site
                            }
                        }

                    }
                } else {
                    System.out.println("Invalid Query");    // Output for monitoring API calls
                }
            }
            if (!noFail){       // if noFail is false
                noFail = true;      // Reset to true after a fail
                System.out.println("Residence "+ building.get("Site") +" Not Matched\n");     // Output for monitoring API calls
                //Logger.addLog("filterMap", "Residence "+ building.get("Site") +" Not Matched");             // adds to logger
            } else {
                if (strikes == 0){
                    filteredAccoms.add(building);   // Add successful building to return List<Map<?,?>>
                } else {
                    try {
                        filterAccomsStrikeList.get(strikes-1).add(building);
                    } catch (IndexOutOfBoundsException i) {
                        i.printStackTrace();
                        strikes--;
                        for ( int iter = strikes; iter >=0; iter--){
                            try {
                                filterAccomsStrikeList.get(iter-1).add(building);
                            } catch (IndexOutOfBoundsException e){e.printStackTrace();}
                        }

                    }
                }
                System.out.println("Residence "+ building.get("Site") +" Matched with " + strikes +" strikes\n");         // Output for monitoring API calls
                //Logger.addLog("filterMap", "Residence "+ building.get("Site") +" Matched");             // adds to logger
            }
        }

        //for (int i = 0; i <filterAccomsStrikeList.size(); i++){
        for (ArrayList<Map<?,?>> StrikeList : filterAccomsStrikeList){
            if (filters.containsKey("HighestPrice")){
                StrikeList =orderAccommodations(StrikeList, "HighestPrice");
            }
            filteredAccoms.addAll(StrikeList);
        }

        return filteredAccoms;  // return the shortened list of accoms that match the queries.
    }


    public static float averageIndexValue(ArrayList<Map<?,?>> list, int index, String key1, String key2, ArrayList<String> removeChars){
        float average;
        if (!list.get(index).get(key1).toString().isBlank() && !list.get(0).get(key2).toString().isBlank()){
            String key1String = list.get(index).get(key1).toString();
            for (String remove : removeChars){
                key1String = key1String.replace(remove, "");
            }
            String key2String = list.get(index).get(key2).toString();
            for (String remove : removeChars){
                key2String = key2String.replace(remove, "");
            }
            average = (Float.parseFloat(key1String) + Float.parseFloat(key2String))/2;
        } else if(!list.get(index).get(key1).toString().isBlank()) {
            String key1String = list.get(index).get(key1).toString();
            for (String remove : removeChars){
                key1String = key1String.replace(remove, "");
            }
            average = Float.parseFloat(key1String);
        } else if(!list.get(index).get(key2).toString().isBlank()) {
            String key2String = list.get(index).get(key1).toString();
            for (String remove : removeChars){
                key2String = key2String.replace(remove, "");
            }
            average = Float.parseFloat(key2String);
        } else {
            average = 999999; // Puts Last w/ unrealistic price weight
        }
        return average;
    }


    // Use Merge Sort on Accomodation lists ("Highest Price" implementation currently)
    public static ArrayList<Map<?,?>> orderAccommodations(ArrayList<Map<?,?>> accommodationList, String key){
        if (key.equals("HighestPrice")){
            ArrayList<String> removeChars = new ArrayList<>();
            removeChars.add("€");
            String key1 = "HighestPrice";
            String key2 = "LowestPrice";
            if (accommodationList.size()>=3){

                // split size 3 and up lists

                ArrayList<Map<?,?>> rightList = new ArrayList<>();
                ArrayList<Map<?,?>> leftList = new ArrayList<>();
                // Construct left/right lists
                for (int i = 0; i < accommodationList.size(); i++ ){
                    if (i < accommodationList.size()/2){
                        leftList.add(accommodationList.get(i));
                    } else {
                        rightList.add(accommodationList.get(i));
                    }
                }

                leftList = orderAccommodations(leftList, key);
                rightList = orderAccommodations(rightList, key);

                ArrayList<Map<?,?>> returnList = new ArrayList<>();


                while (!leftList.isEmpty() || !rightList.isEmpty()){
                    // get left and right values
                    if (!leftList.isEmpty() & !rightList.isEmpty()){
                        float averageFirstLeft;
                        // DONE turn this into unique function to avoid duplicate code
                        averageFirstLeft =  averageIndexValue(leftList,0, key1, key2, removeChars);

                        float averageFirstRight;
                        averageFirstRight = averageIndexValue(rightList,0, key1, key2, removeChars);
                        //if (!rightList.get(0).get("HighestPrice").toString().isBlank() && !rightList.get(0).get("HighestPrice").toString().isBlank()){
                        //    averageFirstRight = (Float.parseFloat(rightList.get(0).get("HighestPrice").toString().replace("€", "")) + Float.parseFloat(rightList.get(0).get("LowestPrice").toString().replace("€", "")))/2;
                        //} else if(!rightList.get(0).get("HighestPrice").toString().isBlank()) {
                        //    averageFirstRight = Float.parseFloat(rightList.get(0).get("HighestPrice").toString().replace("€", ""));
                        //} else if(!rightList.get(0).get("LowestPrice").toString().isBlank()) {
                        //    averageFirstRight = Float.parseFloat(rightList.get(0).get("LowestPrice").toString().replace("€", ""));
                        //} else {
                        //    averageFirstRight = 9999; // Puts Last w/ unrealistic price weight
                        //}

                        if(averageFirstLeft < averageFirstRight){
                            //System.out.println("ADD Left PRICE " + averageFirstLeft);
                            returnList.add(leftList.get(0));
                            leftList.remove(0);
                        } else {
                            //System.out.println("ADD Right PRICE " + averageFirstRight);
                            returnList.add(rightList.get(0));
                            rightList.remove(0);

                        }
                    } else if(!leftList.isEmpty()){
                        //System.out.println("ADD Left PRICE" + "");
                        returnList.add(leftList.get(0));
                        leftList.remove(0);
                    } else {
                        returnList.add(rightList.get(0));
                        //System.out.println("ADD Right PRICE" +rightList.get(0).get("LowestPrice"));
                        rightList.remove(0);
                    }

                }
                System.out.println("");
                return returnList;

            } else if (accommodationList.size() == 2){
                // Order list of size 2

                float average0;
                average0 = averageIndexValue(accommodationList,0, key1, key2, removeChars);

                float average1;
                average1 = averageIndexValue(accommodationList,0, key1, key2, removeChars);


                System.out.println(average1 + " > "  + average0);
                if (average1 > average0){
                    //System.out.println("fixed big: " + average1 + " > "  + average0);
                    //System.out.println("0: LOW PRICE" + accommodationList.get(0).get("LowestPrice"));
                    //System.out.println("1: LOW PRICE" + accommodationList.get(1).get("LowestPrice"));
                    //System.out.println("");
                    return accommodationList;
                } else {
                    ArrayList<Map<?,?>> returnList = new ArrayList<>();
                    returnList.add(0,accommodationList.get(1));
                    returnList.add(1,accommodationList.get(0));
                    //System.out.println("fixed small: " + average0 + " > "  + average1);
                    //System.out.println("0: LOW PRICE" + returnList.get(0).get("LowestPrice"));
                    //System.out.println("1: LOW PRICE" + returnList.get(1).get("LowestPrice"));
                    //System.out.println("");
                    return returnList;
                }
            } else {
                // return list of size 1
                //System.out.println("0: LOW PRICE" + accommodationList.get(0).get("LowestPrice"));
                //System.out.println("");
                return accommodationList;
            }
        }

        return accommodationList;
    }

    public static String packageJsonResidence(String json){
        //String packaged = "{\"Residences\":"+json+"}";
        return "{\"Residences\":"+json+"}";
    }

    public static String addCount(String jsonresponse){
        int siteCount = JSONParser.countProperties(jsonresponse);
        StringBuilder sb = new StringBuilder(jsonresponse);
        sb.insert(sb.length() - 1, ",\n\"Count\":"+siteCount);
        //String result = sb.toString();
        return sb.toString();
    }


    //Convert our queries to JSON
    public static String convertToJsonList(List<Map<?, ?>> accoms) {
        try {
            // Create an ObjectMapper object
            ObjectMapper mapper = new ObjectMapper();
            // Use the ObjectMapper to convert the list to a JSON formatted string
            String json = mapper.writeValueAsString(accoms); // ] [
            return  packageJsonResidence(json);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    //Overload
    //Convert our queries to JSON
    public static String convertToJson(Map<?, ?> accoms) {
        try {
            // Create an ObjectMapper object
            ObjectMapper mapper = new ObjectMapper();
            // Use the ObjectMapper to convert the list to a JSON formatted string
            String json = mapper.writeValueAsString(accoms);
            return  packageJsonResidence(json);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //Find results from a given column name.
    public static ArrayList<String> getFromCol(List<Map<?,?>> list, String col){
        System.out.println("Attempting to grab column: " + col);
        ArrayList<String> vals = new ArrayList<>();
        for (Map<?,?> residence: list){
            vals.add(residence.get(col).toString());
        }
        return vals;
    }

    public static String getValue(List<Map<?,?>> list, int index, String col){
        return(list.get(index).get(col)).toString();
    }
    public static String getURLFromAbbreviation(List<Map<?,?>> list, String ab){
        for(Map<?,?> residence : list){
            if (residence.get("Abbreviation").toString().equalsIgnoreCase(ab))
                    return residence.get("Site_URL").toString();
        }
        return null;
    }

    //Explained in API call.
    public static boolean hasEnsuites(List<Map<?, ?>> list, int id){
        System.out.println("Checking if ID " + id + " has Ensuite");
        System.out.println(list.get(id).toString());
        if(getValue(list, id, "Has Ensuite").equalsIgnoreCase("y")) {
            return true;
        }
        return false;
    }
    //Explained in API call.
    public static boolean hasStudios(List<Map<?, ?>> list, int id){
        System.out.println("Checking if ID " + id + " has Studio");
        System.out.println(list.get(id).toString());
        if (getValue(list, id, "Has Studio").equalsIgnoreCase("y")) {
            return true;
        }
        return false;
    }

    //NOTE - Configured to return the first site it sees with provided name. Multiple sites
    // search is not supported to not make chatbot convoluted.
    public static String getSiteInfo(List<Map<?, ?>> list, String site){
        System.out.println("Getting Site info for: " + site);
        for (Map<?,?> residence : list){
            //Only pretty way of searching for site string appropriately:
            if(residence.get("Site").toString().toLowerCase().contains(site.toLowerCase())){
                return residence.toString();
            }
        }
        return "None";
    }

    //PseudoOverload
    //NOTE - Configured to return the first site it sees with provided name. Multiple sites
    // search is not supported to not make chatbot convoluted.
    public static Map<?,?> getSiteInfoMap(List<Map<?, ?>> list, String site){
        System.out.println("Getting Site info for: " + site);
        for (Map<?,?> residence : list){
            //Only pretty way of searching for site string appropriately:
            if(residence.get("Site").toString().toLowerCase().contains(site.toLowerCase())) {
                return residence;
            }
        }
        return null;
    }

    //NOTE - Unlike getSiteInfo, this returns multiple results as brands
    //own multiple sites. This should not be returned directly in an API response.
    public static String getCompanyInfo(List<Map<?, ?>> list, String company){
        System.out.println("Getting Company info for: " + company);
        ArrayList<String> listOfCompany = new ArrayList<String>();
        for (Map<?,?> site : list){
            if (site.toString().toLowerCase().contains(company.toLowerCase())) {
                listOfCompany.add(site.toString());
            }
        }
        if(listOfCompany.size()==0)
            return "None";
        return listOfCompany.toString();
    }


}