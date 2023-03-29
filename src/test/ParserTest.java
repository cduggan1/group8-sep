package test;

import main.JsonParser;
import main.Main;
import main.CsvData;
import main.UtilitiesFunction;
import org.junit.jupiter.api.Test;

import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    @Test
    void parseJSON() throws IOException {
        //Initialize CSVData Class
        CsvData testData = new CsvData();
        testData.init();

        //Prevent main from adding count to the response as this will defeat the purpose of testing.
        Main.addCount = false;

        //Build fiters for property count = 8
        Map<String, String> testFilters = new HashMap<>(){{
            put("Brand", "Yugo");
        }};

        //Build List of properties with filter applied
        List<Map<String,String>> testAccomsList = UtilitiesFunction.filterAccoms(testData.accoms,testFilters);

        //New Parser
        JsonParser parser = new JsonParser();

        //Convert to JSON our Map of Properties
        String response = UtilitiesFunction.convertToJsonList(testAccomsList);

        //Build a summary map from our parser
        Map<String, Integer> result = JsonParser.parseJSON(response, "Residences");
        //Get the count of objects
        int count = result.get("TOTAL_OBJECTS");

        //Should be 8
        assertEquals(7, count);

        //Apply another filter and rebuild list
        testFilters.put("Site", "Dominick Place");
        testAccomsList = UtilitiesFunction.filterAccoms(testData.accoms, testFilters);
        response = UtilitiesFunction.convertToJsonList(testAccomsList);

        result = JsonParser.parseJSON(response, "Residences");
        count = result.get("TOTAL_OBJECTS");

        assertEquals(1, count);
    }

    @Test
    void countProperties() throws IOException {

        //Initialize CSVData Class
        CsvData testData = new CsvData();
        testData.init();

        //Build fiters for property count = 8
        Map<String, String> testFilters = new HashMap<>(){{
            put("Brand", "Yugo");
        }};

        //Build List of properties with filter applied
        List<Map<String,String>> testAccomsList = UtilitiesFunction.filterAccoms(testData.accoms,testFilters);

        //New Parser
        JsonParser parser = new JsonParser();

        //Convert to JSON our Map of Properties
        String response = UtilitiesFunction.convertToJsonList(testAccomsList);

        assertEquals(8, parser.countProperties(response));
    }

    @Test
    void extractNumberTest(){
        Random rand = new Random();

        int randInt = rand.nextInt(50);

        String s = "D" + randInt;
        String s1 = "Dub" + randInt;
        String s2 = "abcdef" + randInt;

        assertEquals(randInt, UtilitiesFunction.extractNumber(s));
        assertEquals(randInt, UtilitiesFunction.extractNumber(s1));
        assertEquals(randInt, UtilitiesFunction.extractNumber(s2));
    }

    @Test
    void parseTimeTest(){

        String s = "10 hours and 51 minutes";
        Map<String, Integer> sMap = UtilitiesFunction.parseTime(s,false);

        assertEquals(10, sMap.get("h"));
        assertEquals(51, sMap.get("m"));

        sMap = UtilitiesFunction.parseTime(s,true);

        assertEquals(651, sMap.get("m"));



    }

}