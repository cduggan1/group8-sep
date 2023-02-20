package main;

import java.io.File;
import java.io.IOException;
import java.sql.Array;
import java.util.*;

import static spark.Spark.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.csv.*;
import com.github.alexdlaird.ngrok.NgrokClient;
import com.github.alexdlaird.ngrok.protocol.CreateTunnel;
import com.github.alexdlaird.ngrok.protocol.Proto;
import com.github.alexdlaird.ngrok.protocol.Tunnel;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {
    public static boolean tunnelNgrok = true;
    public static List<Map<?, ?>> accoms = buildObject("src/main/info.csv");

    //Provided we built the initial object correctly, start
    //program and initialise API responses.
    public static void main(String[] args) throws IOException {


        if(accoms==null){
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


            // Old in-Query processing -- Copied so own function filterQueries
            /*
            ArrayList<Map<?,?>> filteredAccoms = new ArrayList<>();
            boolean noFail = true; // shifts false of failed query
            for (Map<?,?> building : accoms){
                for (String column : filters.keySet()){
                    if (building.containsKey(column) && !filters.get(column).equals("")){
                        if (building.get(column).toString().toLowerCase().equals(filters.get(column).toString().toLowerCase())){
                            System.out.println("Query Passed");
                        } else {
                            System.out.println("Query Failed");
                            noFail = false;
                            break;
                        }
                    } else {
                        System.out.println("Invalid Query");
                    }
                }
                if (!noFail){
                    noFail = true;
                } else {
                    filteredAccoms.add(building);
                }
            }
            */



            // Convert the filtered list to a JSON formatted string
            String json = convertToJson(filterAccoms(accoms, filters));

            // Set the content type of the response to JSON
            res.type("application/json");

            return json;
        });



        //Receives -> ID
        //Returns -> Site at index (ID) in list.
        get("/id/:index", (req,res)->{
            try {
                int index = Integer.parseInt(req.params(":index"));
                if(index<accoms.size()) {
                    System.out.println("Index requested: " + index);
                    String json = convertToJson(accoms.get(index));
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
            String json = convertToJson(accoms);
            res.type("application/json");
            //Return json
            return json;
        });

        //Receives->Site name
        //Returns->Rows which contain "name" in the Site column
        get("/name/:name", (req,res)->{
            try {
                String name = req.params(":name");
                System.out.println("Name requested: " + name);
                res.type("application/json");
                if (getSiteInfoMap(accoms, req.params(":name")) != null){
                    return convertToJson(getSiteInfoMap(accoms, req.params(":name")));
                }
                return "No Matches in Database";
            } catch (Exception e) {
                return "Invalid input";
            }

        });

        //Receives->Brand name
        //Returns->Rows which contain "brand" in the Brand column
        get("/brand/:brand", (req,res)->{
            Map<String, String> filters = new HashMap<>();
            for (String key : req.queryParams()) {
                filters.put(key, req.queryParams(key));
            }
            // Convert the filtered list to a JSON formatted string
            String json = convertToJson(filterAccoms(accoms, filters));

            // Set the content type of the response to JSON
            res.type("application/json");

            return json;

        });

        //Receives->an index and the hasStudio check.
        //Returns->Whether "Yes" or not "No" the provided index
        //has studios.
        get("/id/:index/hasStudio", (req,res)->{
            int index = Integer.parseInt(req.params(":index"));
            if (hasStudios(accoms, index)){
                return "Yes";
            }else {
                return "No";
            }
        });

        //Receives->an index and the hasEnsuite check.
        //Returns->Whether "Yes" or not "No" the provided index
        //has ensuites.
        get("/id/:index/hasEnsuite", (req,res)->{
            int index = Integer.parseInt(req.params(":index"));
            if (hasEnsuites(accoms, index)){
                return "Yes";
            }else {
                return "No";
            }
        });


        get("/othersite/:site", (req,res)->{
            return getOtherSitesFromBrand(accoms, req.params(":site").toString());
        });

        //End of API calls.


        //Assign temp URL using ngrok
        if(tunnelNgrok) {
            try {
                final NgrokClient ngrokClient = new NgrokClient.Builder().build();
                //Don't leak this auth token.
                ngrokClient.setAuthToken("2LSQAcGXtCXfmAcvQ6dhKaOg9z9_242ur7wT27KLcYjR1PBzh");

                final CreateTunnel sshCreateTunnel = new CreateTunnel.Builder()
                        .withProto(Proto.HTTP)
                        .withAddr(4567)
                        .build();
                final Tunnel httpTunnel = ngrokClient.connect(sshCreateTunnel);

                String url = httpTunnel.getPublicUrl();
                System.out.println("Url for API: " + url);

            } catch (Exception e) {
                System.out.println("ERROR, ngrok failed");
            }
        }
    }





    //Start of functions.

// Unfinished function held back by issues with Set<String> inherent java implementaiton.
/*
    public static Map<?,?> createFilter(Set<String> filter){
        Map<String, String> filters = new HashMap<>();
        for (String key : filter) {
            filters.put(key, filter.get(key));
        }
        System.out.println(filters.toString());
        return null;
    }
*/

    //Create Filter Map
    public static List<Map<?,?>> filterAccoms(List<Map<?,?>> accoms, Map<String,String> filters){
        ArrayList<Map<?,?>> filteredAccoms = new ArrayList<>();
        boolean noFail = true; // shifts false of failed query

        for (Map<?,?> building : accoms){

            for (String column : filters.keySet()){
                if (building.containsKey(column) && !filters.get(column).equals("")){
                    if (building.get(column).toString().toLowerCase().equals(filters.get(column).toString().toLowerCase())){
                        System.out.println("Query Passed");
                    } else {
                        System.out.println("Query Failed");
                        noFail = false;
                        break;
                    }

                } else {
                    System.out.println("Invalid Query");
                }
            }
            if (!noFail){
                noFail = true;
            } else {
                filteredAccoms.add(building);
            }

        }

        return filteredAccoms;
    }



    //Convert our queries to JSON
    public static String convertToJson(List<Map<?, ?>> accoms) {
        try {
            // Create an ObjectMapper object
            ObjectMapper mapper = new ObjectMapper();
            // Use the ObjectMapper to convert the list to a JSON formatted string
            String json = mapper.writeValueAsString(accoms);
            return json;
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
            return json;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    //When called, will update the Object
    //in the current scope, with a provided filename.
    //It defaults to a hard-coded filename if not provided one.
    public static boolean updateObject(){try {updateObject("src/main/info.csv");return true;}catch(Exception e){return false;}}//Ignore Errors.
    public static boolean updateObject(String filename){
        try {
            accoms = buildObject(filename);
            return true;
        }catch(Exception e){return false;}//Ignore
    }

    //Object builder from CSV file
    public static List<Map<?, ?>> buildObject(String filename) {
        System.out.println("Building Object from " + filename);
        //Read file
        File input = new File(filename);
        try {
            //initialise a schema
            CsvSchema csv = CsvSchema.emptySchema().withHeader();
            //initialise a mapper
            CsvMapper csvMapper = new CsvMapper();
            //Iterate through CSV file and Map.
            MappingIterator<Map<?, ?>> mappingIterator = csvMapper.reader().forType(Map.class).with(csv).readValues(input);
            System.out.println("Object Built from " + filename + " successfully.");
            //Returns a map list.
            return mappingIterator.readAll();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    //find all sites in list.
    public static ArrayList<String> getSites(List<Map<?,?>> list){
        return getFromCol(list, "Site");
    }
    //Find all Brands in list
    public static ArrayList<String> getBrand(List<Map<?,?>> list){
        return getFromCol(list, "Brand");
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


    public static String getOtherSitesFromBrand(List<Map<?,?>> list, String site){
        //receive a site name
        //find company name from that site
        //find other sites from the same company name
        //return those sites

        System.out.println("Got site " + site);
        String brand="";
        for(int i=0;i<list.size();i++){
            if(list.get(i).get("Site").toString().toLowerCase().contains(site.toLowerCase())){
                brand = list.get(i).get("Brand").toString();
                break;
            }
        }
        //brand=
        ArrayList<String> sites = new ArrayList<>();
        for(int i=0; i<list.size(); i++){
            if(list.get(i).get("Brand").toString().toLowerCase().contains(brand.toLowerCase())){
                sites.add(list.get(i).toString());
            }
        }

        return sites.toString();
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

    //NOTE - Unlike getCompanyInfo, this returns a list<map<?,?>> with all sites belonging to brand
    //This should not be returned directly in an API response.
    public static List<Map<?, ?>> getCompanySiteList(List<Map<?, ?>> list, String company){
        System.out.println("Getting Site List for Company: " + company);

        ArrayList<Map<?,?>> listOfCompany = new ArrayList<Map<?,?>>();
        for(int i=0;i<list.size();i++) {
            if (list.get(i).toString().toLowerCase().contains(company.toLowerCase())) { // searches string of map to see if name of brand is in it
                listOfCompany.add(list.get(i));
            }
        }
        if(listOfCompany.size()==0)
            return null;
        return listOfCompany;
    }



}