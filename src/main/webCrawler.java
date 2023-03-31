package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import com.fasterxml.jackson.annotation.JsonKey;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONPropertyName;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import static main.JsonParser.countProperties;
import static main.JsonParser.findValuesOf;

public class webCrawler implements Callable {
    //Callabe is the multithreading interface we will be using
    public static String BER_Query;
    public static String Country;
    static HashMap<String, String> urlMap = new HashMap<String, String>();
    static CsvData config = new CsvData();

    static CsvData cityData = new CsvData();

    public webCrawler (String Url, String BER_Query, int index, String country){ //Constructor, which also updates shared resources for threads.
        this.BER_Query = BER_Query;
        this.Country = country;

        if (Url.contains("daft.ie")) {
            urlMap.put(Integer.toString(index), Url); //Adding the url associated with this crawler, along with an index corresponding to its thread number
        } else {
            urlMap.put(Integer.toString(index), config.value(Country,"Country","urlBase") + Url); // to a hashmap for the threads to access concurrently.
        }
    }

    public static String Daft(String parentUrl, String BER_Query, HashMap<String, String> filterMap, String search, String city) { //Main method. Handles all other methods.

        config.updateObject("src/main/config.csv");
        cityData.updateObject("src/main/cityData.csv");

        int index = 0;
        int crawlerIndex = 0;

        ArrayList<String> Pages = GetUrlList(parentUrl, filterMap, search, city); //Calling daftGetUrlList() with a parentUrl to get an arraylist of the urls for all properties.
        ArrayList<webCrawler> Crawlers = new ArrayList<>(); //Empty arraylist for instances of webCrawler class
        ArrayList<Thread> Threads = new ArrayList<>(); //Empty arraylist for threads

        String json = "{\"Residences\":["; //Start of output Json

        ArrayList<FutureTask> pageTasks = new ArrayList<>(); //Empty arraylist of type "FutureTask", which is an object specific to the Callable interface

        for (String page : Pages) {  //For each url in our returned list of property urls...
            Crawlers.add(new webCrawler(page, BER_Query, crawlerIndex, search)); //Create a new webCrawler instance, passing the current property and index
            crawlerIndex++;

            pageTasks.add(new FutureTask(Crawlers.get(Crawlers.size()-1))); //Add the new webCrawler as a new FutureTask to our arraylist

            Threads.add(new Thread(pageTasks.get(pageTasks.size()-1), Integer.toString(index))); //Pass the new FutureTask to a new Thread, and name the thread with index
            Threads.get(Threads.size()-1).start(); //Start the new thread
            index++;
        }

        for (FutureTask task : pageTasks) { //After all threads initialised, for each FutureTask...
            try {
                if (task.get() != null) {
                    json = json + task.get();//Get the response and add it to our json string
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        StringBuilder json_sb= new StringBuilder(json); //Initialise a stringbuilder equal to the json string (not synchronised, but more efficient than stringBuffer)
        if (json.contains("title")) { //If there is a property...
            json_sb.deleteCharAt(json_sb.length() - 1); //Remove the last character, which is a stray comma  >:^(
        } else {
            json_sb.append("{}"); //If there are no properties, simply add an empty json object, {}
        }
        json = json_sb.toString(); //make the json string equal to the stringBuilder we were working with
        String tempJson =  json + "]}";
        return json + "], \"Count\":" + countProperties(tempJson) + "}"; //return final json
    }



    //Method to get list of property urls from a parent/seed url
    public static ArrayList<String> GetUrlList(String parentUrl, HashMap<String, String> filterMap, String Country, String City) {

        int index = 0;

        boolean endOfList = false;

        String tempUrl = parentUrl; //storing the base string, without an index

        //Creating an empty array list to store the urls in
        ArrayList<String> urlList = new ArrayList<>();

        try {
            System.out.println(Country);
            if (Country.equals("Ireland")) {
                System.out.println("Got to daft api call");
                String prepend = "{\"section\":\"residential-to-rent\",\"filters\":[{\"name\":\"adState\",\"values\":[\"published\"]}" +
                        ",{\"values\":[\"furnished\"],\"name\":\"furnishing\"},{\"values\":[\"" + filterMap.get("propertyType=") + "\"],\"name\":\"propertyType\"}]," +
                        "\"andFilters\":[{\"values\":[" + filterMap.get("facilities=") + "],\"name\":\"facilities\"}]" +
                        ",\"ranges\":[{\"from\":\"" + filterMap.get("numBaths_from=") + "\",\"to\":\"\",\"name\":\"numBaths\"},{\"from\":\"" + filterMap.get("leaseLength_from=") + "\",\"to\":\"\",\"name\":\"leaseLength\"}," +
                        "{\"from\":\"\",\"to\":\"" + filterMap.get("rentalPrice_to=") + "\",\"name\":\"rentalPrice\"},{\"from\":\"" + filterMap.get("numBeds_from=") + "\",\"to\":\"\",\"name\":\"numBeds\"}]," +
                        "\"paging\":{\"from\":\"";

                String append = "\",\"pageSize\":\"50\"},\"geoFilter\":{\"storedShapeIds\":[\"4410\"]," +
                        "\"geoSearchType\":\"STORED_SHAPES\"},\"terms\":\"\"}";

                //Code for a hidden API call on the Daft website used to circumvent the inaccessible webpages issue.
                while (!endOfList) {
                    Connection.Response postReq = Jsoup.connect("https://gateway.daft.ie/old/v1/listings")
                            .ignoreContentType(true)
                            .header("authority", "gateway.daft.ie")
                            .header("accept", "application/json")
                            .header("accept-language", "en-US,en;q=0.9")
                            .header("brand", "daft")
                            .header("cache-control", "no-cache, no-store")
                            .header("content-type", "application/json")
                            .header("expires", "0")
                            .header("origin", "https://www.daft.ie")
                            .header("platform", "web")
                            .header("pragma", "no-cache")
                            .header("referer", "https://www.daft.ie/")
                            .header("sec-ch-ua", "Google Chrome\";v=\"111\", \"Not(A:Brand\";v=\"8\", \"Chromium\";v=\"111\"")
                            .header("sec-ch-ua-mobile", "?0")
                            .header("sec-ch-ua-platform", "\"Windows\"")
                            .header("sec-fetch-dest", "empty")
                            .header("sec-fetch-mode", "cors")
                            .header("sec-fetch-site", "same-site")
                            .header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36")
                            .requestBody(prepend + index + append)
                            .method(Connection.Method.POST)
                            .execute();

                    index = index + 50;

                    Document daftCall = postReq.parse();
                    String daftCallString = daftCall.toString();
                    Document daftCallDoc = Jsoup.parse(daftCallString);

                    StringBuilder daftCallStringBuilder = new StringBuilder(daftCallDoc.toString());
                    daftCallStringBuilder.delete(0, 30).delete(daftCallStringBuilder.length() - 16, daftCallStringBuilder.length() - 1);
                    daftCallString = daftCallStringBuilder.toString();

                    ArrayList<String> JSONParse = findValuesOf(daftCallString, "seoFriendlyPath");
                    if (!JSONParse.isEmpty()) {
                        urlList.addAll(JSONParse);
                    } else {
                        endOfList = true;
                    }
                }
            } else if (Country.equals("Abroad")) {

                String prepend = "{\"requests\":[{\"query\":\"\",\"params\":\"distinct=true&insideBoundingBox=%5B%5B";

                String coords = cityData.value(City, "City", "Coordinates"); //Fetch from CSV

                String filters = ""; //Build in Main and pass through

                String mid = "%5D%5D&facets=%5B%5D&attributesToHighlight=%5B%5D&attributesToRetrieve=%5B%22path%22%5D&hitsPerPage=100&filters=";

                String append = "isSearchable%3Atrue&page=0\",\"indexName\":\"production_listings_rank_withOrpheus\"}]}";

                Connection.Response postReq = Jsoup.connect("https://y8l112mibf-dsn.algolia.net/1/indexes/*/queries?x-algolia-agent=Algolia%20for%20JavaScript%20(4.10.5)%3B%20Browser")
                        .ignoreContentType(true)
                        .header("Accept", "*/*")
                        .header("Accept-Language", "en-US,en;q=0.9")
                        .header("Connection", "keep-alive")
                        .header("Origin", "https://housinganywhere.com")
                        .header("Referer", "https://housinganywhere.com/")
                        .header("Sec-Fetch-Dest", "empty")
                        .header("Sec-Fetch-Mode", "cors")
                        .header("Sec-Fetch-Site", "cross-site")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36")
                        .header("content-type", "application/x-www-form-urlencoded")
                        .header("sec-ch-ua", "\"Google Chrome\";v=\"111\", \"Not(A:Brand\";v=\"8\", \"Chromium\";v=\"111\"")
                        .header("sec-ch-ua-mobile", "?0")
                        .header("sec-ch-ua-platform", "\"Windows\"")
                        .header("x-algolia-api-key", "170cf5d8f85035f219107d6fb900e3dd")
                        .header("x-algolia-application-id", "Y8L112MIBF")
                        .requestBody(prepend + coords + mid + filters + append)
                        .method(Connection.Method.POST)
                        .execute();

                Document HA_Call = postReq.parse();
                String HA_CallString = HA_Call.toString();
                Document HA_CallDoc = Jsoup.parse(HA_CallString);

                StringBuilder HA_CallStringBuilder = new StringBuilder(HA_CallDoc.toString());
                HA_CallStringBuilder.delete(0, 30).delete(HA_CallStringBuilder.length() - 16, HA_CallStringBuilder.length() - 1);
                HA_CallString = HA_CallStringBuilder.toString();

                ArrayList<String> JSONParse = findValuesOf(HA_CallString, "path");
                urlList.addAll(JSONParse);

            }

            System.out.println(urlList);
            return urlList;
        }
        //Crawly Bit - Old Method
        catch(Exception e){
            e.printStackTrace();
            while (!endOfList) {

                try {
                    Document urlDoc = Jsoup.connect(parentUrl).get(); //Connect to the parent in a while loop, and download the HTML source

                    Elements links = urlDoc.select(config.value(Country,"Country","linksList")); //Create an "Elements", which is a collection of type Element, specific to Jsoup,
                    // which contains all links containing /for-rent/
                    // href refers to any link, *= means "contains"

                    for (Element link : links) { //For every individual element in our "Elements"...
                        if (link.attr("abs:href").contains(config.value(Country,"Country","Links"))) { //If the absolute href contains /for-rent/...
                            urlList.add(link.attr("abs:href")); //Add the absolute href to our url list
                        }
                    }
                    if (!links.attr("abs:href").contains(config.value(Country,"Country","Links"))) { //if our "Elements" doesn't contain a link containing /for-rent/...
                        endOfList = true; //we have reached the end of the list, as there are no more properties.
                    } else {
                        index = index + 20; //If it still contains /for-rent/ links, we need to increment the parenturl's index
                        parentUrl = tempUrl + index; //append index to parent url.
                    }
                } catch (Exception ex) { //In the event that the link we try to connect to is bad...
                    index = index + 20; //Increment index
                    parentUrl = tempUrl + index; //Append new index, and try again
                }
            }
        }
        System.out.println(urlList);
        return urlList; //Return the list of urls
    }


    //Pulls info from a property. This method is accessed by all threads concurrently, so be careful modifying it.
    public static String Scrape(String BER_Query) {

        System.out.println("Thread Number " + Thread.currentThread().getName() + " Started"); //Simply prints out which thread number just started, for debug purposes

        Document document;

        int count = 0;

        String BER_Ratings[] = {"Exempt","G","F","E2","E1","D2","D1","C3","C2","C1","B3","B2","B1","A3","A2","A1"}; //Fixed array of BER ratings, to be read only

        String json = ""; //Empty string to contain the json object
        StringBuilder json_sb= new StringBuilder(json); //Convert string to StringBuilder so that we can edit it

        try {
            document = Jsoup.connect(urlMap.get(Thread.currentThread().getName())).get(); //Get the url with an index matching the thread number

            //Creating elements containing required info.
            Element title = document.selectFirst(config.value(Country,"Country","title")); //Select first h1 tag with a class containing "TitleBlock"

            Element price = document.selectFirst(config.value(Country, "Country", "price")); //Select first paragraph containing the euro symbol


            Element BER = document.selectFirst(config.value(Country,"Country","Energy_Rating")); //Select the first img tag with a class containing "BerDetails"

            Element type = document.selectFirst(config.value(Country,"Country","type")); //Select the first paragraph with a data-testid property that equals "property-type"

            Elements details = document.select(config.value(Country,"Country","details"));

            Element baths = document.selectFirst(config.value(Country,"Country","baths"));
            Element beds = document.selectFirst(config.value(Country,"Country","beds"));
            Element lease = document.selectFirst(config.value(Country,"Country","lease"));

            if (Country.equals("Ireland")) {
                for (Element li : details) {
                    if (li.select("li").text().contains("Bath")) baths = li.selectFirst("li:contains(Bath)");
                    if (li.select("li").text().contains("Minimum")) lease = li.selectFirst("li:contains(Minimum)");
                    if (li.select("li").text().contains("Bedroom")) beds = li.selectFirst("li:contains(Bedroom)");
                }
            }

            Element img = document.selectFirst(config.value(Country,"Country","img"));

            Element floorPlan = document.selectFirst(config.value(Country,"Country","floorPlan"));

            //If the type element is not null
            // and the index of the BER_Query passed in is less than or equal to the index of the BER rating of the property in question, or equals "All"...
            if ((Arrays.asList(BER_Ratings).indexOf(BER_Query)
                    <= Arrays.asList(BER_Ratings).indexOf(BER.attr("alt"))
                    || BER_Query.equalsIgnoreCase("All")) && type != null && title != null && price != null) { //&& price != null
                json_sb.append("{");
                //System.out.println(title.text() + "\n" + price.text());

                //Each if statement checks if its associated element is null and appends them to the json StringBuilder, following the json syntax
                if (title != null && price != null) {
                    json_sb.append("\"title\":\"" + title.text() + "\",\"price\":\"" + price.text() + "\",");
                }

                if (BER != null) {
                    if (BER.attr("alt").equalsIgnoreCase("SI_666")) {
                        json_sb.append("\"BER\":\"Exempt\",");
                    }
                    else if (BER.hasText()){
                        json_sb.append("\"BER\":\"" + BER.text() + "\",");
                    } else {
                        json_sb.append("\"BER\":\"" + BER.attr("alt") + "\",");
                        //System.out.println(BER.attr("alt"));
                    }
                }


                if (type != null) {
                    json_sb.append("\"type\":\"" + type.text() + "\",");
                    //System.out.println(type.text());
                }
                else {
                    json_sb.append("\"type\":\"\",");
                }

                if (lease != null) {
                    json_sb.append("\"lease\":\"" + lease.text() + "\",");
                    //System.out.println(lease.text());
                }
                else {
                    json_sb.append("\"lease\":\"\",");
                }

                if (beds != null) {
                    json_sb.append("\"beds\":\"" + beds.text() + "\",");
                    //System.out.println(beds.text());
                }
                else {
                    json_sb.append("\"beds\":\"\",");
                }


                if (baths != null) {
                    json_sb.append("\"baths\":\"" + baths.text() + "\",");
                }
                else {
                    json_sb.append("\"baths\":\"\",");
                }


                if (img != null) {
                    json_sb.append("\"img\":\"" + img.attr("src") + "\",");
                }
                else {
                    json_sb.append("\"img\":\"\",");
                }


                if (urlMap.get(Thread.currentThread().getName()) != null) {
                    json_sb.append("\"url\":\"" + urlMap.get(Thread.currentThread().getName()) + "\",");
                } //No need for else {} here, as if there is no url, we won't reach here anyway.


                if (floorPlan != null) {
                    json_sb.append("\"floorPlan\":\"" + floorPlan.attr("abs:href") + "\",");
                }
                else {
                    json_sb.append("\"floorPlan\":\"noPlan\",");
                }

                Elements facilities = document.select(config.value(Country,"Country","facilitiesList")); //Select the unordered list with class containing "PropertyDetails"
                for (Element li : facilities) { //For each element in the unordered list we just extracted...
                    json_sb.append("\"amenity" + count + "\":\"" + li.select(config.value(Country,"Country","facilities")).text() //append them to the json StringBuilder twice: once for key, once for value
                            + ",\",");

                    count++;
                    //System.out.println(li.select("li").text());
                }

                if (count != 0) {
                    json_sb.deleteCharAt((json_sb.length() - 3)); //Remove stray comma >:^(
                }

                while (count <= 10) { //If we didn't reach amenity 10, fill the remaining amenities with an empty string
                    json_sb.append("\"amenity" + count + "\":\"\",");
                    count++;
                }

                json_sb.deleteCharAt(json_sb.length()-1); //Delete the stray comma at the end  >:^(
                json_sb.append("},"); //Append the closing bracket and comma to the json StringBuilder
                //System.out.println("");
            }

            json = json_sb.toString(); //Make the json string to be returned equal to the json StringBuilder
            System.out.println("Thread Number " + Thread.currentThread().getName() + " Finished"); //Print out what thread number just finished for debug purposes
            return json; //Return the json string
        }

        catch (Exception e) {
            return null;
        }
    }

    @Override
    public Object call() throws Exception { //This method is what is going to be run by each thread, so make sure it is safe for concurrency. It is from the Callable interface.
        String threadJson = Scrape(BER_Query); //All this one does is call the scrape method, passing our BER_Query, which is thread safe, as it doesn't change
        return threadJson; //It then returns the resulting json string
    }
}
