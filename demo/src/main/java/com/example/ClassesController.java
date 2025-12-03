package com.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.dao.CourseDao;

public class ClassesController {

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
    private TableColumn<CourseView, Number> creditsColumn;

    @FXML
    private TableColumn<CourseView, String> deptColumn;

    @FXML
    private TableColumn<CourseView, String> majorColumn;

    @FXML
    private TableColumn<CourseView, String> semesterColumn;

    private CourseDao courseDao = new CourseDao();

    @FXML
    private void initialize() {
        // Set up table columns
        codeColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCourseCode()));
        nameColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCourseName()));
        creditsColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getCredits()));
        deptColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDepartment()));
        majorColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getMajor()));
        semesterColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSemester()));

        // Load filter options and initial data
        loadFilters();
        loadCourses(null, null);
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
}
