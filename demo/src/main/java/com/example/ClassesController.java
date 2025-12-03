package com.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.dao.CourseDao;
import java.sql.*;
import com.example.CourseView;
import com.example.dao.DatabaseConnection;


public class ClassesController {
   
    @FXML
    private TableColumn<CourseView, Boolean> selectColumn;

    @FXML
    private ComboBox<String> majorComboBox;

    @FXML
    private ComboBox<String> semesterComboBox;

    @FXML
    private TableView<CourseView> classesTable;

    @FXML
    private TableColumn<CourseView, String> codeColumn;

    @FXML
    private TableColumn<CourseView, String> nameColumn;

    @FXML
    private TableColumn<CourseView, Integer> creditsColumn;

    @FXML
    private TableColumn<CourseView, String> deptColumn;

    @FXML
    private TableColumn<CourseView, String> majorColumn;

    @FXML
    private TableColumn<CourseView, String> semesterColumn;

    private CourseDao courseDao = new CourseDao();

    @FXML
    private void initialize() {
        // existing cell value factories:
        codeColumn.setCellValueFactory(data -> data.getValue().courseCodeProperty());
        nameColumn.setCellValueFactory(data -> data.getValue().courseNameProperty());
        creditsColumn.setCellValueFactory(data -> data.getValue().creditsProperty().asObject());
        deptColumn.setCellValueFactory(data -> data.getValue().departmentProperty());
        majorColumn.setCellValueFactory(data -> data.getValue().majorProperty());
        semesterColumn.setCellValueFactory(data -> data.getValue().semesterProperty());

        selectColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectColumn.setCellFactory(col -> new javafx.scene.control.cell.CheckBoxTableCell<>());


        // load filters and data
        loadFilters();
        loadCourses(null, null);
    }
    @FXML
    private void handleEnroll() {
        // 1. Get current student (replace fallback with your login logic)
        String studentId = "1001"; // temp
        if (studentId == null || studentId.isEmpty()) {
            showError("Enrollment error", "No student is logged in. Please log in again.");
            return;
        }



        // 2. Get selected semester (required to enroll)
        String semester = semesterComboBox.getSelectionModel().getSelectedItem();
        if (semester == null || semester.isEmpty()) {
            showError("Enrollment error", "Please select a semester before enrolling.");
            return;
        }

        // 3. Collect selected courses
        List<CourseView> selected = new ArrayList<>();
        for (CourseView cv : classesTable.getItems()) {
            if (cv.isSelected()) {
                selected.add(cv);
            }
        }

        if (selected.isEmpty()) {
            showError("Enrollment error", "Please check at least one class to enroll.");
            return;
        }

        // 4. Calculate total credits of selected courses
        int newCredits = 0;
        for (CourseView cv : selected) {
            newCredits += cv.getCredits();
        }

        // 5. Check existing credits and insert if <= 18 total
        try (Connection conn = com.example.dao.DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            // Get existing credits for this student + semester
            int existingCredits = 0;
            String creditSql =
                    "SELECT COALESCE(SUM(c.credits), 0) AS total " +
                    "FROM enrollments e " +
                    "JOIN courses c ON e.course_code = c.course_code " +
                    "WHERE e.student_id = ? AND e.semester = ?";

            try (PreparedStatement creditStmt = conn.prepareStatement(creditSql)) {
                creditStmt.setString(1, studentId);
                creditStmt.setString(2, semester);
                try (ResultSet rs = creditStmt.executeQuery()) {
                    if (rs.next()) {
                        existingCredits = rs.getInt("total");
                    }
                }
            }

            int totalIfEnrolled = existingCredits + newCredits;
            if (totalIfEnrolled > 18) {
                conn.rollback();
                showError(
                    "Enrollment error",
                    "You are currently enrolled in " + existingCredits + " units.\n" +
                    "Adding " + newCredits + " units would make " + totalIfEnrolled +
                    " units, which exceeds the 18-unit limit."
                );
                return;
            }

            // 6. Insert enrollments, avoiding duplicates
            String insertSql =
                    "INSERT INTO enrollments (student_id, course_code, semester, enrollment_date) " +
                    "SELECT ?, ?, ?, CURDATE() " +
                    "FROM DUAL WHERE NOT EXISTS (" +
                    "  SELECT 1 FROM enrollments " +
                    "  WHERE student_id = ? AND course_code = ? AND semester = ?" +
                    ")";

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                for (CourseView cv : selected) {
                    insertStmt.setString(1, studentId);
                    insertStmt.setString(2, cv.getCourseCode());
                    insertStmt.setString(3, semester);
                    insertStmt.setString(4, studentId);
                    insertStmt.setString(5, cv.getCourseCode());
                    insertStmt.setString(6, semester);
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();
            }

            conn.commit();

            // 7. clear selections and reload table
            for (CourseView cv : selected) {
                cv.setSelected(false);
            }


            showInfo("Enrollment successful",
                    "You have been enrolled in " + selected.size() +
                    " course(s) for " + semester +
                    ". Total units this term: " + totalIfEnrolled + ".");

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Enrollment error", e.getMessage());
        }
    }


    private void loadFilters() {
        try {
            List<String> majors = new ArrayList<>();
            majors.add("All");
            majors.addAll(courseDao.getAllMajors());
            majorComboBox.setItems(FXCollections.observableArrayList(majors));
            majorComboBox.getSelectionModel().selectFirst();

            List<String> semesters = new ArrayList<>();
            semesters.add("All");
            semesters.addAll(courseDao.getAllSemesters());
            semesterComboBox.setItems(FXCollections.observableArrayList(semesters));
            semesterComboBox.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            showError("Error loading filters", e.getMessage());
        }
    }

    private void loadCourses(String majorFilter, String semesterFilter) {
        try {
            if ("All".equals(majorFilter)) majorFilter = null;
            if ("All".equals(semesterFilter)) semesterFilter = null;

            var list = courseDao.getCourses(majorFilter, semesterFilter);
            ObservableList<CourseView> data = FXCollections.observableArrayList(list);
            classesTable.setItems(data);
        } catch (SQLException e) {
            showError("Error loading courses", e.getMessage());
        }
    }

    @FXML
    private void handleFilter() {
        String major = majorComboBox.getSelectionModel().getSelectedItem();
        String semester = semesterComboBox.getSelectionModel().getSelectedItem();
        loadCourses(major, semester);
    }

    @FXML
    private void handleClear() {
        majorComboBox.getSelectionModel().selectFirst();
        semesterComboBox.getSelectionModel().selectFirst();
        loadCourses(null, null);
    }

    @FXML
    private void handleBack() throws IOException {
        // Go back to your main/primary page
        App.setRoot("primary");
    }

    private void showError(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
}

}
