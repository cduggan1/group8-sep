package main;

import java.io.IOException;
import java.util.*;

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
    //Provided we built the initial object correctly, start
    //program and initialise API responses.
    public static void main(String[] args) throws IOException {
        csvData.init("info.csv");

        if(csvData.accoms==null){
            System.out.println("Error Parsing CSV");
            Logger.addLog("Init", "CSV Error");
            //Something didn't work
            System.exit(1);
        }

        get("/fullQuery", (req, res) -> {

            System.out.println("Filtering Query...");
            Logger.addLog("fullQuery", "API Called");


            // Construct a map of filters based on the query parameters
            Map<String, String> filters = new HashMap<>();
            for (String key : req.queryParams()) {
                filters.put(key, req.queryParams(key));
            }
            System.out.println(filters.toString());
            if(filters.size()>0) {
                Logger.addLog("fullQuery", "FILTERS ADDED:" + filters.toString());
            }else{
                Logger.addLog("fullQuery", "No filters specified.");
            }

            // Filter the accoms list based on the query parameters
            // Convert the filtered list to a JSON formatted string
            List<Map<?,?>> filteredAccoms = filterAccoms(csvData.accoms, filters);

            // Set the content type of the response to JSON
            res.type("application/json");

            //response = "{\"Residences\":"+response+"}";
            String response = convertToJsonList(filteredAccoms);

            System.out.println("RESPONSE" + response);
            Logger.addLog("RESPONSE" , response);
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
                return Logger.logFile.toString();
            }else{
                return "Logging Disabled.";
            }
                });

        //End of API calls.

        //Assign temp URL using ngrok
        if(tunnelNgrok) {
            ngrokTunnel.startNgrok("");
        }
    }

    //Create Filter Map
    public static List<Map<?,?>> filterAccoms(List<Map<?,?>> accoms, Map<String,String> filters){
        ArrayList<Map<?,?>> filteredAccoms = new ArrayList<>();
        boolean noFail = true; // Shifts false on failed query
        for (Map<?,?> building : accoms){       // iterate through every student residence as a Map<>
            for (String column : filters.keySet()){     // iterate through every filter key as a String
                if (building.containsKey(column) && !filters.get(column).equals("")){   // If the filter is not "" and the building map contains the key of hte filler, continue
                    if (building.get(column).toString().equalsIgnoreCase(filters.get(column).toString())){      // If the values of both the filter and building map are equal, continue
                        System.out.println("Query for " + column +": " + building.get(column)+ " Passed");      // Output for monitoring API calls
                    } else {
                        System.out.println("Query for " + column + ": " + building.get(column)+ " Failed" );    // Output for monitoring API calls
                        System.out.println("Wanted " + column + ": " + filters.get(column));    // Output for monitoring API calls
                        noFail = false;    // Fail this residence
                        break; // Exit for loop upon failure
                    }
                } else {
                    System.out.println("Invalid Query");    // Output for monitoring API calls
                }
            }
            if (!noFail){       // if noFail is false
                noFail = true;      // Reset to true after a fail
                System.out.println("Residence "+ building.get("Site") +" Not Matched\n\n");     // Output for monitoring API calls
            } else {
                filteredAccoms.add(building);   // Add successful building to return List<Map<?,?>>
                System.out.println("Residence "+ building.get("Site") +" Matched\n\n");         // Output for monitoring API calls
            }
        }
        return filteredAccoms;  // return the shortened list of accoms that match the queries.
    }

    public static String packageJsonResidence(String json){
        return "{\"Residences\":"+json+"}";
    }


    //Convert our queries to JSON
    public static String convertToJsonList(List<Map<?, ?>> accoms) {
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

    //Explained in API call.
    public static boolean hasEnsuites(List<Map<?, ?>> list, int id){
        System.out.println("Checking if ID " + id + " has Ensuite");
        System.out.println(list.get(id).toString());
        if(getValue(list, id, "Has Ensuite").equalsIgnoreCase("y"))
            return true;
        return false;
    }
    //Explained in API call.
    public static boolean hasStudios(List<Map<?, ?>> list, int id){
        System.out.println("Checking if ID " + id + " has Studio");
        System.out.println(list.get(id).toString());
        if(getValue(list, id, "Has Studio").equalsIgnoreCase("y")) {
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