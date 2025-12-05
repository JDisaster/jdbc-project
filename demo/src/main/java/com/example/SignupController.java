package com.example;

import java.io.IOException;
import java.time.LocalDate;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.example.dao.DatabaseConnection;
import java.sql.SQLIntegrityConstraintViolationException;


public class SignupController {

    @FXML
    private TextField studentNameField;

    @FXML
    private TextField newStudentID;

    @FXML
    private PasswordField newPassword;

    @FXML
    private ComboBox<String> majorComboBox;

    @FXML
    private ComboBox<SemesterItem> semesterComboBox;

    @FXML
    private Label statusMessage;

    @FXML
    private void initialize() {
        // Majors
        majorComboBox.getItems().addAll(
                "Computer Science",
                "Business Administration",
                "Electrical Engineering",
                "Biology",
                "Psychology",
                "Mathematics"
        );

        // Semesters for 2025–2026
        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        int month = today.getMonthValue();

        // Map month -> term index
        // 0 = Spring, 1 = Summer, 2 = Fall, 3 = Winter
        int currentTermIndex;
        if (month >= 1 && month <= 3) {
            currentTermIndex = 0; // Spring
        } else if (month >= 4 && month <= 6) {
            currentTermIndex = 1; // Summer
        } else if (month >= 7 && month <= 9) {
            currentTermIndex = 2; // Fall
        } else {
            currentTermIndex = 3; // Winter
        }

        // Clamp year to 2025–2026 range
        if (currentYear < 2025) {
            currentYear = 2025;
        } else if (currentYear > 2026) {
            currentYear = 2026;
        }

        String[] terms = { "Spring", "Summer", "Fall", "Winter" };

        SemesterItem defaultSelection = null;

        for (int year = 2025; year <= 2026; year++) {
            for (int termIndex = 0; termIndex < terms.length; termIndex++) {
                String label = terms[termIndex] + " " + year;

                boolean isPast;
                if (year < currentYear) {
                    isPast = true;
                } else if (year > currentYear) {
                    isPast = false;
                } else {
                    // Same year -> compare term index.
                    // Past = strictly before the current term.
                    isPast = termIndex < currentTermIndex;
                }

                SemesterItem item = new SemesterItem(label, isPast);
                semesterComboBox.getItems().add(item);

                // Default = the FIRST non-past semester (this will include the current one)
                if (!isPast && defaultSelection == null) {
                    defaultSelection = item;
                }
            }
        }

        if (defaultSelection != null) {
            semesterComboBox.setValue(defaultSelection);
        }

        // Cell factory to grey out past semesters and make them unselectable in the list
        semesterComboBox.setCellFactory(listView -> new ListCell<SemesterItem>() {
            @Override
            protected void updateItem(SemesterItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setDisable(false);
                    setStyle("");
                } else {
                    setText(item.getLabel());
                    if (item.isPast()) {
                        setDisable(true);                  // cannot be selected
                        setStyle("-fx-text-fill: gray;");  // greyed out
                    } else {
                        setDisable(false);
                        setStyle("");
                    }
                }
            }
        });

        // Button cell (what you see when closed) – just shows the selected label
        semesterComboBox.setButtonCell(new ListCell<SemesterItem>() {
            @Override
            protected void updateItem(SemesterItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getLabel());
                }
            }
        });
    }

    @FXML
    private void handleCreateAccount() {
        String studentName = studentNameField.getText();
        String studentID   = newStudentID.getText();
        String password    = newPassword.getText();
        String major       = majorComboBox.getValue();
        SemesterItem semesterItem = semesterComboBox.getValue();

        // Validation with error messages

        if (studentName == null || studentName.isBlank()) {
            statusMessage.setText("Student name cannot be empty.");
            return;
        }
        if (studentID == null || studentID.isBlank()) {
            statusMessage.setText("Student ID cannot be empty.");
            return;
        }
        if (password == null || password.isBlank()) {
            statusMessage.setText("Password cannot be empty.");
            return;
        }
        if (major == null || major.isBlank()) {
            statusMessage.setText("Please select a major.");
            return;
        }
        if (semesterItem == null) {
            statusMessage.setText("Please select a semester.");
            return;
        }
        if (!studentID.matches("\\d+")) {
            statusMessage.setText("Student ID must contain numbers only.");
            return;
        }
        if (password.length() < 8) {
            statusMessage.setText("Password must be at least 8 characters long.");
            return;
        }
        if (semesterItem.isPast()) {
            statusMessage.setText("Please select a current or future semester.");
            return;
        }

        String semester = semesterItem.getLabel();

        // Insert into DB
        String sql = "INSERT INTO users (student_id, name, password, major, semester) " +
                    "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentID);
            stmt.setString(2, studentName);
            stmt.setString(3, password);
            stmt.setString(4, major);
            stmt.setString(5, semester);

            stmt.executeUpdate();

            statusMessage.setText(
                "Account created for " + studentName +
                " (" + studentID + "), Major: " + major +
                ", Semester: " + semester
            );

            App.setCurrentStudentId(studentID);
            App.setRoot("primary");

        } catch (SQLIntegrityConstraintViolationException dup) {
            // student_id already exists
            statusMessage.setText("Student ID " + studentID + " is already taken.");
        } catch (SQLException e) {
            e.printStackTrace();
            statusMessage.setText("Database error creating account: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            statusMessage.setText("Error opening main screen: " + e.getMessage());
        }
    }


    @FXML
    private void goBackToLogin() throws IOException {
        App.setRoot("login");
    }

    // Helper class: holds label and "past" flag
    public static class SemesterItem {
        private final String label;
        private final boolean past;

        public SemesterItem(String label, boolean past) {
            this.label = label;
            this.past = past;
        }

        public String getLabel() {
            return label;
        }

        public boolean isPast() {
        return past;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
