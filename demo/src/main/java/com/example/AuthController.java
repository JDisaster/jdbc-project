package com.example;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.example.dao.DatabaseConnection;

public class AuthController {

    // ----- LOGIN -----
    @FXML private TextField loginStudentID;
    @FXML private PasswordField loginPassword;
    @FXML private Label loginMessage;

    // ----- SIGNUP -----
    @FXML private TextField signupName;
    @FXML private TextField signupStudentID;
    @FXML private PasswordField signupPassword;
    @FXML private ComboBox<String> signupMajor;
    @FXML private ComboBox<String> signupSemester;
    @FXML private Label signupMessage;

    @FXML
    private void initialize() {
        // Majors
        signupMajor.getItems().addAll(
            "Computer Science",
            "Business Administration",
            "Electrical Engineering",
            "Biology",
            "Psychology",
            "Mathematics"
        );

        // Semesters 2025–2026
        String[] terms = { "Spring", "Summer", "Fall", "Winter" };
        for (int year = 2025; year <= 2026; year++) {
            for (String t : terms) {
                signupSemester.getItems().add(t + " " + year);
            }
        }
    }

    // LOGIN HANDLER
    @FXML
    private void handleLogin() throws IOException {
        String id = loginStudentID.getText();
        String pw = loginPassword.getText();

        if (id.isBlank() || pw.isBlank()) {
            loginMessage.setText("Please enter both ID and password.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT password FROM users WHERE student_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                loginMessage.setText("Account not found.");
                return;
            }

            String storedPw = rs.getString("password");
            if (!storedPw.equals(pw)) {
                loginMessage.setText("Incorrect password.");
                return;
            }

            // Success -> go to main page
            App.setCurrentStudentId(id);
            App.setRoot("primary");

        } catch (SQLException e) {
            loginMessage.setText("DB error: " + e.getMessage());
        }
    }

    // SIGNUP HANDLER
    @FXML
    private void handleSignup() {
        String name     = signupName.getText();
        String id       = signupStudentID.getText();
        String pw       = signupPassword.getText();
        String major    = signupMajor.getValue();
        String semester = signupSemester.getValue();

        if (name.isBlank() || id.isBlank() || pw.isBlank()) {
            signupMessage.setText("All fields are required.");
            return;
        }
        if (!id.matches("\\d+")) {
            signupMessage.setText("Student ID must be numbers only.");
            return;
        }
        if (pw.length() < 8) {
            signupMessage.setText("Password must be at least 8 characters.");
            return;
        }
        if (major == null || semester == null) {
            signupMessage.setText("Select major and semester.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql =
                "INSERT INTO users (student_id, name, password, major, semester) " +
                "VALUES (?, ?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, id);
            stmt.setString(2, name);
            stmt.setString(3, pw);
            stmt.setString(4, major);
            stmt.setString(5, semester);
            stmt.executeUpdate();

            signupMessage.setText("Account created!");

        } catch (SQLIntegrityConstraintViolationException dup) {
            signupMessage.setText("Student ID already exists.");
        } catch (SQLException e) {
            signupMessage.setText("DB error: " + e.getMessage());
        }
    }
}
