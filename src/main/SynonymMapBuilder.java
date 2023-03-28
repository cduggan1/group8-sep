package main;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SynonymMapBuilder {
    public static Map<String, ArrayList<String>> amenitiesSynonym = new HashMap<>();

    //private static String file = "Amenities.synonym";

    public static void init() throws IOException{
        try {
            amenitiesSynonym = buildMap("Amenities.synonym");

        } catch(Exception e){
            System.out.println("Thesaurus Failed. Trying again...");
            amenitiesSynonym = buildMap("src/main/Amenities.synonym");
            Logger.addLog("Thesaurus Builder", "Exception in Thesaurus Initialization");
        }

    }


    public static Map<String, ArrayList<String>> buildMap (String file) throws IOException{

        System.out.println("Building Map from " + file);

        // create map
        Map<String, ArrayList<String>> buildAmenities = new HashMap<>();


        // initialize file reader

        FileReader fileReader = new FileReader(file);// Enter the entire path of the file if needed
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        // create lines array
        ArrayList<String> lineAL = new ArrayList<String>();
        String i;

        while((i=bufferedReader.readLine()) != null) {
            lineAL.add(i);
        }
        // close readers
        bufferedReader.close();
        fileReader.close();


        // process lines
        for (String entry : lineAL){
            try{
                // delimit strings by = and , respectively
                String[] initialSplit= entry.split("=",2); // maximize the amount of strings this can break down into to a maximum of 2
                String[] synonyms = initialSplit[1].split(","); // no maximum
                ArrayList<String> synonymOutput = new ArrayList<>(List.of(synonyms));

                // TODO Dirty implementation of this, find a cleaner syntax
                while (synonymOutput.remove("")){} // remove all "" entries, prevents total matches and possible null-issues

                buildAmenities.put(initialSplit[0], synonymOutput);

            } catch (Exception e){
                System.out.println(Logger.RED + "Error in nested Try-Catch in synonym mapBuilder: " + e +Logger.RESET);
            }

        }
        System.out.println(Logger.GREEN + "Thesaurus Object built from " + file + " successfully." + Logger.RESET);
        return buildAmenities;

    }

}
