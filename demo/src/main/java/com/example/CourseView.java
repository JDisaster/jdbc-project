package com.example;

public class CourseView {
    private String courseCode;
    private String courseName;
    private int credits;
    private String department;
    private String major;
    private String semester;

    public CourseView(String courseCode, String courseName, int credits,
                      String department, String major, String semester) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credits = credits;
        this.department = department;
        this.major = major;
        this.semester = semester;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public int getCredits() {
        return credits;
    }

    public String getDepartment() {
        return department;
    }

    public String getMajor() {
        return major;
    }

    public String getSemester() {
        return semester;
    }
}
