package com.example;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;

import com.example.dao.DatabaseConnection;

public class PrimaryController {

    // --- top labels ---
    @FXML
    private Label studentIdLabel;

    @FXML
    private Label studentNameLabel;

    @FXML
    private Label studentMajorLabel;

    @FXML
    private Label studentSemesterLabel;

    @FXML
    private Button dropClassesButton;

    // --- table for enrolled classes ---
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
    private void initialize() {
        System.out.println("DEBUG PrimaryController.initialize()");

        // always show the student ID we think is logged in
        String studentId = App.getCurrentStudentId();
        if (studentId == null || studentId.isBlank()) {
            studentIdLabel.setText("Student ID: (not set)");
            if (studentNameLabel != null)     studentNameLabel.setText("Name: (unknown)");
            if (studentMajorLabel != null)    studentMajorLabel.setText("Major: (unknown)");
            if (studentSemesterLabel != null) studentSemesterLabel.setText("Current Semester: (unknown)");
        } else {
            studentIdLabel.setText("Student ID: " + studentId);
        }

        // set up table columns
        if (enrolledTable != null) {
            enrolledTable.setEditable(true);

            if (enrolledSelectColumn != null) {
                enrolledSelectColumn.setCellValueFactory(cd -> cd.getValue().selectedProperty());
                enrolledSelectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(enrolledSelectColumn));
            }

            enrolledCodeColumn.setCellValueFactory(data -> data.getValue().courseCodeProperty());
            enrolledNameColumn.setCellValueFactory(data -> data.getValue().courseNameProperty());
            enrolledCreditsColumn.setCellValueFactory(data -> data.getValue().creditsProperty());
            enrolledDeptColumn.setCellValueFactory(data -> data.getValue().departmentProperty());
            enrolledMajorColumn.setCellValueFactory(data -> data.getValue().majorProperty());
            enrolledSemesterColumn.setCellValueFactory(data -> data.getValue().semesterProperty());
        }

        // drop button disabled by default
        if (dropClassesButton != null) {
            dropClassesButton.setDisable(true);
        }

        // if we have a logged-in student, load their info + schedule
        if (studentId != null && !studentId.isBlank()) {
            loadStudentInfo(studentId);
            loadEnrolledClasses(studentId);
        }
    }

    // ------------------ student header info ------------------

    private void loadStudentInfo(String studentId) {
        System.out.println("DEBUG loadStudentInfo for studentId=" + studentId);

        String sql = "SELECT name, major, semester FROM users WHERE student_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String name     = rs.getString("name");
                    String major    = rs.getString("major");
                    String semester = rs.getString("semester");

                    System.out.println("DEBUG found user: " + name + ", " + major + ", " + semester);

                    if (studentNameLabel != null) {
                        studentNameLabel.setText("Name: " + name);
                    }
                    if (studentMajorLabel != null) {
                        studentMajorLabel.setText("Major: " + major);
                    }
                    if (studentSemesterLabel != null) {
                        studentSemesterLabel.setText("Current Semester: " + semester);
                    }
                } else {
                    System.out.println("DEBUG no user row found for " + studentId);
                    if (studentNameLabel != null)     studentNameLabel.setText("Name: (not found)");
                    if (studentMajorLabel != null)    studentMajorLabel.setText("Major: (not found)");
                    if (studentSemesterLabel != null) studentSemesterLabel.setText("Current Semester: (not found)");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (studentNameLabel != null)     studentNameLabel.setText("Name: (error)");
            if (studentMajorLabel != null)    studentMajorLabel.setText("Major: (error)");
            if (studentSemesterLabel != null) studentSemesterLabel.setText("Current Semester: (error)");
        }
    }

    // ------------------ enrolled classes table ------------------

    private void loadEnrolledClasses(String studentId) {
        ObservableList<CourseView> data = FXCollections.observableArrayList();

        if (studentId == null || studentId.isBlank() || enrolledTable == null) {
            if (enrolledTable != null) {
                enrolledTable.setItems(data);
            }
            if (dropClassesButton != null) {
                dropClassesButton.setDisable(true);
            }
            return;
        }

        String sql =
            "SELECT e.enrollment_id, " +
            "       c.course_code, c.course_name, c.credits, c.department, " +
            "       u.major, e.semester " +
            "FROM enrollments e " +
            "JOIN courses c ON e.course_code = c.course_code " +
            "JOIN users   u ON e.student_id   = u.student_id " +
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

                    // store enrollment_id into the row so Drop can use it
                    cv.setEnrollmentId(rs.getInt("enrollment_id"));

                    // whenever a row is (un)checked, update Drop button state
                    cv.selectedProperty().addListener((obs, oldVal, newVal) -> updateDropButtonState());

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

    // ------------------ Drop Classes ------------------

    @FXML
    private void handleDropClasses() {
        if (enrolledTable == null) return;

        ObservableList<CourseView> items    = enrolledTable.getItems();
        ObservableList<CourseView> selected = FXCollections.observableArrayList();
        for (CourseView cv : items) {
            if (cv.isSelected()) {
                selected.add(cv);
            }
        }

        if (selected.isEmpty()) {
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Drop Classes");
        confirm.setHeaderText("Are you sure you want to drop these classes?");
        confirm.setContentText("Number of classes to drop: " + selected.size());
        var result = confirm.showAndWait();

        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        String studentId = App.getCurrentStudentId();
        if (studentId == null || studentId.isBlank()) {
            return;
        }

        String deleteGradesSql = "DELETE FROM grades      WHERE enrollment_id = ?";
        String deleteEnrollSql = "DELETE FROM enrollments WHERE enrollment_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement deleteGrades = conn.prepareStatement(deleteGradesSql);
                 PreparedStatement deleteEnroll = conn.prepareStatement(deleteEnrollSql)) {

                for (CourseView cv : selected) {
                    int enrollmentId = cv.getEnrollmentId();

                    deleteGrades.setInt(1, enrollmentId);
                    deleteGrades.addBatch();

                    deleteEnroll.setInt(1, enrollmentId);
                    deleteEnroll.addBatch();
                }

                deleteGrades.executeBatch();
                deleteEnroll.executeBatch();
                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                Alert err = new Alert(Alert.AlertType.ERROR);
                err.setTitle("Drop Error");
                err.setHeaderText("Error dropping classes");
                err.setContentText(e.getMessage());
                err.showAndWait();
                return;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setTitle("Drop Error");
            err.setHeaderText("Database error dropping classes");
            err.setContentText(e.getMessage());
            err.showAndWait();
            return;
        }

        // reload after successful drop
        loadEnrolledClasses(studentId);
    }

    // ------------------ Navigation ------------------

    @FXML
    private void goToClasses() throws IOException {
        App.setRoot("classes");
    }

    // left as a no-op in case old FXML still references it
    @FXML
    private void switchToSecondary() throws IOException {
        // no-op
    }
}
