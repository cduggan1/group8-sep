package test;

import main.Main;
import main.CsvData;
import main.SynonymMapBuilder;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class MainTest {
    private static Map<String, String> FILTERS = new HashMap<>();

    @BeforeEach
    void main() throws IOException {
        CsvData testData = new CsvData();
        testData.init();
        SynonymMapBuilder.init();
    }

    @org.junit.jupiter.api.Test
    void filterAccoms() throws IOException {
        // Test
        FILTERS = new HashMap<>();
        FILTERS.put("Brand", "Yugo");
        FILTERS.put("Site", "Highfield Park");
        CsvData testData = new CsvData();
        testData.init();
        List<Map<String,String>> accoms = testData.buildObject("src/main/info.csv");
        String filterMap = Main.filterAccoms(accoms, FILTERS).toString();
        assert(filterMap.contains("Highfield Park"));
        assert(!filterMap.contains("Dominick Place"));

        FILTERS.clear();
        FILTERS.put("Amenities", "disable gymnasium television room");
        filterMap = Main.filterAccoms(accoms, FILTERS).toString();
        assert(filterMap.contains("Dominick Place") && filterMap.contains("Beckett House"));
        //System.out.println(FILTERS.keySet().toString());
        assert(FILTERS.containsKey("Disability_Access"));
    }


    @org.junit.jupiter.api.Test
    void convertToJsonList() {

    }
}