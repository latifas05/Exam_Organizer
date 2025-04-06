package org.example.organizer.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.ObjectProperty;

import java.time.LocalDate;
import java.time.LocalTime;

public class Exam {
    private final StringProperty courseCode = new SimpleStringProperty();
    private final StringProperty courseName = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> examDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalTime> examTime = new SimpleObjectProperty<>();
    private final StringProperty location = new SimpleStringProperty();
    private int id;

    public Exam(int id, String courseCode, String courseName, LocalDate examDate, LocalTime examTime, String location) {
        this.id = id;
        this.courseCode.set(courseCode);
        this.courseName.set(courseName);
        this.examDate.set(examDate);
        this.examTime.set(examTime);
        this.location.set(location);
    }

    // Getters for properties
    public int getId() { return id; }
    public String getCourseCode() { return courseCode.get(); }
    public StringProperty courseCodeProperty() { return courseCode; }

    public String getCourseName() { return courseName.get(); }
    public StringProperty courseNameProperty() { return courseName; }

    public LocalDate getExamDate() { return examDate.get(); }
    public ObjectProperty<LocalDate> examDateProperty() { return examDate; }

    public LocalTime getExamTime() { return examTime.get(); }
    public ObjectProperty<LocalTime> examTimeProperty() { return examTime; }

    public String getLocation() { return location.get(); }
    public StringProperty locationProperty() { return location; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setCourseCode(String courseCode) { this.courseCode.set(courseCode); }
    public void setCourseName(String courseName) { this.courseName.set(courseName); }
    public void setExamDate(LocalDate examDate) { this.examDate.set(examDate); }
    public void setExamTime(LocalTime examTime) { this.examTime.set(examTime); }
    public void setLocation(String location) { this.location.set(location); }
}