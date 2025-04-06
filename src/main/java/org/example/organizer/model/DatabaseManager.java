package org.example.organizer.model;

import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String URL = "jdbc:postgresql://localhost:5432/schedule_organizer";
    private static final String USER = "postgres";
    private static final String PASSWORD = "12345";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // CRUD Operations
    public static void addExam(Exam exam) throws SQLException {
        String sql = "INSERT INTO exams (course_code, course_name, exam_date, exam_time, location) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, exam.getCourseCode());
            stmt.setString(2, exam.getCourseName());
            stmt.setDate(3, Date.valueOf(exam.getExamDate()));
            stmt.setTime(4, Time.valueOf(exam.getExamTime()));
            stmt.setString(5, exam.getLocation());
            stmt.executeUpdate();
        }
    }

    public static List<Exam> getAllExams() throws SQLException {
        List<Exam> exams = new ArrayList<>();
        String sql = "SELECT * FROM exams ORDER BY exam_date, exam_time";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Exam exam = new Exam(
                        rs.getInt("id"),
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getDate("exam_date").toLocalDate(),
                        rs.getTime("exam_time").toLocalTime(),
                        rs.getString("location")
                );
                exams.add(exam);
                // Print each exam retrieved
                System.out.println("Retrieved Exam: " + exam);
            }
        }
        System.out.println("Total exams retrieved: " + exams.size());
        return exams;
    }

    public static void updateExam(Exam exam) throws SQLException {
        String sql = "UPDATE exams SET course_code = ?, course_name = ?, exam_date = ?, exam_time = ?, location = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, exam.getCourseCode());
            stmt.setString(2, exam.getCourseName());
            stmt.setDate(3, Date.valueOf(exam.getExamDate()));
            stmt.setTime(4, Time.valueOf(exam.getExamTime()));
            stmt.setString(5, exam.getLocation());
            stmt.setInt(6, exam.getId());
            stmt.executeUpdate();
        }
    }

    public static void deleteExam(int id) throws SQLException {
        String sql = "DELETE FROM exams WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public static void exportToCSV(String filePath) throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM exams");
             PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {

            // Write header
            writer.println("id,course_code,course_name,exam_date,exam_time,location");

            // Write data
            while (rs.next()) {
                writer.printf("%d,%s,%s,%s,%s,%s%n",
                        rs.getInt("id"),
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getDate("exam_date"),
                        rs.getTime("exam_time"),
                        rs.getString("location"));
            }
        } catch (IOException e) {
            throw new SQLException("Failed to write CSV file: " + e.getMessage());
        }
    }

    public static int importFromCSV(String filePath) throws SQLException {
        int count = 0;
        try (Connection conn = getConnection();
             BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

            reader.readLine(); // Skip header

            String insertSql = "INSERT INTO exams (course_code, course_name, exam_date, exam_time, location) " +
                    "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length < 6) continue;

                    try {
                        Exam exam = new Exam(
                                0,
                                parts[1],
                                parts[2],
                                LocalDate.parse(parts[3]),
                                LocalTime.parse(parts[4]),
                                parts[5]
                        );
                        validateExamFields(exam);

                        setExamParameters(stmt, exam);
                        stmt.executeUpdate();
                        count++;
                    } catch (Exception e) {
                        System.err.println("Skipping invalid record: " + line);
                    }
                }
            }
        } catch (IOException e) {
            throw new SQLException("Failed to read CSV file: " + e.getMessage());
        }
        return count;
    }

    private static void setExamParameters(PreparedStatement stmt, Exam exam) throws SQLException {
        stmt.setString(1, exam.getCourseCode());
        stmt.setString(2, exam.getCourseName());
        stmt.setDate(3, Date.valueOf(exam.getExamDate()));
        stmt.setTime(4, Time.valueOf(exam.getExamTime()));
        stmt.setString(5, exam.getLocation());
    }

    private static void validateExamFields(Exam exam) throws SQLException {
        if (exam.getCourseCode() == null || exam.getCourseCode().length() > 50) {
            throw new SQLException("Course code must be 1-50 characters");
        }
        if (exam.getCourseName() == null || exam.getCourseName().length() > 100) {
            throw new SQLException("Course name must be 1-100 characters");
        }
        if (exam.getLocation() == null || exam.getLocation().length() > 100) {
            throw new SQLException("Location must be 1-100 characters");
        }
        if (exam.getExamDate() == null || exam.getExamDate().isBefore(LocalDate.now())) {
            throw new SQLException("Exam date cannot be in the past");
        }
        if (exam.getExamTime() == null) {
            throw new SQLException("Exam time is required");
        }
    }

    public static boolean hasConflict(Exam newExam) throws SQLException {
        String sql = "SELECT COUNT(*) FROM exams WHERE exam_date = ? AND exam_time = ? AND location = ? AND id != ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(newExam.getExamDate()));
            stmt.setTime(2, Time.valueOf(newExam.getExamTime()));
            stmt.setString(3, newExam.getLocation());
            stmt.setInt(4, newExam.getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public static List<Exam> getUpcomingExams() throws SQLException {
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);

        String sql = "SELECT * FROM exams WHERE exam_date BETWEEN ? AND ? ORDER BY exam_date, exam_time";
        List<Exam> exams = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(today));
            stmt.setDate(2, Date.valueOf(nextWeek));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Exam exam = new Exam(
                        rs.getInt("id"),
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getDate("exam_date").toLocalDate(),
                        rs.getTime("exam_time").toLocalTime(),
                        rs.getString("location")
                );
                exams.add(exam);
            }
        }
        return exams;
    }

    private static void addExam(Connection conn, Exam exam) throws SQLException {
        String sql = "INSERT INTO exams (id, course_code, course_name, exam_date, exam_time, location) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, exam.getId());
            stmt.setString(2, exam.getCourseCode());
            stmt.setString(3, exam.getCourseName());
            stmt.setDate(4, Date.valueOf(exam.getExamDate()));
            stmt.setTime(5, Time.valueOf(exam.getExamTime()));
            stmt.setString(6, exam.getLocation());
            stmt.executeUpdate();
        }
    }
}
