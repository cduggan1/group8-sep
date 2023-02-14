package main;

import java.io.File;
import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static spark.Spark.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.csv.*;
import com.github.alexdlaird.ngrok.NgrokClient;
import com.github.alexdlaird.ngrok.protocol.CreateTunnel;
import com.github.alexdlaird.ngrok.protocol.Proto;
import com.github.alexdlaird.ngrok.protocol.Tunnel;


public class Main {
    public static boolean tunnelNgrok = false;
    public static List<Map<?, ?>> accoms = buildObject("src/main/info.csv");

    //Provided we built the initial object correctly, start
    //program and initialise API responses.
    public static void main(String[] args) throws IOException {


        if(accoms==null){
            System.out.println("Error Parsing CSV");
            //Something didn't work
            System.exit(1);
        }

        //Receives -> ID
        //Returns -> Site at index (ID) in list.
        get("/id/:index", (req,res)->{
            try {
                int index = Integer.parseInt(req.params(":index"));
                if(index<accoms.size()) {
                    System.out.print("Index requested: " + index);
                    return accoms.get(index).toString();
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
            return accoms.toString();
        });

        //Receives->Site name
        //Returns->Rows which contain "name" in the Site column
        get("/name/:name", (req,res)->{
            return getSiteInfo(accoms, req.params(":name"));
        });

        //Receives->Brand name
        //Returns->Rows which contain "brand" in the Brand column
        get("/brand/:brand", (req,res)->{
            return getCompanyInfo(accoms, req.params(":brand"));
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


}