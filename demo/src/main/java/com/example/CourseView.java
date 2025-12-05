package com.example;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CourseView {

    private final StringProperty courseCode;
    private final StringProperty courseName;
    private final IntegerProperty credits;
    private final StringProperty department;
    private final StringProperty major;
    private final StringProperty semester;

    // checkbox state for selection
    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    // whether this course row is already enrolled for the current student (for
    // greying out)
    private final BooleanProperty alreadyEnrolled = new SimpleBooleanProperty(false);

    // enrollment_id from the enrollments table (for dropping + grades)
    private final IntegerProperty enrollmentId = new SimpleIntegerProperty(0);

    public CourseView(String courseCode,
            String courseName,
            int credits,
            String department,
            String major,
            String semester) {
        this.courseCode = new SimpleStringProperty(courseCode);
        this.courseName = new SimpleStringProperty(courseName);
        this.credits = new SimpleIntegerProperty(credits);
        this.department = new SimpleStringProperty(department);
        this.major = new SimpleStringProperty(major);
        this.semester = new SimpleStringProperty(semester);
    }

    public String getCourseCode() {
        return courseCode.get();
    }

    public void setCourseCode(String value) {
        courseCode.set(value);
    }

    public StringProperty courseCodeProperty() {
        return courseCode;
    }

    public String getCourseName() {
        return courseName.get();
    }

    public void setCourseName(String value) {
        courseName.set(value);
    }

    public StringProperty courseNameProperty() {
        return courseName;
    }

    public int getCredits() {
        return credits.get();
    }

    public void setCredits(int value) {
        credits.set(value);
    }

    public IntegerProperty creditsProperty() {
        return credits;
    }

    public String getDepartment() {
        return department.get();
    }

    public void setDepartment(String value) {
        department.set(value);
    }

    public StringProperty departmentProperty() {
        return department;
    }

    public String getMajor() {
        return major.get();
    }

    public void setMajor(String value) {
        major.set(value);
    }

    public StringProperty majorProperty() {
        return major;
    }

    public String getSemester() {
        return semester.get();
    }

    public void setSemester(String value) {
        semester.set(value);
    }

    public StringProperty semesterProperty() {
        return semester;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean value) {
        selected.set(value);
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    // for grey-out logic
    public boolean isAlreadyEnrolled() {
        return alreadyEnrolled.get();
    }

    public void setAlreadyEnrolled(boolean value) {
        alreadyEnrolled.set(value);
    }

    public BooleanProperty alreadyEnrolledProperty() {
        return alreadyEnrolled;
    }

    public int getEnrollmentId() {
        return enrollmentId.get();
    }

    public void setEnrollmentId(int value) {
        enrollmentId.set(value);
    }

    public IntegerProperty enrollmentIdProperty() {
        return enrollmentId;
    }
}
