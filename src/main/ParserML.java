package main;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.ling.CoreLabel;

public class ParserML {

    public static String[] keywords = new String[]{"alarm","cable-television","dishwasher","garden-patio-balcony","central-heating","internet","microwave","parking","pets-allowed","smoking","dryer","washing-machine","wheelchair-access","serviced-property"};

    public String directory;

    public ParserML(String directory){
        this.directory = directory;
    }
    public String query(String input, boolean clean) {
        Logger.addLog("ML", "Received query for " + input);
        ArrayList<String> filters = new ArrayList<>();


        filters.addAll(parseNoML(input));

        String[] sentences = input.toLowerCase().split("\\.");

        ExecutorService executor = Executors.newFixedThreadPool(sentences.length);

        List<Future<String>> futures = new ArrayList<>();

        // Submit a new thread for each sentence
        for (String sentence : sentences) {
            Future<String> future = executor.submit(() -> parseString(sentence));
            futures.add(future);
        }

        for (Future<String> future : futures) {
            try {
                String result = future.get();
                Logger.addLog("ML", "RESULT = " + result);
                System.out.println("RESULT = " + result.split(" ")[0]);

                // Default:
                if (!result.equalsIgnoreCase("Gym 0.12162162162162159")) {
                    //Custom Matching:
                    if(result.split(" ")[0].equalsIgnoreCase("Laundry") ||result.split(" ")[0].equalsIgnoreCase("Tumble Dryer") ) {
                        filters.add("\"" + "dryer" + "\"");
                        filters.add("\"" + "washing-machine" + "\"");
                    }else if(result.split(" ")[0].equalsIgnoreCase("disabled access")){
                        filters.add("\"" + "wheelchair-access" + "\"");
                    }
                    else if(result.split(" ")[0].equalsIgnoreCase("Serviced Property")){
                        filters.add("\"" + "serviced-property" + "\"");
                    }
                    else if(result.split(" ")[0].equalsIgnoreCase("TV room")){
                        filters.add("\"" + "cable-television" + "\"");
                    }
                    else if(result.split(" ")[0].equalsIgnoreCase("Balcony")||result.split(" ")[0].equalsIgnoreCase("Garden")){
                        filters.add("\"" + "garden-patio-balcony" + "\"");
                    }

                    else {
                        filters.add("\"" + result.split(" ")[0] + "\"");
                    }
                }
            } catch (Exception e) {
                // Ignore
                Logger.addLog("ML", "Parsing Error. Skipping sentence.");
            }
        }

        // Shut down the thread pool
        executor.shutdown();
        if(clean)
            filters = cleanResult(filters);
        return  filters.toString().replace("[", "").replace("]", "");
    }

    public ArrayList<String> cleanResult(ArrayList<String> word){

        System.out.println("Cleaning " + word.toString());

        ArrayList<String> result = new ArrayList<>();
        for(String keyword : keywords){
            System.out.println("Checking " + keyword);
            if(word.contains("\""+keyword+"\"")) {
                result.add("\""+keyword+"\"");
            }
            else{
                System.out.println("Does not contain.");
            }
        }
        return result;
    }

    public String parseString(String input_text) throws IOException {
        String[] command = {"python3", directory + "/predict.py", input_text, directory};

        Process process = new ProcessBuilder(command).start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String output = reader.readLine();

        try {
            int exitStatus = process.waitFor();
            if (exitStatus != 0) {
                throw new RuntimeException("Python script failed with exit status " + exitStatus);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Python script was interrupted", e);
        }

        return output;
    }

    public ArrayList<String> parseNoML(String input){
        ArrayList<String> filters = new ArrayList<>();
        for(String keyword : keywords){
            if (input.toLowerCase().contains(keyword.toLowerCase()) && !filters.contains("\"" + keyword + "\"")){
                filters.add("\"" + keyword + "\"");
                Logger.addLog("ML", "Adding " + keyword);
                System.out.println("Adding " + keyword);
            }
            if(input.toLowerCase().contains("tv") && !filters.contains("\"" + "cable-television" + "\"")){
                filters.add("\"" + "cable-television" + "\"");
            }
            if((input.toLowerCase().contains("heating") || input.toLowerCase().contains("heat") )&& !filters.contains("\"" + "central-heating" + "\"")){
                filters.add("\"" + "central-heating" + "\"");
            }
        }
        return filters;
    }
}
