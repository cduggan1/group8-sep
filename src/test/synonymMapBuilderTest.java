package test;

import main.synonymMapBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


public class synonymMapBuilderTest {

    @BeforeEach
    void init() throws IOException {
        synonymMapBuilder.init();
    }


    @Test
    void voidTest(){
        assert(synonymMapBuilder.amenitiesSynonym != null);
    }

    @Test
    void ContainKeyTest(){
        assert(synonymMapBuilder.amenitiesSynonym.containsKey("Disability_Access"));
        assert(synonymMapBuilder.amenitiesSynonym.containsKey("Gym"));
        assert(synonymMapBuilder.amenitiesSynonym.containsKey("TV_Room"));
        assert(synonymMapBuilder.amenitiesSynonym.containsKey("Study_Space"));
        assert(synonymMapBuilder.amenitiesSynonym.containsKey("Laundry_Room"));
        assert(synonymMapBuilder.amenitiesSynonym.containsKey("Cinema_Room"));
        assert(synonymMapBuilder.amenitiesSynonym.containsKey("Rooftop_Garden"));
        assert(synonymMapBuilder.amenitiesSynonym.containsKey("Balcony"));
        assert(synonymMapBuilder.amenitiesSynonym.containsKey("Dishwasher"));
        assert(synonymMapBuilder.amenitiesSynonym.containsKey("Stovetop"));
        assert(synonymMapBuilder.amenitiesSynonym.containsKey("Cafeteria"));
        assert(synonymMapBuilder.amenitiesSynonym.containsKey("Sports_Hall"));
        assert(synonymMapBuilder.amenitiesSynonym.containsKey("Fast_Wifi"));
        assert(synonymMapBuilder.amenitiesSynonym.containsKey("Ethernet"));

    }

    @Test
    void ContainValueTest(){
        assert(synonymMapBuilder.amenitiesSynonym.get("Disability_Access").contains("disable"));
        assert(synonymMapBuilder.amenitiesSynonym.get("Gym").contains("gym"));
        assert(synonymMapBuilder.amenitiesSynonym.get("TV_Room").contains("television"));
        assert(synonymMapBuilder.amenitiesSynonym.get("Study_Space").contains("academic"));
        //System.out.println(synonymMapBuilder.amenitiesSynonym.get("Laundry_Room").toString());
        assert(synonymMapBuilder.amenitiesSynonym.get("Laundry_Room").contains("laundrette"));
        assert(synonymMapBuilder.amenitiesSynonym.get("Cinema_Room").contains("movie"));
        assert(synonymMapBuilder.amenitiesSynonym.get("Rooftop_Garden").contains("rooftop garden"));
        assert(synonymMapBuilder.amenitiesSynonym.get("Balcony").contains("balcony"));
        assert(synonymMapBuilder.amenitiesSynonym.get("Dishwasher").contains("dishes"));
        assert(synonymMapBuilder.amenitiesSynonym.get("Stovetop").contains("hob"));
        assert(synonymMapBuilder.amenitiesSynonym.get("Cafeteria").contains("mess hall"));
        assert(synonymMapBuilder.amenitiesSynonym.get("Sports_Hall").contains("sports"));
        assert(synonymMapBuilder.amenitiesSynonym.get("Fast_Wifi").contains("wireless internet"));
        assert(synonymMapBuilder.amenitiesSynonym.get("Ethernet").contains("wired internet"));
        System.out.println(synonymMapBuilder.amenitiesSynonym.get("Ethernet").toString());

    }
}
