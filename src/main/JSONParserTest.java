package main;

import org.eclipse.jetty.util.ajax.JSON;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JSONParserTest {

    @Test
    void parseJSON() throws IOException {
        //Initialize CSVData Class
        csvData.init();

        //Prevent main from adding count to the response as this will defeat the purpose of testing.
        Main.addCount = false;

        //Build fiters for property count = 8
        Map<String, String> testFilters = new HashMap<>(){{
            put("Brand", "Yugo");
        }};

        //Build List of properties with filter applied
        List<Map<?,?>> testAccomsList = Main.filterAccoms(csvData.accoms,testFilters);

        //New Parser
        JSONParser parser = new JSONParser();

        //Convert to JSON our Map of Properties
        String response = Main.convertToJsonList(testAccomsList);

        //Build a summary map from our parser
        Map<String, Integer> result = JSONParser.parseJSON(response, "Residences");
        //Get the count of objects
        int count = result.get("TOTAL_OBJECTS");

        //Should be 8
        assertEquals(8, count);

        //Apply another filter and rebuild list
        testFilters.put("Site", "Dominick Place");
        testAccomsList = Main.filterAccoms(csvData.accoms, testFilters);
        response = Main.convertToJsonList(testAccomsList);

        result = JSONParser.parseJSON(response, "Residences");
        count = result.get("TOTAL_OBJECTS");

        assertEquals(1, count);
    }

    @Test
    void countProperties() throws IOException {

        //Initialize CSVData Class
        csvData.init();

        //Build fiters for property count = 8
        Map<String, String> testFilters = new HashMap<>(){{
            put("Brand", "Yugo");
        }};

        //Build List of properties with filter applied
        List<Map<?,?>> testAccomsList = Main.filterAccoms(csvData.accoms,testFilters);

        //New Parser
        JSONParser parser = new JSONParser();

        //Convert to JSON our Map of Properties
        String response = Main.convertToJsonList(testAccomsList);

        assertEquals(8, parser.countProperties(response));
    }
}