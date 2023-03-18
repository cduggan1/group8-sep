package main;

import java.sql.*;

public class DatabaseManager {
    private Connection conn;

    public DatabaseManager(String url, String username, String password) {
        try {
            conn = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return conn;
    }

    public static boolean testConnection() {
        String url = "jdbc:mysql://localhost:3306/accomsdata";
        String user = "group8sep";
        String password = "group8-sweng!";

        try{
            Connection connection = DriverManager.getConnection(url, user, password);
            System.out.println("Database connection successful!");
            return true;
        } catch (Exception e) {
            System.err.println("Database connection failed:");
            return false;
        }
    }

}
