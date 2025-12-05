package com.example.dao;

import com.example.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public void createUser(User user) throws SQLException {
        String sql = "INSERT INTO users (student_id, name, password, major, semester) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getStudentId());
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getMajor());
            pstmt.setString(5, user.getSemester());
            pstmt.executeUpdate();
        }
    }

    public User getUserByStudentId(String studentId) throws SQLException {
        String sql = "SELECT * FROM users WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getString("student_id"),
                        rs.getString("name"),
                        rs.getString("password"),
                        rs.getString("major"),
                        rs.getString("semester"));
            }
        }
        return null;
    }

    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(new User(
                        rs.getString("student_id"),
                        rs.getString("name"),
                        rs.getString("password"),
                        rs.getString("major"),
                        rs.getString("semester")));
            }
        }
        return users;
    }
}