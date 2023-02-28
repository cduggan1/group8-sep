package main;

import java.util.ArrayList;

public class Logger {

    public static ArrayList<String> logFile = new ArrayList<>();

    public static void addLog(String tag, String msg) {
        logFile.add(tag + "," + msg + ".\n");
    }

    public static void clearLog(){
        logFile = new ArrayList<>();
    }
}
