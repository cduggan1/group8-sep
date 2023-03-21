package main;

import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.*;

public class JSONParser {

    public static Map<String, Integer> parseJSON(String jsonStr, String object) throws JSONException {
        try {
            Map<String, Integer> summaryMap = new HashMap<String, Integer>();

            JSONObject jsonObj = new JSONObject(jsonStr);
            JSONArray jsonArray = jsonObj.getJSONArray(object);

            int totalObjects = jsonArray.length();
            summaryMap.put("TOTAL_OBJECTS", totalObjects);

            return summaryMap;
        }catch(Exception e){
            System.out.println("Error with Parsing JSON.. " + jsonStr);
            System.out.println("Returning null...");
            return null;
        }
    }

    public static int countProperties(String jsonString) {
        return parseJSON(jsonString, "Residences").get("TOTAL_OBJECTS");
    }

    public static ArrayList<String> findValuesOf(String json, String key) throws JsonProcessingException {
        try{
            JsonNode jsonNode = new ObjectMapper().readTree(json);
            return new ArrayList<>(jsonNode.findValuesAsText(key));
        }
        catch(JsonProcessingException e1){
            System.err.println("Unable to parse JSON String");
            return null;
        }
    }



}
