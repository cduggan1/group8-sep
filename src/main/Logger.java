package main;

import java.io.*;
import java.util.ArrayList;

public class Logger {

    public static ArrayList<String> logFile = new ArrayList<>();
    public static String epoch = String.valueOf(System.currentTimeMillis());

    public static void addLog(String tag, String msg) {
        File file = new File("src/logs/","new_log_"+epoch+".txt");

        try(FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println(tag+":"+msg);
        } catch (IOException e) {
            //exception handling left as an exercise for the reader
        }
        logFile.add(tag + "," + msg + ".\n");
    }

    public static void clearLog(){
        logFile = new ArrayList<>();
    }
}
