package main;

import java.io.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Logger {

    public static ArrayList<String> logFile = new ArrayList<>();
    public static String epoch = String.valueOf(System.currentTimeMillis());
    public static boolean sendToConsole = false;

    // Regular Colors
    public static final String BLACK = "\033[0;30m";   // BLACK
    public static final String RED = "\033[0;31m";     // RED
    public static final String GREEN = "\033[0;32m";   // GREEN
    public static final String YELLOW = "\033[0;33m";  // YELLOW
    public static final String BLUE = "\033[0;34m";    // BLUE
    public static final String PURPLE = "\033[0;35m";  // PURPLE
    public static final String CYAN = "\033[0;36m";    // CYAN
    public static final String WHITE = "\033[0;37m";   // WHITE
    // Reset
    public static final String RESET = "\033[0m";  // Text Reset


    public static void addLog(String tag, String msg) {
        File file = new File("src/logs/","new_log_"+epoch+".txt");
        LocalTime time = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedTime = time.format(formatter);
        try(FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println("["+time.format(formatter)+"]" + tag +":"+msg+"\n");
        } catch (IOException e) {
            //exception handling left as an exercise for the reader
        }
        logFile.add("\n["+time.format(formatter)+"]" +tag + "," + msg + ".\n");
        if(sendToConsole)
            System.out.println(("\n["+time.format(formatter)+"]" +tag + "," + msg + ".\n"));


    }

    public static void clearLog(){
        logFile = new ArrayList<>();
    }
}
