    package main;

    import java.io.*;
    import java.lang.reflect.Array;
    import java.util.*;
    import java.util.concurrent.*;

    import edu.stanford.nlp.ling.CoreAnnotations;
    import edu.stanford.nlp.pipeline.StanfordCoreNLP;
    import edu.stanford.nlp.pipeline.Annotation;
    import edu.stanford.nlp.util.CoreMap;
    import edu.stanford.nlp.ling.CoreLabel;

    public class ParserML {
        public static String[] keywords = new String[]{"alarm","cable-television","dishwasher","garden-patio-balcony","central-heating","internet","microwave","parking","pets-allowed","smoking","dryer","washing-machine","wheelchair-access","serviced-property"};

        public String directory;
        public static Map<String, List<String>> synonyms = new HashMap<>();


        public ParserML(String directory){
            this.directory = directory;

            File file = new File("Amenities.synonym");
            try (Scanner scanner = new Scanner(file)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] parts = line.split("=");
                    String word = parts[0];
                    String[] syns = parts[1].split(",");
                    synonyms.put(word, Arrays.asList(syns));
                }
            }
            catch(Exception e){
                System.out.println("ML unable to read synonyms. Continuing...");
                e.printStackTrace();
            }

        }



        public String query(String input, boolean clean) {
            Logger.addLog("ML", "Received query for " + input);
            ArrayList<String> filters = new ArrayList<>();


            filters.addAll(parseNoML(input));

            System.out.println("FIlters after parseNoMl is : " + filters.toString());

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

                    String filterName = result.split(" ")[0].toLowerCase();

                    // Default:
                    if (!result.equalsIgnoreCase("Gym 0.12162162162162159")) {
                        //Custom Matching:
                        if(filterName.equalsIgnoreCase("Laundry") ||filterName.equalsIgnoreCase("Tumble Dryer") ) {
                            filters.add("\"" + "dryer" + "\"");
                            filters.add("\"" + "washing-machine" + "\"");
                            Logger.addLog("ML", "adding dryer and washing machine");
                        }else if(filterName.equalsIgnoreCase("disabled access")){
                            filters.add("\"" + "wheelchair-access" + "\"");
                            Logger.addLog("ML", "adding wheelchair");
                        }
                        else if(filterName.equalsIgnoreCase("Serviced Property")){
                            filters.add("\"" + "serviced-property" + "\"");
                            Logger.addLog("ML", "adding serviced");

                        }
                        else if(filterName.equalsIgnoreCase("TV room")){
                            filters.add("\"" + "cable-television" + "\"");
                            Logger.addLog("ML", "adding cable tv");

                        }
                        else if(filterName.equalsIgnoreCase("Balcony")||filterName.equalsIgnoreCase("Garden")){
                            filters.add("\"" + "garden-patio-balcony" + "\"");
                            Logger.addLog("ML", "adding garden-patio-balcony");
                        }

                        else {
                            if(!filters.contains(filterName)) {
                                filters.add("\"" + filterName + "\"");
                                Logger.addLog("ML", "adding " + filterName + " from ML guess");
                            }
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

            //Method for removing duplicates from an ArrayList<String>
            filters = new ArrayList<>(new HashSet<>(filters));
            return  filters.toString().replace("[", "").replace("]", "");
        }

        public ArrayList<String> cleanResult(ArrayList<String> word) {
            ArrayList<String> result = new ArrayList<>();
            for (String str : word) {
                for (String keyword : keywords) {
                    if (str.contains(keyword)) {
                        result.add(str);
                        Logger.addLog("ML", "adding " + str + " from cleaner as word:" + word + " contains:" + keyword);
                        break;
                    }
                }
            }
            return result;
        }



        public String parseString(String input_text) throws IOException {
            System.out.println("ML Called");
            String[] command = {"python3", directory + "/ML/predict.py", input_text, directory};

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

            System.out.println("mL RETURNING: " + output);

            return output;
        }

        public ArrayList<String> parseNoML(String input){
            ArrayList<String> filters = new ArrayList<>();
            for(String keyword : keywords){
                //For matching all keywords:
                if (input.toLowerCase().contains(keyword.toLowerCase()) && !filters.contains("\"" + keyword.toLowerCase() + "\"")){
                    filters.add("\"" + keyword.toLowerCase() + "\"");
                    Logger.addLog("ML", "Adding " + keyword + " from keyword matching");
                }
            }


            for (String word : input.split(" ")) {
                    word = word.replace(".", "");

                    //for every word we get:


                    for (Map.Entry<String, List<String>> entry : synonyms.entrySet()) {
                        Logger.addLog("ML", "ENTRY : " + entry.getValue() + "for value " + word);


                        if (entry.getValue().toString().toLowerCase().contains(word) && !filters.contains("\"" + entry.getKey().toLowerCase() + "\"")) {
                            Logger.addLog("ML", "MATCHED");
                            Logger.addLog("ML", "Adding " + entry.getKey().toLowerCase() + " as it contains " + word);
                            filters.add("\"" + entry.getKey().toLowerCase() + "\"");
                        }
                    }
            }

            return filters;
        }
    }
