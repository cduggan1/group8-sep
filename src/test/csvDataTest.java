package test;

import main.csvData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class csvDataTest {

    @BeforeEach
    void init() throws IOException {
        csvData.init();
    }

    @Test
    void buildObject() throws IOException {
        List<Map<?, ?>> buildObject = csvData.buildObject("src/main/test-data.csv");
        assert buildObject != null;
        assertEquals(3, buildObject.size());
        assertEquals(1, buildObject.get(0).size());
        assertEquals(1, buildObject.get(1).size());
        assertEquals(1, buildObject.get(2).size());
    }
}