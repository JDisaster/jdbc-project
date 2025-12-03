package com.example.dao;

import com.example.CourseView;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseDao {

    public List<CourseView> getCourses(String majorFilter, String semesterFilter) throws SQLException {
        List<CourseView> courses = new ArrayList<>();

        StringBuilder sb = new StringBuilder(
            "SELECT DISTINCT " +
            "c.course_code, " +
            "c.course_name, " +
            "c.credits, " +
            "c.department, " +
            "u.major, " +
            "e.semester " +
            "FROM courses c " +
            "JOIN enrollments e ON c.course_code = e.course_code " +
            "JOIN users u ON e.student_id = u.student_id " +
            "WHERE 1=1 "
        );

        if (majorFilter != null && !majorFilter.equals("All")) {
            sb.append(" AND u.major = ?");
        }

        if (semesterFilter != null && !semesterFilter.equals("All")) {
            sb.append(" AND e.semester = ?");
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sb.toString())) {

            int idx = 1;

            if (majorFilter != null && !majorFilter.equals("All")) {
                stmt.setString(idx++, majorFilter);
            }

            if (semesterFilter != null && !semesterFilter.equals("All")) {
                stmt.setString(idx++, semesterFilter);
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                courses.add(new CourseView(
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getInt("credits"),
                        rs.getString("department"),
                        rs.getString("major"),
                        rs.getString("semester")
                ));
            }
        }

        return courses;
    }

    public List<String> getAllMajors() throws SQLException {
        List<String> majors = new ArrayList<>();
        String sql = "SELECT DISTINCT major FROM users ORDER BY major";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                majors.add(rs.getString("major"));
            }
        }

        return majors;
    }

    public List<String> getAllSemesters() throws SQLException {
        List<String> semesters = new ArrayList<>();
        String sql = "SELECT DISTINCT semester FROM enrollments ORDER BY semester";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                semesters.add(rs.getString("semester"));
            }
        }

        return semesters;
    }
}
