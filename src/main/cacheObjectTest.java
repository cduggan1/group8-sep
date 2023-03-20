package main;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class cacheObjectTest {
    Random rand = new Random();

    @Test
    void putGet() {
        cacheObject.expiryTimeMillis = 2000;
        cacheObject.put("TestSeq", "12345");
        assertEquals("12345", cacheObject.get("TestSeq"));
    }

    @Test
    void expiry(){
        long milliToWait = rand.nextInt(100)+1;
        cacheObject.expiryTimeMillis = milliToWait;
        cacheObject.put("TestSeq", "12345");
        assertEquals("12345", cacheObject.get("TestSeq"));

        try{Thread.sleep(milliToWait);}catch(Exception e){assertTrue(false);}
        assertNull(cacheObject.get("TestSeq"));
    }
}