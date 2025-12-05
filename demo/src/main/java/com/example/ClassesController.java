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
import java.util.HashSet;
import java.util.Set;

import com.example.dao.CourseDao;
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
        // table & column must be editable
        classesTable.setEditable(true);
        selectColumn.setEditable(true);

        // normal columns
        codeColumn.setCellValueFactory(data -> data.getValue().courseCodeProperty());
        nameColumn.setCellValueFactory(data -> data.getValue().courseNameProperty());
        creditsColumn.setCellValueFactory(data -> data.getValue().creditsProperty().asObject());
        deptColumn.setCellValueFactory(data -> data.getValue().departmentProperty());
        majorColumn.setCellValueFactory(data -> data.getValue().majorProperty());
        semesterColumn.setCellValueFactory(data -> data.getValue().semesterProperty());

        // checkbox column
        selectColumn.setCellValueFactory(cd -> cd.getValue().selectedProperty());
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));

        // NEW: grey out rows that are already enrolled & disable them
        classesTable.setRowFactory(tv -> new TableRow<CourseView>() {
            @Override
            protected void updateItem(CourseView item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setStyle("");
                    setDisable(false);
                } else if (item.isAlreadyEnrolled()) {
                    setStyle("-fx-background-color: #e0e0e0; -fx-opacity: 0.7;");
                    setDisable(true); // disables checkbox & row selection
                } else {
                    setStyle("");
                    setDisable(false);
                }
            }
        });

        // filters + data
        loadFilters();
        loadCourses(null, null);
    }

    @FXML
    private void handleEnroll() {
        String studentId = App.getCurrentStudentId();
        if (studentId == null || studentId.isEmpty()) {
            showError("Enrollment error", "No student is logged in. Please log in again.");
            return;
        }

        // Get selected semester (required to enroll)
        String semester = semesterComboBox.getSelectionModel().getSelectedItem();
        if (semester == null || semester.isEmpty()) {
            showError("Enrollment error", "Please select a semester before enrolling.");
            return;
        }

        // Collect selected courses
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

        // Calculate total credits of selected courses
        int newCredits = 0;
        for (CourseView cv : selected) {
            newCredits += cv.getCredits();
        }

        // Check existing credits and insert if <= 18 total
        try (Connection conn = com.example.dao.DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            // Get existing credits for this student + semester
            int existingCredits = 0;
            String creditSql = "SELECT COALESCE(SUM(c.credits), 0) AS total " +
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
                                " units, which exceeds the 18-unit limit.");
                return;
            }

            // Insert enrollments, avoiding duplicates
            String insertSql = "INSERT INTO enrollments (student_id, course_code, semester, enrollment_date) " +
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

            // clear selections and reload table
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
            if ("All".equals(majorFilter))
                majorFilter = null;
            if ("All".equals(semesterFilter))
                semesterFilter = null;

            var list = courseDao.getCourses(majorFilter, semesterFilter);

            // find which (course_code, semester) pairs this student is already enrolled in
            String studentId = App.getCurrentStudentId();
            Set<String> enrolledKeys = new HashSet<>();
            if (studentId != null && !studentId.isBlank()) {
                String sql = "SELECT course_code, semester FROM enrollments WHERE student_id = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                        PreparedStatement stmt = conn.prepareStatement(sql)) {

                    stmt.setString(1, studentId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            String key = rs.getString("course_code") + "|" + rs.getString("semester");
                            enrolledKeys.add(key);
                        }
                    }
                }
            }

            // Mark each CourseView as alreadyEnrolled if it’s in that set
            for (CourseView cv : list) {
                String key = cv.getCourseCode() + "|" + cv.getSemester();
                if (enrolledKeys.contains(key)) {
                    cv.setAlreadyEnrolled(true);
                } else {
                    cv.setAlreadyEnrolled(false);
                }
            }

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
