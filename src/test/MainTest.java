package test;

import main.Main;
import main.csvData;
import org.junit.jupiter.api.BeforeEach;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MainTest {
    private static Map<String, String> FILTERS = new HashMap<>();

    @BeforeEach
    void main(){
        csvData.init("src/main/test-data.csv");
    }

    @org.junit.jupiter.api.Test
    void filterAccoms() {
        FILTERS = new HashMap<>();
        FILTERS.put("Brand", "Yugo");
        FILTERS.put("Site", "Highfield Park");
        List<Map<?,?>> accoms = csvData.buildObject("src/main/info.csv");
        String filterMap = Main.filterAccoms(accoms, FILTERS).toString();
        assert(filterMap.contains("Highfield Park"));
    }


    @org.junit.jupiter.api.Test
    void convertToJsonList() {

    }
}