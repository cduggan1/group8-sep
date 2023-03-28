package test;

import main.JSONParser;
import main.Main;
import main.csvData;
import org.junit.jupiter.api.Test;

import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class parserTest {

    @Test
    void parseJSON() throws IOException {
        //Initialize CSVData Class
        csvData testData = new csvData();
        testData.init();

        //Prevent main from adding count to the response as this will defeat the purpose of testing.
        Main.addCount = false;

        //Build fiters for property count = 8
        Map<String, String> testFilters = new HashMap<>(){{
            put("Brand", "Yugo");
        }};

        //Build List of properties with filter applied
        List<Map<String,String>> testAccomsList = Main.filterAccoms(testData.accoms,testFilters);

        //New Parser
        JSONParser parser = new JSONParser();

        //Convert to JSON our Map of Properties
        String response = Main.convertToJsonList(testAccomsList);

        //Build a summary map from our parser
        Map<String, Integer> result = JSONParser.parseJSON(response, "Residences");
        //Get the count of objects
        int count = result.get("TOTAL_OBJECTS");

        //Should be 8
        assertEquals(7, count);

        //Apply another filter and rebuild list
        testFilters.put("Site", "Dominick Place");
        testAccomsList = Main.filterAccoms(testData.accoms, testFilters);
        response = Main.convertToJsonList(testAccomsList);

        result = JSONParser.parseJSON(response, "Residences");
        count = result.get("TOTAL_OBJECTS");

        assertEquals(1, count);
    }

    @Test
    void countProperties() throws IOException {

        //Initialize CSVData Class
        csvData testData = new csvData();
        testData.init();

        //Build fiters for property count = 8
        Map<String, String> testFilters = new HashMap<>(){{
            put("Brand", "Yugo");
        }};

        //Build List of properties with filter applied
        List<Map<String,String>> testAccomsList = Main.filterAccoms(testData.accoms,testFilters);

        //New Parser
        JSONParser parser = new JSONParser();

        //Convert to JSON our Map of Properties
        String response = Main.convertToJsonList(testAccomsList);

        assertEquals(8, parser.countProperties(response));
    }

    @Test
    void extractNumberTest(){
        Random rand = new Random();

        int randInt = rand.nextInt(50);

        String s = "D" + randInt;
        String s1 = "Dub" + randInt;
        String s2 = "abcdef" + randInt;

        assertEquals(randInt, Main.extractNumber(s));
        assertEquals(randInt, Main.extractNumber(s1));
        assertEquals(randInt, Main.extractNumber(s2));
    }

    @Test
    void parseTimeTest(){

        String s = "10 hours and 51 minutes";
        Map<String, Integer> sMap = Main.parseTime(s,false);

        assertEquals(10, sMap.get("h"));
        assertEquals(51, sMap.get("m"));

        sMap = Main.parseTime(s,true);

        assertEquals(651, sMap.get("m"));



    }

}