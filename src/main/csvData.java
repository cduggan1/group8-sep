package main;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.File;
import java.util.List;
import java.util.Map;

public class csvData {
    public static List<Map<?,?>> accoms = null;

    public static void init(String filename) {
        accoms = buildObject(filename);
    }

    //When called, will update the Object
    //in the current scope, with a provided filename.
    //It defaults to a hard-coded filename if not provided one.
    public static boolean updateObject(){try {updateObject("src/main/info.csv");return true;}catch(Exception e){return false;}}//Ignore Errors.
    public static boolean updateObject(String filename){
        try {
            accoms = buildObject(filename);
            return true;
        }catch(Exception e){return false;}//Ignore
    }

    //Object builder from CSV file
    public static List<Map<?, ?>> buildObject(String filename) {
        System.out.println("Building Object from " + filename);
        //Read file
        File input = new File(filename);
        try {
            //initialise a schema
            CsvSchema csv = CsvSchema.emptySchema().withHeader();
            //initialise a mapper
            CsvMapper csvMapper = new CsvMapper();
            //Iterate through CSV file and Map.
            MappingIterator<Map<?, ?>> mappingIterator = csvMapper.reader().forType(Map.class).with(csv).readValues(input);
            System.out.println("Object Built from " + filename + " successfully.");
            //Returns a map list.
            return mappingIterator.readAll();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
