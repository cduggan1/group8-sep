import java.io.File;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static spark.Spark.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.csv.*;
//
public class Main {
    public static List<Map<?, ?>> accoms = buildObject("src/info.csv");


    public static void main(String[] args) {
        if(accoms==null){
            System.out.printf("Error Parsing CSV");
            System.exit(1);
        }

        //Receives -> ID
        //Returns -> Site at index (ID) in list.
        get("/id/:index", (req,res)->{
                int index = Integer.parseInt(req.params(":index"));
                System.out.print("Index requested: " + index);
                return accoms.get(index).toString();
            });

        //Receives -> Ask for All
        //Returns -> Every row in list.
        get("/all", (req,res)->{
            System.out.print("Requested All");
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
        get("/id/:index/:hasStudio", (req,res)->{
            int index = Integer.parseInt(req.params(":index"));
            System.out.print("Studio Check for index: " + index);
            if (hasStudios(accoms, index)){
                return "Yes";
            }else {
                return "No";
            }
        });

        //Receives->an index and the hasEneuite check.
        //Returns->Whether "Yes" or not "No" the provided index
        //has ensuites.
        get("/id/:index/:hasEnsuite", (req,res)->{
            int index = Integer.parseInt(req.params(":index"));
            System.out.print("Ensuite Check for index: " + index);
            if (hasEnsuites(accoms, index)){
                return "Yes";
            }else {
                return "No";
            }
        });

        //End of API calls.
    }

    //Start of functions.

    //When called, will update the Object
    //in the current scope, with a provided filename.
    //It defaults to a hard-coded filename if not provided one.
    public static void updateObject(){try {updateObject("src/info.csv");}catch(Exception e){}}//Ignore Errors.
    public static void updateObject(String filename){
        try {
            accoms = buildObject(filename);
        }catch(Exception e){}//Ignore
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
        ArrayList<String> vals = new ArrayList<>();
        for(int i=0;i<list.size(); i++){
            vals.add(list.get(i).get(col).toString());
        }
        return vals;
    }

    //Explained in API call.
    public static boolean hasEnsuites(List<Map<?, ?>> list, int id){
        return(list.get(id).toString().contains("Has Ensuite=y"));
    }
    //Explained in API call.
    public static boolean hasStudios(List<Map<?, ?>> list, int id){
        return(list.get(id).toString().contains("Has Studio=y"));
    }

    //NOTE - Configured to return the first site it sees with provided name. Multiple sites
    // search is not supported to not make chat-bot convoluted.
    public static String getSiteInfo(List<Map<?, ?>> list, String site){
        for(int i=0;i<list.size();i++){
            //Only pretty way of searching for site string appropriately:
            if(list.get(i).get("Site").toString().toLowerCase().contains(site.toLowerCase())){
                return list.get(i).toString();
            }
        }
        return "None";
    }

    //NOTE - Unlike getSiteInfo, this returns multiple results as brands
    //own multiple sites. This should not be returned directly in an API response.
    public static String getCompanyInfo(List<Map<?, ?>> list, String company){
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