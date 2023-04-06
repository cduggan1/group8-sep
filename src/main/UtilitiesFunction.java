package main;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UtilitiesFunction {



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


    // TODO Shorten - Liam
    //Create Filter Map
    public static List<Map<String,String>> filterAccoms(List<Map<String,String>> accoms, Map<String,String> filters){
        ArrayList<Map<String,String>> filteredAccoms = new ArrayList<>();

        boolean noFail = true; // Shifts false on failed query

        // DONE ljdzed make this one function that takes a list<map<string,List<string>>> and sorts intelligently (maybe) ex. of map in this list :  <"TV_Room", {"tv", "television"}> to allow for better filtering

        if (filters.containsKey("Amenities")){

            Logger.addLog("Amenities Query", filters.get("Amenities"));
            String amenitiesList = "";
            String query = filters.get("Amenities").toLowerCase();
            filters.remove("Amenities");

            for (Map.Entry<String, ArrayList<String>> entry : SynonymMapBuilder.amenitiesSynonym.entrySet()){
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
        ArrayList<ArrayList<Map<String,String>>> filterAccomsStrikeList = new ArrayList<>();
        int limitStrikeListSize = filters.size();
        if (filters.containsKey("HighestPrice")){
            limitStrikeListSize++;
        }
        for (int i = 0; i < limitStrikeListSize; i++){
            ArrayList<Map<String,String>> tmpFilter = new ArrayList<Map<String,String>>();
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

        for (Map<String,String> building : accoms){       // iterate through every student residence as a Map<>
            strikes = 0;

            // Put a formatted version of the amenities into the buildings object
            if (!building.containsKey("AmenitiesString")){
                building.put("AmenitiesString", amenitiesString(building));
            }

            for (String column : filters.keySet()){     // iterate through every filter key as a String


                if (column.equals("HighestPrice")){

                    String priceHigh = building.get(column);
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
                    String priceLow = building.get("LowestPrice");

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
                    if (building.get(column).equalsIgnoreCase(filters.get(column))){      // If the values of both the filter and building map are equal, continue
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
                    filteredAccoms.add(building);   // Add successful building to return List<Map<String,String>>
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
            }
        }

        //for (int i = 0; i <filterAccomsStrikeList.size(); i++){
        for (ArrayList<Map<String,String>> StrikeList : filterAccomsStrikeList){
            if (filters.containsKey("HighestPrice")){
                StrikeList =orderAccommodations(StrikeList, "HighestPrice");
            }
            filteredAccoms.addAll(StrikeList);
        }

        return filteredAccoms;  // return the shortened list of accoms that match the queries.
    }


    public static float averageIndexValue(ArrayList<Map<String,String>> list, int index, String key1, String key2, ArrayList<String> removeChars){
        float average;


        if (list.get(index).containsKey(key1+"_"+key2+"_AVG")){
            String tmp = list.get(index).get(key1+"_"+key2+"_AVG");
            for (String remove : removeChars){
                tmp = tmp.replace(remove, "");
            }
            return Float.parseFloat(tmp);
        } else {
            if (!list.get(index).get(key1).isBlank() && !list.get(0).get(key2).isBlank()){
                String key1String = list.get(index).get(key1);
                for (String remove : removeChars){
                    key1String = key1String.replace(remove, "");
                }
                String key2String = list.get(index).get(key2);
                for (String remove : removeChars){
                    key2String = key2String.replace(remove, "");
                }
                average = (Float.parseFloat(key1String) + Float.parseFloat(key2String))/2;
            } else if(!list.get(index).get(key1).isBlank()) {
                String key1String = list.get(index).get(key1);
                for (String remove : removeChars){
                    key1String = key1String.replace(remove, "");
                }
                average = Float.parseFloat(key1String);
            } else if(!list.get(index).get(key2).isBlank()) {
                String key2String = list.get(index).get(key1);
                for (String remove : removeChars){
                    key2String = key2String.replace(remove, "");
                }
                average = Float.parseFloat(key2String);
            } else {
                average = 999999; // Puts Last w/ unrealistic price weight
            }
            list.get(index).put(key1+ "_" + key2 + "_AVG","€" + average);
            return average;
        }



    }


    // Use Merge Sort on Accomodation lists ("Highest Price" implementation currently)
    public static ArrayList<Map<String,String>> orderAccommodations(ArrayList<Map<String,String>> accommodationList, String key){
        if (key.equals("HighestPrice")){
            ArrayList<String> removeChars = new ArrayList<>();
            removeChars.add("€");
            String key1 = "HighestPrice";
            String key2 = "LowestPrice";
            if (accommodationList.size()>=3){
                // split size 3 and up lists
                ArrayList<Map<String,String>> rightList = new ArrayList<>();
                ArrayList<Map<String,String>> leftList = new ArrayList<>();
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

                ArrayList<Map<String,String>> returnList = new ArrayList<>();

                while (!leftList.isEmpty() || !rightList.isEmpty()){
                    // get left and right values
                    if (!leftList.isEmpty() & !rightList.isEmpty()){
                        float averageFirstLeft = averageIndexValue(leftList,0, key1, key2, removeChars);
                        float averageFirstRight = averageIndexValue(rightList,0, key1, key2, removeChars);

                        if(averageFirstLeft < averageFirstRight){
                            returnList.add(leftList.get(0));
                            leftList.remove(0);
                        } else {
                            returnList.add(rightList.get(0));
                            rightList.remove(0);
                        }
                    } else if(!leftList.isEmpty()){
                        returnList.add(leftList.get(0));
                        leftList.remove(0);
                    } else {
                        returnList.add(rightList.get(0));
                        rightList.remove(0);
                    }
                }
                return returnList;

            } else if (accommodationList.size() == 2){
                // Order list of size 2
                float average0 = averageIndexValue(accommodationList,0, key1, key2, removeChars);
                float average1 = averageIndexValue(accommodationList,1, key1, key2, removeChars);
                if (average1 > average0){
                    return accommodationList;
                } else {
                    ArrayList<Map<String,String>> returnList = new ArrayList<>();
                    returnList.add(0,accommodationList.get(1));
                    returnList.add(1,accommodationList.get(0));
                    return returnList;
                }
            } else {
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
        int siteCount = JsonParser.countProperties(jsonresponse);
        StringBuilder sb = new StringBuilder(jsonresponse);
        sb.insert(sb.length() - 1, ",\n\"Count\":"+siteCount);
        //String result = sb.toString();
        return sb.toString();
    }


    //Convert our queries to JSON
    public static String convertToJsonList(List<Map<String, String>> accoms) {
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

    //Find results from a given column name.
    public static ArrayList<String> getFromCol(List<Map<String,String>> list, String col){
        System.out.println("Attempting to grab column: " + col);
        ArrayList<String> vals = new ArrayList<>();
        for (Map<String,String> residence: list){
            vals.add(residence.get(col));
        }
        return vals;
    }

    public static String getValue(List<Map<String,String>> list, int index, String col){
        return(list.get(index).get(col));
    }
    public static String getURLFromAbbreviation(List<Map<String,String>> list, String ab){
        for(Map<String,String> residence : list){
            if (residence.get("Abbreviation").equalsIgnoreCase(ab))
                return residence.get("Site_URL");
        }
        return null;
    }

    // Gym TV_Room Study_Space Laundry_Room Cinema_Room Rooftop_Garden Balcony Dishwasher Stovetop Cafeteria Sports_Hall Fast_WiFi Disability_Access
    public static String amenitiesString (Map<String,String> accommodationList){
        //String flavorText = "This student accommodation, "+ accommodationList.get("Site")+ ", has ";
        String flavorText = "This student accommodation has ";
        String returnAmenity = "";
        String lastAmenity = "";
        String secondLastAmenity = "";
        if (!accommodationList.get("Gym").isBlank()){
            lastAmenity ="a "+ accommodationList.get("Gym").toLowerCase();
        }
        if (!accommodationList.get("TV_Room").isBlank()){
            if (!secondLastAmenity.isBlank()){
                returnAmenity += secondLastAmenity + ", ";
            }
            secondLastAmenity = lastAmenity;
            lastAmenity = "a shared television";
        }
        if (!accommodationList.get("Study_Space").isBlank()){
            if (!secondLastAmenity.isBlank()){
                returnAmenity += secondLastAmenity + ", ";
            }
            secondLastAmenity = lastAmenity;
            lastAmenity = "a "+ accommodationList.get("Study_Space").toLowerCase();
        }
        if (!accommodationList.get("Laundry_Room").isBlank()){
            if (!secondLastAmenity.isBlank()){
                returnAmenity += secondLastAmenity+ ", ";
            }
            secondLastAmenity = lastAmenity;
            lastAmenity = "a "+ accommodationList.get("Laundry_Room").toLowerCase();
        }
        if (!accommodationList.get("Cinema_Room").isBlank()){
            if (!secondLastAmenity.isBlank()){
                returnAmenity += secondLastAmenity+ ", ";
            }
            secondLastAmenity = lastAmenity;
            lastAmenity = "a "+ accommodationList.get("Cinema_Room").toLowerCase();
        }
        if (!accommodationList.get("Rooftop_Garden").isBlank()){
            if (!secondLastAmenity.isBlank()){
                returnAmenity += secondLastAmenity+ ", ";
            }
            secondLastAmenity = lastAmenity;
            lastAmenity = "a "+ accommodationList.get("Rooftop_Garden").toLowerCase();
        }
        if (!accommodationList.get("Balcony").isBlank()){
            if (!secondLastAmenity.isBlank()){
                returnAmenity += secondLastAmenity+ ", ";
            }
            secondLastAmenity = lastAmenity;
            lastAmenity = "a "+ accommodationList.get("Balcony").toLowerCase();
        }
        if (!accommodationList.get("Dishwasher").isBlank()){
            if (!secondLastAmenity.isBlank()){
                returnAmenity += secondLastAmenity+ ", ";
            }
            secondLastAmenity = lastAmenity;
            lastAmenity = "a "+ accommodationList.get("Dishwasher").toLowerCase();
        }
        if (!accommodationList.get("Stovetop").isBlank()){
            if (!secondLastAmenity.isBlank()){
                returnAmenity += secondLastAmenity+ ", ";
            }
            secondLastAmenity = lastAmenity;
            lastAmenity = "a "+ accommodationList.get("Stovetop").toLowerCase();
        }
        if (!accommodationList.get("Cafeteria").isBlank()){
            if (!secondLastAmenity.isBlank()){
                returnAmenity += secondLastAmenity+ ", ";
            }
            secondLastAmenity = lastAmenity;
            lastAmenity = "a "+ accommodationList.get("Cafeteria").toLowerCase();
        }
        if (!accommodationList.get("Sports_Hall").isBlank()){
            if (!secondLastAmenity.isBlank()){
                returnAmenity += secondLastAmenity+ ", ";
            }
            secondLastAmenity = lastAmenity;
            lastAmenity = "a "+ accommodationList.get("Sports_Hall").toLowerCase();
        }
        if (!accommodationList.get("Fast_WiFi").isBlank()){
            if (!secondLastAmenity.isBlank()){
                returnAmenity += secondLastAmenity+ ", ";
            }
            secondLastAmenity = lastAmenity;
            lastAmenity = accommodationList.get("Fast_WiFi").toLowerCase();
        }
        if (!accommodationList.get("Disability_Access").isBlank()){
            if (!secondLastAmenity.isBlank()){
                returnAmenity += secondLastAmenity+ ", ";
            }
            secondLastAmenity = lastAmenity;
            lastAmenity = accommodationList.get("Disability_Access").toLowerCase();
        }


        if (lastAmenity.isBlank()){
            return "Apologies, the information on this student accommodation is incomplete.";
        } else if (secondLastAmenity.isBlank()){
            return flavorText + lastAmenity + ".";
        } else if (returnAmenity.isBlank()){
            return flavorText + secondLastAmenity + " and " + lastAmenity + ".";
        } else {
            return flavorText + returnAmenity + secondLastAmenity + ", and " + lastAmenity + ".";
        }

    }


    //Explained in API call.  (Checks for EnSuites)
    public static boolean hasEnsuites(List<Map<String, String>> list, int id){
        System.out.println("Checking if ID " + id + " has Ensuite");
        System.out.println(list.get(id).toString());
        if(getValue(list, id, "HasEnsuite").equalsIgnoreCase("y")) {
            return true;
        }
        return false;
    }
    //Explained in API call. (Checks for Studios)
    public static boolean hasStudios(List<Map<String, String>> list, int id){
        System.out.println("Checking if ID " + id + " has Studio");
        System.out.println(list.get(id).toString());
        if (getValue(list, id, "HasStudio").equalsIgnoreCase("y")) {
            return true;
        } else {
            return false;
        }
    }

    //NOTE - Configured to return the first site it sees with provided name. Multiple sites
    // search is not supported to not make chatbot convoluted.
    public static String getSiteInfo(List<Map<String, String>> list, String site){
        System.out.println("Getting Site info for: " + site);
        for (Map<String,String> residence : list){
            //Only pretty way of searching for site string appropriately:
            if(residence.get("Site").toLowerCase().contains(site.toLowerCase())){
                return residence.toString();
            }
        }
        return "None";
    }

    //PseudoOverload
    //NOTE - Configured to return the first site it sees with provided name. Multiple sites
    // search is not supported to not make chatbot convoluted.
    public static Map<String,String> getSiteInfoMap(List<Map<String, String>> list, String site){
        System.out.println("Getting Site info for: " + site);
        for (Map<String,String> residence : list){
            //Only pretty way of searching for site string appropriately:
            if(residence.get("Site").toLowerCase().contains(site.toLowerCase())) {
                return residence;
            }
        }
        return null;
    }

    //NOTE - Unlike getSiteInfo, this returns multiple results as brands
    //own multiple sites. This should not be returned directly in an API response.
    public static String getCompanyInfo(List<Map<String, String>> list, String company){
        System.out.println("Getting Company info for: " + company);
        ArrayList<String> listOfCompany = new ArrayList<String>();
        for (Map<String, String> site : list){
            if (site.toString().toLowerCase().contains(company.toLowerCase())) {
                listOfCompany.add(site.toString());
            }
        }
        if(listOfCompany.size()==0)
            return "None";
        return listOfCompany.toString();
    }

}
