package test;

import main.SynonymMapBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


public class SynonymMapBuilderTest {

    @BeforeEach
    void init() throws IOException {
        SynonymMapBuilder.init();
    }


    @Test
    void voidTest() {
        assert(SynonymMapBuilder.amenitiesSynonym != null);
    }

    @Test
    void initializeTest()  throws IOException{
        Map<String,ArrayList<String>> test = SynonymMapBuilder.buildMap("src/main/Amenities.synonym");

        // TODO Find why this is throwing a warning
        try {
            if (test.keySet() != null){
                assertEquals(test.size(), 14);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(test.get("Rooftop_Garden").size(), 1);
        assertEquals(test.get("Ethernet").size(), 2);
    }
    @Test
    void ContainKeyTest(){
        assert(SynonymMapBuilder.amenitiesSynonym.containsKey("Disability_Access"));
        assert(SynonymMapBuilder.amenitiesSynonym.containsKey("Gym"));
        assert(SynonymMapBuilder.amenitiesSynonym.containsKey("TV_Room"));
        assert(SynonymMapBuilder.amenitiesSynonym.containsKey("Study_Space"));
        assert(SynonymMapBuilder.amenitiesSynonym.containsKey("Laundry_Room"));
        assert(SynonymMapBuilder.amenitiesSynonym.containsKey("Cinema_Room"));
        assert(SynonymMapBuilder.amenitiesSynonym.containsKey("Rooftop_Garden"));
        assert(SynonymMapBuilder.amenitiesSynonym.containsKey("Balcony"));
        assert(SynonymMapBuilder.amenitiesSynonym.containsKey("Dishwasher"));
        assert(SynonymMapBuilder.amenitiesSynonym.containsKey("Stovetop"));
        assert(SynonymMapBuilder.amenitiesSynonym.containsKey("Cafeteria"));
        assert(SynonymMapBuilder.amenitiesSynonym.containsKey("Sports_Hall"));
        assert(SynonymMapBuilder.amenitiesSynonym.containsKey("Fast_Wifi"));
        assert(SynonymMapBuilder.amenitiesSynonym.containsKey("Ethernet"));

    }

    @Test
    void ContainValueTest(){
        assert(SynonymMapBuilder.amenitiesSynonym.get("Disability_Access").contains("disable"));
        assert(SynonymMapBuilder.amenitiesSynonym.get("Gym").contains("gym"));
        assert(SynonymMapBuilder.amenitiesSynonym.get("TV_Room").contains("television"));
        assert(SynonymMapBuilder.amenitiesSynonym.get("Study_Space").contains("academic"));
        assert(SynonymMapBuilder.amenitiesSynonym.get("Laundry_Room").contains("laundrette"));
        assert(SynonymMapBuilder.amenitiesSynonym.get("Cinema_Room").contains("movie"));
        assert(SynonymMapBuilder.amenitiesSynonym.get("Rooftop_Garden").contains("rooftop garden"));
        assert(SynonymMapBuilder.amenitiesSynonym.get("Balcony").contains("balcony"));
        assert(SynonymMapBuilder.amenitiesSynonym.get("Dishwasher").contains("dishes"));
        assert(SynonymMapBuilder.amenitiesSynonym.get("Stovetop").contains("hob"));
        assert(SynonymMapBuilder.amenitiesSynonym.get("Cafeteria").contains("mess hall"));
        assert(SynonymMapBuilder.amenitiesSynonym.get("Sports_Hall").contains("sports"));
        assert(SynonymMapBuilder.amenitiesSynonym.get("Fast_Wifi").contains("wireless internet"));
        assert(SynonymMapBuilder.amenitiesSynonym.get("Ethernet").contains("wired internet"));


    }
}
