import java.io.File;
import java.util.List;
import java.util.Map;

import static spark.Spark.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.csv.*;
//
public class Main {
    public static void main(String[] args) {

        List<Map<?, ?>> accoms = buildObject("src/info.csv");
        if(accoms==null){
            System.out.printf("Error Parsing CSV");
            System.exit(1);
        }
        System.out.println(accoms.get(1).toString());
            get("/id/:index", (req,res)->{
                int index = Integer.parseInt(req.params(":index"));
                System.out.print("Index is: " + index);
                String returnedval = accoms.get(index).toString();
                return returnedval;
            });

//        get("/hello", (req, res)->"Hello, world");
//

    }

    public static List<Map<?, ?>> buildObject(String filename) {
        System.out.println("Building Object from " + filename);
        File input = new File(filename);
        try {
            CsvSchema csv = CsvSchema.emptySchema().withHeader();
            CsvMapper csvMapper = new CsvMapper();
            MappingIterator<Map<?, ?>> mappingIterator = csvMapper.reader().forType(Map.class).with(csv).readValues(input);
            List<Map<?, ?>> list = mappingIterator.readAll();
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}