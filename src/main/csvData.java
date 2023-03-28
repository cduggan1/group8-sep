package main;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class csvData {
    public List<Map<String,String>> accoms = null;

    public void init() throws IOException {
        try{
            accoms = buildObject("info.csv");
        }catch(Exception e)
        {
            System.out.println("Directory Failed. Trying again...");
            try{Thread.sleep(200);}catch(Exception f){f.printStackTrace();}
            accoms= buildObject("src/main/info.csv");
        }
    }

    //When called, will update the Object
    //in the current scope, with a provided filename.
    //It defaults to a hard-coded filename if not provided one.
    public boolean updateObject(){try {updateObject("info.csv");return true;}catch(Exception e){return false;}}//Ignore Errors.
    public boolean updateObject(String filename){
        try {
            accoms = buildObject(filename);
            return true;
        }catch(Exception e){return false;}//Ignore
    }

    //Object builder from CSV file
    public static List<Map<String, String>> buildObject(String filename) throws IOException {
        System.out.println("Building Object from " + filename);
        //Read file
        File input = new File(filename);
        //initialise a schema
        CsvSchema csv = CsvSchema.emptySchema().withHeader();
        //initialise a mapper
        CsvMapper csvMapper = new CsvMapper();
        //Iterate through CSV file and Map.
        MappingIterator<Map<String, String>> mappingIterator = csvMapper.reader().forType(Map.class).with(csv).readValues(input);
        System.out.println(Logger.GREEN + "Object built from " + filename + " successfully." + Logger.RESET);
        //Returns a map list.
        return mappingIterator.readAll();
    }


}
