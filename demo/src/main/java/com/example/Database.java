package com.example;

import java.sql.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.mindrot.jbcrypt.BCrypt;

public class Database {
    
    public final static String INITURL = "jdbc:mysql://localhost:3306";
    public final static String URL = "jdbc:mysql://localhost:3306/studentdb";
    public final static String USERNAME = "admin";
    public final static String PASSWORD = "admin";

    // returns a connection to the database given the final account details and uses the default port for MySQL
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // initializes the database with sql statements in schema.sql
    public static void init() {
        try {
            Connection initConn = DriverManager.getConnection(INITURL, USERNAME, PASSWORD);
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

    // creates a user given student name, email, password, and major
    public static void createUser(String studentName, String email, String password, String major) {
        try {
            Connection conn = getConnection();
            PreparedStatement SQL = conn.prepareStatement(
                "INSERT INTO students (studentName, email, passwordHash, major) VALUES (?, ?, ?, ?)"
            );
            SQL.setString(1, studentName);
            SQL.setString(2, email);

            String hash = BCrypt.hashpw(password, BCrypt.gensalt());
            SQL.setString(3, hash);

            SQL.setString(4, major);
            SQL.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // login given a username and password, returns true if successful, false if not
    public static boolean login(String email, String password) {
        try {
            Connection conn = getConnection();
            PreparedStatement SQL = conn.prepareStatement(
                "SELECT passwordHash FROM students WHERE email = ?"
            );
            email = email.trim().toLowerCase();
            SQL.setString(1, email);
            ResultSet rs = SQL.executeQuery();
            if(!rs.next()) {
                return false;
            }
            String passwordHash = rs.getString("passwordHash");
            rs.close();
            SQL.close();
            conn.close();
            return BCrypt.checkpw(password, passwordHash);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getStudentId(String email) {
        try {
            Connection conn = getConnection();
            PreparedStatement SQL = conn.prepareStatement(
                "SELECT studentID FROM students WHERE email = ?"
            );
            SQL.setString(1, email);
            return Integer.toString(SQL.executeQuery().getInt("studentID"));
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
