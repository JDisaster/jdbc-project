package com.example.mysql;

import java.sql.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Database {
    
    public final static String URL = "jdbc:mysql://localhost:3306";
    public final static String USERNAME = "admin";
    public final static String PASSWORD = "admin";


    public static void init() {
        try {
            Connection initConn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            Statement initSQL = initConn.createStatement();

            String sql = Files.readString(Paths.get("schema.sql"));
            for(String s : sql.split(";")) {
                String trim = s.trim();
                if(trim.isEmpty() || trim.startsWith("--") || trim.startsWith("/*") || trim.endsWith("*/")) {
                    continue;
                }
                initSQL.execute(trim);
            }
            initConn.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
