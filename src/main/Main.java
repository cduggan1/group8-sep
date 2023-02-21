package main;

import java.io.IOException;
import java.util.*;

import static spark.Spark.*;

import com.github.alexdlaird.ngrok.NgrokClient;
import com.github.alexdlaird.ngrok.protocol.CreateTunnel;
import com.github.alexdlaird.ngrok.protocol.Proto;
import com.github.alexdlaird.ngrok.protocol.Tunnel;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {

    String url = "sql8.freemysqlhosting.net";
    String user = "sql8600069";
    String password = "aAGxAXEthq";
    String query = "SELECT * FROM mytable";

    public static boolean tunnelNgrok = false;
    //Provided we built the initial object correctly, start
    //program and initialise API responses.
    public static void main(String[] args) throws IOException {
        csvData.init("src/main/info.csv");


        if(csvData.accoms==null){
            System.out.println("Error Parsing CSV");
            //Something didn't work
            System.exit(1);
        }

        get("/fullQuery", (req, res) -> {

            System.out.println("Filtering Query...");

            // Construct a map of filters based on the query parameters
            Map<String, String> filters = new HashMap<>();
            for (String key : req.queryParams()) {
                filters.put(key, req.queryParams(key));
            }
            System.out.println(filters.toString());

            // Filter the accoms list based on the query parameters
            // Convert the filtered list to a JSON formatted string
            List<Map<?,?>> filteredAccoms = filterAccoms(csvData.accoms, filters);

            // Set the content type of the response to JSON
            res.type("application/json");

            //response = "{\"Residences\":"+response+"}";
            return convertToJsonList(filteredAccoms);

        });

        //Receives -> ID
        //Returns -> Site at index (ID) in list.
        get("/id/:index", (req,res)->{
            try {
                int index = Integer.parseInt(req.params(":index"));
                if(index<csvData.accoms.size()) {
                    System.out.println("Index requested: " + index);
                    String json = convertToJson(csvData.accoms.get(index));
                    res.type("application/json");
                    return json;
                }else{
                    return "Out of Bounds";
                }
            }catch(Exception e){
                return "Invalid input";
            }
        });

        //Receives -> Ask for All
        //Returns -> Every row in list.
        get("/all", (req,res)->{
            System.out.println("Requested All");
            //Filter object
            //---NO FILTER REQUIRED AS RETURNING ALL----
            //Pass object into method to new string (json).
            String json = convertToJsonList(csvData.accoms);
            res.type("application/json");
            //Return json
            System.out.println(json.toString());
            return json;
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
        boolean noFail = true; // shifts false of failed query
        for (Map<?,?> building : accoms){
            for (String column : filters.keySet()){
                if (building.containsKey(column) && !filters.get(column).equals("")){
                    if (building.get(column).toString().equalsIgnoreCase(filters.get(column).toString())){
                        System.out.println("Query for " + column +": " + building.get(column)+ " Passed");
                    } else {
                        System.out.println("Query for " + column + ": " + building.get(column)+ " Failed" );
                        System.out.println("Wanted " + column + ": " + filters.get(column));
                        noFail = false;
                        break;
                    }
                } else {
                    System.out.println("Invalid Query");
                }
            }
            if (!noFail){
                noFail = true;
                System.out.println("Residence "+ building.get("Site") +" Not Matched\n\n");

            } else {
                filteredAccoms.add(building);
                System.out.println("Residence "+ building.get("Site") +" Matched\n\n");
            }
        }
        return filteredAccoms;
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
        for(int i=0;i<list.size(); i++){
            vals.add(list.get(i).get(col).toString());
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
        if(getValue(list, id, "Has Studio").equalsIgnoreCase("y"))
            return true;
        return false;
    }

    //NOTE - Configured to return the first site it sees with provided name. Multiple sites
    // search is not supported to not make chat-bot convoluted.
    public static String getSiteInfo(List<Map<?, ?>> list, String site){
        System.out.println("Getting Site info for: " + site);
        for(int i=0;i<list.size();i++){
            //Only pretty way of searching for site string appropriately:
            if(list.get(i).get("Site").toString().toLowerCase().contains(site.toLowerCase())){
                return list.get(i).toString();
            }
        }
        return "None";
    }

    //PseudoOverload
    //NOTE - Configured to return the first site it sees with provided name. Multiple sites
    // search is not supported to not make chat-bot convoluted.
    public static Map<?,?> getSiteInfoMap(List<Map<?, ?>> list, String site){
        System.out.println("Getting Site info for: " + site);
        for(int i=0;i<list.size();i++){
            //Only pretty way of searching for site string appropriately:
            if(list.get(i).get("Site").toString().toLowerCase().contains(site.toLowerCase())){
                return list.get(i);
            }
        }
        return null;
    }

    //NOTE - Unlike getSiteInfo, this returns multiple results as brands
    //own multiple sites. This should not be returned directly in an API response.
    public static String getCompanyInfo(List<Map<?, ?>> list, String company){
        System.out.println("Getting Company info for: " + company);
        ArrayList<String> listOfCompany = new ArrayList<String>();
        for(int i=0;i<list.size();i++) {
            if (list.get(i).toString().toLowerCase().contains(company.toLowerCase())) {
                listOfCompany.add(list.get(i).toString());
            }
        }
        if(listOfCompany.size()==0)
            return "None";
        return listOfCompany.toString();
    }


}