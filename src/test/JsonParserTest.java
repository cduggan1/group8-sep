package test;

import main.JsonParser;
import main.Main;
import main.CsvData;
//import org.eclipse.jetty.util.ajax.JSON;
import main.UtilitiesFunction;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonParserTest {

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
        assertEquals(8, count);

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
    void findValuesOf() throws IOException {
        byte[] encodedBytes = Files.readAllBytes(Paths.get("src/test/testjson.txt"));
        String json = new String(encodedBytes, StandardCharsets.UTF_8);
        ArrayList<String> list = JsonParser.findValuesOf(json, "seoFriendlyPath");
        System.out.println(list.toString());
        assertEquals(3204, list.toString().length());
    }
}