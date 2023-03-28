package test;

import main.CacheObject;
//import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class CacheObjectTest {
    Random rand = new Random();

    @Test
    void putGet() {
        CacheObject.expiryTimeMillis = 2000;
        CacheObject.put("TestSeq", "12345");
        assertEquals("12345", CacheObject.get("TestSeq"));
    }

    @Test
    void expiry(){
        long milliToWait = rand.nextInt(100)+1;
        CacheObject.expiryTimeMillis = milliToWait;
        CacheObject.put("TestSeq", "12345");
        assertEquals("12345", CacheObject.get("TestSeq"));

        try{Thread.sleep(milliToWait);}catch(Exception e){fail();}
        assertNull(CacheObject.get("TestSeq"));
    }
}