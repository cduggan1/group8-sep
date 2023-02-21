package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class redundantApiCalls {
    //Receives->Site name
    //Returns->Rows which contain "name" in the Site column
//    get("/name/:name", (req,res)->{
//        try {
//            String name = req.params(":name");
//            System.out.println("Name requested: " + name);
//            res.type("application/json");
//            if (getSiteInfoMap(csvData.accoms, req.params(":name")) != null){
//                return convertToJson(getSiteInfoMap(csvData.accoms, req.params(":name")));
//            }
//            return "No Matches in Database: " + name;
//        } catch (Exception e) {
//            return "Error: Invalid input";
//        }
//    });
//
//    //Receives->Brand name
//    //Returns->Rows which contain "brand" in the Brand column
//    get("/brand/:brand", (req,res)->{
//        Map<String, String> filters = new HashMap<>();
//        for (String key : req.queryParams()) {
//            filters.put(key, req.queryParams(key));
//        }
//        // Convert the filtered list to a JSON formatted string
//        String json = convertToJsonList(filterAccoms(csvData.accoms, filters));
//
//        // Set the content type of the response to JSON
//        res.type("application/json");
//
//        return json;
//
//    });
//
//    //Receives->an index and the hasStudio check.
//    //Returns->Whether "Yes" or not "No" the provided index
//    //has studios.
//    get("/id/:index/hasStudio", (req,res)->{
//        int index = Integer.parseInt(req.params(":index"));
//        if (hasStudios(csvData.accoms, index)){
//            return "Yes";
//        }else {
//            return "No";
//        }
//    });
//
//    //Receives->an index and the hasEnsuite check.
//    //Returns->Whether "Yes" or not "No" the provided index
//    //has ensuites.
//    get("/id/:index/hasEnsuite", (req,res)->{
//        int index = Integer.parseInt(req.params(":index"));
//        if (hasEnsuites(csvData.accoms, index)){
//            return "Yes";
//        }else {
//            return "No";
//        }
//    });
//
//
//    get("/othersite/:site", (req,res)->{
//        return getOtherSitesFromBrand(csvData.accoms, req.params(":site").toString());
//    });

//    //find all sites in list.
//    public static ArrayList<String> getSites(List<Map<?,?>> list){
//        return getFromCol(list, "Site");
//    }
//    //Find all Brands in list
//    public static ArrayList<String> getBrand(List<Map<?,?>> list){
//        return getFromCol(list, "Brand");
//    }
//NOTE - Unlike getCompanyInfo, this returns a list<map<?,?>> with all sites belonging to brand
//This should not be returned directly in an API response.
//public static List<Map<?, ?>> getCompanySiteList(List<Map<?, ?>> list, String company){
//    System.out.println("Getting Site List for Company: " + company);
//
//    ArrayList<Map<?,?>> listOfCompany = new ArrayList<Map<?,?>>();
//    for(int i=0;i<list.size();i++) {
//        if (list.get(i).toString().toLowerCase().contains(company.toLowerCase())) { // searches string of map to see if name of brand is in it
//            listOfCompany.add(list.get(i));
//        }
//    }
//    if(listOfCompany.size()==0)
//        return null;
//    return listOfCompany;
//}
}
