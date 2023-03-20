package main;

import java.util.HashMap;

public class cacheObject {
    private static HashMap<String, String> cache = new HashMap<String, String>();

    public static boolean putRequest(String request, String response){
        try {
            cache.put(request, response);
            return true;
        }catch(Exception e) {
            return false;
        }
    }

    public static String getRequest(String request){
        return cache.getOrDefault(request, null);
    }



}
