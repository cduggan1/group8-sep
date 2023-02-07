import java.io.File;
import java.util.ArrayList;
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

            get("/id/:index", (req,res)->{
                int index = Integer.parseInt(req.params(":index"));
                System.out.print("Index requested: " + index);
                return accoms.get(index).toString();
            });



            get("/all", (req,res)->{
                System.out.print("Requested All");
                return accoms.toString();
            });

            get("/name/:name", (req,res)->{
                return getSiteInfo(accoms, req.params(":name"));
            });

            get("/brand/:brand", (req,res)->{
                return getCompanyInfo(accoms, req.params(":brand"));
            });

            get("/id/:index/:hasEnsuite", (req,res)->{
                int index = Integer.parseInt(req.params(":index"));
                System.out.print("Studio Check for index: " + index);
                if (hasStudios(accoms, index)){
                    return "Yes";
                }else {
                    return "No";
                }
            });

            get("/id/:index/:hasEnsuite", (req,res)->{
                int index = Integer.parseInt(req.params(":index"));
                System.out.print("Ensuite Check for index: " + index);
                if (hasEnsuites(accoms, index)){
                    return "Yes";
                }else {
                    return "No";
                }
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
            return mappingIterator.readAll();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean hasEnsuites(List<Map<?, ?>> list, int id){
        return(list.get(id).toString().contains("Has Ensuite=y"));
    }
    public static boolean hasStudios(List<Map<?, ?>> list, int id){
        return(list.get(id).toString().contains("Has Studio=y"));
    }
    public static String getSiteInfo(List<Map<?, ?>> list, String site){
        for(int i=0;i<list.size();i++){
            if(list.get(i).toString().contains("Site="+site)){
                return list.get(i).toString();
            }
        }
        return "None";
    }

    public static String getCompanyInfo(List<Map<?, ?>> list, String company){
        ArrayList<String> listOfCompany = new ArrayList<String>();

        for(int i=0;i<list.size();i++) {
            if (list.get(i).toString().contains("Brand=" + company)) {
                listOfCompany.add(list.get(i).toString());
            }
        }
        if(listOfCompany.size()==0)
            return "None";

        return listOfCompany.toString();
    }

}