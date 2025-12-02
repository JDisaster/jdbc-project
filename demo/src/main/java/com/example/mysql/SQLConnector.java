package com.example.mysql;

import java.sql.Connection;
import java.sql.DriverManager;

public class SQLConnector {
    private static final String URL = "jdbc:mysql://localhost:3306/studentdb";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";

    //tester main method to ensure local MySQL credentials are created properly
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Pass");
            conn.close();
        } catch (Exception e) {
            System.out.println("Fail");
        }
    }
}
