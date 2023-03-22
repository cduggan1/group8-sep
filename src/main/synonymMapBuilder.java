package main;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class synonymMapBuilder {
    public static Map<String, ArrayList<String>> amenitiesSynonym = new HashMap<>();

    //private static String file = "Amenities.synonym";

    public static void init(){
        try {
            amenitiesSynonym = buildMap("Amenities.synonym");

        } catch(Exception e){
            Logger.addLog("Thesaurus Builder", "Exception in Thesaurus Initialization");
        }

    }


    public static Map<String, ArrayList<String>> buildMap (String file) {

        System.out.println("TESS");
        // create map
        Map<String, ArrayList<String>> buildAmenities = new HashMap<>();
        try {
            // initialize file reader

            FileReader fileReader = new FileReader(file);// Enter the entire path of the file if needed

            System.out.println("TESS");

            BufferedReader bufferedReader = new BufferedReader(fileReader);

            // create lines arrary
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
                    String[] initialSplit= entry.split("=",2);
                    String[] synonyms = initialSplit[1].split(",");
                    ArrayList<String> synonymOutput = new ArrayList<>();
                    /*
                    for (String syn: synonyms){
                        // add each synonym list
                        synonymOutput.add(syn);
                    }
                    */
                    synonymOutput.addAll(List.of(synonyms));
                    //synonymOutput = (ArrayList<String>) List.of(synonyms);
                    //Map<String, ArrayList<String>> synonymMap = new HashMap<>();
                    buildAmenities.put(initialSplit[0], synonymOutput);

                } catch (Exception e){
                    System.out.println(Logger.RED + "Error in nested Try-Catch in synonym mapBuilder: " + e +Logger.RESET);
                }

            }
            System.out.println(Logger.GREEN + "Thesaurus Object built from " + file + " successfully." + Logger.RESET);
            return buildAmenities;
        } catch (Exception e) {
            e.printStackTrace();
            Logger.addLog("Thesaurus Builder", "Exception in Thesaurus Initialization: " + e);
            return null;
        }
    }

}
