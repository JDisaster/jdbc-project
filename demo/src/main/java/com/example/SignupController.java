package com.example;

import java.io.IOException;
import java.time.LocalDate;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SignupController {

    @FXML
    private TextField studentNameField;

    @FXML
    private TextField newEmail;

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
        // ----- Majors -----
        majorComboBox.getItems().addAll(
                "Computer Science",
                "Business Administration",
                "Electrical Engineering",
                "Biology",
                "Psychology",
                "Mathematics"
        );

        // ----- Semesters for 2025–2026 -----
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

        // Clamp year to 2025–2026 range as requested
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
        String email = newEmail.getText();
        String password = newPassword.getText();
        String major = majorComboBox.getValue();
        SemesterItem semesterItem = semesterComboBox.getValue();

        // ---- Validation with clear error messages ----

        // 1) All fields required
        if (studentName == null || studentName.isBlank()) {
            statusMessage.setText("Student name cannot be empty.");
            return;
        }

        if (email == null || email.isBlank()) {
            statusMessage.setText("Email cannot be empty.");
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

        // 3) Password must be at least 8 characters long
        if (password.length() < 8) {
            statusMessage.setText("Password must be at least 8 characters long.");
            return;
        }

        // 4) Semester cannot be in the past (extra safety)
        if (semesterItem.isPast()) {
            statusMessage.setText("Please select a current or future semester.");
            return;
        }

        // ---- All good ----
        Database.createUser(studentName, email, password, major);
        String semester = semesterItem.getLabel();
        statusMessage.setText(
                "Account created for " + studentName +
                " (" + email + "), Major: " + major +
                ", Semester: " + semester
        );
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
