package main;

import java.util.HashMap;
import java.util.Map;

public class CacheObject {


    //private static long expiryTimeMillis = 3600000; // 1 hour

    public static long expiryTimeMillis = 5000; // 5 seconds


    private static Map<String, ExpiryMap<String>> map = new HashMap<>();


    public static void put(String request, String response) {
        map.put(request, new ExpiryMap<>(response));
    }

    public static String get(String request) {
        ExpiryMap<String> entry = map.get(request);
        if (entry == null || entry.expired()) {
            map.remove(request);
            return null;
        }
        return entry.getVal();
    }

    private static class ExpiryMap<String> {

        private final String value;
        private final long expiryTime;

        ExpiryMap(String response) {
            this.value = response;
            this.expiryTime = System.currentTimeMillis() + expiryTimeMillis;
        }

        boolean expired() {
            return System.currentTimeMillis() > expiryTime;
        }

        String getVal() {
            return value;
        }
    }


}
