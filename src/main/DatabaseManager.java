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
}
