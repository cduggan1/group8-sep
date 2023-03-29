package test;

import main.CsvData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CsvDataTest {

    @BeforeEach
    void init() throws IOException {
        CsvData testData = new CsvData();
        testData.init();
    }

    @Test
    void buildObject() throws IOException {
        CsvData testData = new CsvData();
        testData.init();
        List<Map<String, String>> buildObject = testData.buildObject("src/test/test-data.csv");
        assert buildObject != null;
        assertEquals(3, buildObject.size());
        assertEquals(1, buildObject.get(0).size());
        assertEquals(1, buildObject.get(1).size());
        assertEquals(1, buildObject.get(2).size());
    }
}