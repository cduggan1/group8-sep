package test;

import main.Main;
import main.csvData;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MainTest {
    private static Map<String, String> FILTERS = new HashMap<>();

    @BeforeEach
    void main() throws IOException {
        csvData.init();
    }

    @org.junit.jupiter.api.Test
    void filterAccoms() throws IOException {
        FILTERS = new HashMap<>();
        FILTERS.put("Brand", "Yugo");
        FILTERS.put("Site", "Highfield Park");
        FILTERS.put("Amenities", "disable");
        List<Map<?,?>> accoms = csvData.buildObject("src/main/info.csv");
        String filterMap = Main.filterAccoms(accoms, FILTERS).toString();
        assert(filterMap.contains("Highfield Park"));


        FILTERS.clear();
        FILTERS.put("Amenities", "disable");
        filterMap = Main.filterAccoms(accoms, FILTERS).toString();
        assert(filterMap.contains("Dominick Place") && filterMap.contains("Beckett House"));
        assert(FILTERS.containsKey("Disability_Access"));
    }


    @org.junit.jupiter.api.Test
    void convertToJsonList() {

    }
}