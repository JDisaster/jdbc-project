package com.example;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import com.example.dao.DatabaseConnection;

public class PrimaryController {

    // --- top bar ---
    @FXML
    private ImageView profileImageView;

    @FXML
    private Label studentIdLabel;

    // --- center table: enrolled classes ---
    @FXML
    private TableView<CourseView> enrolledTable;

    @FXML
    private TableColumn<CourseView, Boolean> enrolledSelectColumn;

    @FXML
    private TableColumn<CourseView, String> enrolledCodeColumn;

    @FXML
    private TableColumn<CourseView, String> enrolledNameColumn;

    @FXML
    private TableColumn<CourseView, Number> enrolledCreditsColumn;

    @FXML
    private TableColumn<CourseView, String> enrolledDeptColumn;

    @FXML
    private TableColumn<CourseView, String> enrolledMajorColumn;

    @FXML
    private TableColumn<CourseView, String> enrolledSemesterColumn;

    @FXML
    private Button dropClassesButton;

    @FXML
    private void initialize() {
        System.out.println("DEBUG PrimaryController.initialize()");

        // --- profile image (optional) ---
        URL imgUrl = getClass().getResource("/com/example/profile.png");
        if (imgUrl != null && profileImageView != null) {
            profileImageView.setImage(new Image(imgUrl.toExternalForm()));
        }

        // --- student ID label ---
        String studentId = App.getCurrentStudentId();
        if (studentIdLabel != null) {
            if (studentId != null && !studentId.isBlank()) {
                studentIdLabel.setText("Student ID: " + studentId);
            } else {
                studentIdLabel.setText("Student ID: (not set)");
            }
        }

        // --- enrolled classes table setup ---
        
    if (enrolledTable != null) {
        System.out.println("DEBUG: enrolledTable is not null");

        // Only configure select column if it was injected
        if (enrolledSelectColumn != null) {
            enrolledSelectColumn.setCellValueFactory(cd -> cd.getValue().selectedProperty());
            enrolledSelectColumn.setCellFactory(
                CheckBoxTableCell.forTableColumn(enrolledSelectColumn)
            );
        } else {
            System.out.println("DEBUG: enrolledSelectColumn is null (no checkbox column in FXML)");
        }

            enrolledCodeColumn.setCellValueFactory(data -> data.getValue().courseCodeProperty());
            enrolledNameColumn.setCellValueFactory(data -> data.getValue().courseNameProperty());
            enrolledCreditsColumn.setCellValueFactory(data -> data.getValue().creditsProperty());
            enrolledDeptColumn.setCellValueFactory(data -> data.getValue().departmentProperty());
            enrolledMajorColumn.setCellValueFactory(data -> data.getValue().majorProperty());
            enrolledSemesterColumn.setCellValueFactory(data -> data.getValue().semesterProperty());

            loadEnrolledClasses(App.getCurrentStudentId());
        }

        if (dropClassesButton != null) {
            dropClassesButton.setDisable(true);
        }
    }

    private void loadEnrolledClasses(String studentId) {
        ObservableList<CourseView> data = FXCollections.observableArrayList();

        if (studentId == null || studentId.isBlank()) {
            if (enrolledTable != null) {
                enrolledTable.setItems(data);
            }
            if (dropClassesButton != null) {
                dropClassesButton.setDisable(true);
            }
            return;
        }

        String sql =
            "SELECT c.course_code, c.course_name, c.credits, c.department, " +
            "       u.major, e.semester " +
            "FROM enrollments e " +
            "JOIN courses c ON e.course_code = c.course_code " +
            "JOIN users u ON e.student_id = u.student_id " +
            "WHERE e.student_id = ? " +
            "ORDER BY e.semester, c.course_code";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CourseView cv = new CourseView(
                            rs.getString("course_code"),
                            rs.getString("course_name"),
                            rs.getInt("credits"),
                            rs.getString("department"),
                            rs.getString("major"),
                            rs.getString("semester")
                    );

                    // whenever a row's checkbox changes, update Drop button state
                    cv.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                        updateDropButtonState();
                    });

                    data.add(cv);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        enrolledTable.setItems(data);
        updateDropButtonState();
        System.out.println("DEBUG: loaded " + data.size() + " enrolled classes");
    }

    /** Enable Drop button only if at least one class is selected. */
    private void updateDropButtonState() {
        if (dropClassesButton == null || enrolledTable == null) return;

        boolean anySelected = false;
        for (CourseView cv : enrolledTable.getItems()) {
            if (cv.isSelected()) {
                anySelected = true;
                break;
            }
        }
        dropClassesButton.setDisable(!anySelected);
    }

    @FXML
    private void handleDropClasses() {
        if (enrolledTable == null) return;

        // collect selected rows
        ObservableList<CourseView> items = enrolledTable.getItems();
        ObservableList<CourseView> selected = FXCollections.observableArrayList();
        for (CourseView cv : items) {
            if (cv.isSelected()) {
                selected.add(cv);
            }
        }

        if (selected.isEmpty()) {
            return; // button should already be disabled in this case
        }

        // 3) confirmation popup
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Drop Classes");
        confirm.setHeaderText("Are you sure you want to drop these classes?");
        confirm.setContentText("Number of classes to drop: " + selected.size());
        var result = confirm.showAndWait();

        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return; // user cancelled
        }

        String studentId = App.getCurrentStudentId();
        if (studentId == null || studentId.isBlank()) {
            return;
        }

        // delete from enrollments
        String sql =
            "DELETE FROM enrollments " +
            "WHERE student_id = ? AND course_code = ? AND semester = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (CourseView cv : selected) {
                stmt.setString(1, studentId);
                stmt.setString(2, cv.getCourseCode());
                stmt.setString(3, cv.getSemester());
                stmt.addBatch();
            }

            stmt.executeBatch();

        } catch (SQLException e) {
            e.printStackTrace();
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setTitle("Drop Error");
            err.setHeaderText("Error dropping classes");
            err.setContentText(e.getMessage());
            err.showAndWait();
            return;
        }

        // reload table
        loadEnrolledClasses(studentId);
    }

    @FXML
    private void goToClasses() throws IOException {
        App.setRoot("classes");
    }

    // harmless in case any old FXML references it
    @FXML
    private void switchToSecondary() throws IOException {
        // App.setRoot("secondary");
    }
}
