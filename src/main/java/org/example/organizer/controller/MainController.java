package org.example.organizer.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import org.example.organizer.model.DatabaseManager;
import org.example.organizer.model.Exam;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

public class MainController {
    @FXML private TableView<Exam> examsTable;
    @FXML private TableColumn<Exam, String> codeColumn;
    @FXML private TableColumn<Exam, String> nameColumn;
    @FXML private TableColumn<Exam, LocalDate> dateColumn;
    @FXML private TableColumn<Exam, LocalTime> timeColumn;
    @FXML private TableColumn<Exam, String> locationColumn;

    @FXML
    public TextField codeField;
    @FXML
    public TextField nameField;
    @FXML
    public DatePicker datePicker;
    @FXML
    public TextField timeField;
    @FXML
    public TextField locationField;
    @FXML private Label statusLabel;

    private ObservableList<Exam> examsData = FXCollections.observableArrayList();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        setupTableColumns();
        loadInitialData();
        setupTableSelectionListener();
        setupTimeFieldFormatter();
    }

    private void setupTableColumns() {
        codeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCourseCode()));

        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCourseName()));

        dateColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getExamDate()));

        timeColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getExamTime()));

        locationColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getLocation()));

        // date column
        dateColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }
        });

        timeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(DateTimeFormatter.ofPattern("HH:mm")));
            }
        });

        // time column
        timeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("HH:mm")));
                }
            }
        });
    }

    private void loadInitialData() {
        try {
            List<Exam> exams = DatabaseManager.getAllExams();
            System.out.println("Number of exams loaded: " + exams.size());
            examsData.setAll(exams);
            examsTable.setItems(examsData);
            updateStatus("Loaded " + examsData.size() + " exams");
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load exams: " + e.getMessage());
            updateStatus("Error loading exams");
        }
    }

    private void setupTableSelectionListener() {
        examsTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showExamDetails(newValue));
    }

    private void setupTimeFieldFormatter() {
        timeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("^\\d{0,2}:?\\d{0,2}$")) {
                timeField.setText(oldValue);
            }
        });
    }

    private void showExamDetails(Exam exam) {
        if (exam != null) {
            codeField.setText(exam.getCourseCode());
            nameField.setText(exam.getCourseName());
            datePicker.setValue(exam.getExamDate());
            timeField.setText(exam.getExamTime().format(timeFormatter));
            locationField.setText(exam.getLocation());
        }
    }

    @FXML
    private void clearFields() {
        examsTable.getSelectionModel().clearSelection();
        codeField.clear();
        nameField.clear();
        datePicker.setValue(null);
        timeField.clear();
        locationField.clear();
        updateStatus("Fields cleared");
    }

    @FXML
    private void handleAddExam() {
        if (validateInput()) {
            try {
                Exam newExam = createExamFromFields();
                if (DatabaseManager.hasConflict(newExam)) {
                    showConfirmation("Conflict Detected",
                            "There's already an exam scheduled at this time and location. Add anyway?",
                            () -> {
                                try {
                                    addExamToDatabase(newExam);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                } else {
                    addExamToDatabase(newExam);
                }
            } catch (SQLException e) {
                showAlert("Database Error", "Error adding exam: " + e.getMessage());
                updateStatus("Error adding exam");
            } catch (DateTimeParseException e) {
                showAlert("Invalid Time", "Please enter time in HH:MM format");
                updateStatus("Invalid time format");
            }
        }
    }

    private void addExamToDatabase(Exam exam) throws SQLException {
        DatabaseManager.addExam(exam);
        refreshTable();
        clearFields();
        updateStatus("Exam added successfully");
    }

    @FXML
    private void handleUpdateExam() {
        Exam selectedExam = examsTable.getSelectionModel().getSelectedItem();
        if (selectedExam == null) {
            showAlert("No Selection", "Please select an exam to update.");
            return;
        }

        if (validateInput()) {
            try {
                updateExamInDatabase(selectedExam);
            } catch (SQLException e) {
                showAlert("Database Error", "Error updating exam: " + e.getMessage());
                updateStatus("Error updating exam");
            } catch (DateTimeParseException e) {
                showAlert("Invalid Time", "Please enter time in HH:MM format");
                updateStatus("Invalid time format");
            }
        }
    }

    private void updateExamInDatabase(Exam exam) throws SQLException {
        exam.setCourseCode(codeField.getText());
        exam.setCourseName(nameField.getText());
        exam.setExamDate(datePicker.getValue());
        exam.setExamTime(LocalTime.parse(timeField.getText(), timeFormatter));
        exam.setLocation(locationField.getText());

        DatabaseManager.updateExam(exam);
        refreshTable();
        updateStatus("Exam updated successfully");
    }

    @FXML
    private void handleDeleteExam() {
        Exam selectedExam = examsTable.getSelectionModel().getSelectedItem();
        if (selectedExam == null) {
            showAlert("No Selection", "Please select an exam to delete.");
            return;
        }

        showConfirmation("Confirm Delete",
                "Are you sure you want to delete this exam?\n" + selectedExam.getCourseCode() + " - " +
                        selectedExam.getCourseName(), () -> {
                    try {
                        DatabaseManager.deleteExam(selectedExam.getId());
                        refreshTable();
                        clearFields();
                        updateStatus("Exam deleted successfully");
                    } catch (SQLException e) {
                        showAlert("Database Error", "Error deleting exam: " + e.getMessage());
                        updateStatus("Error deleting exam");
                    }
                });
    }

    @FXML
    private void handleRefresh() {
        refreshTable();
        updateStatus("Data refreshed");
    }

    private void refreshTable() {
        try {
            examsData.setAll(DatabaseManager.getAllExams());
            updateStatus("Loaded " + examsData.size() + " exams");
        } catch (SQLException e) {
            showAlert("Database Error", "Error loading exams: " + e.getMessage());
            updateStatus("Error loading exams");
        }
    }

    @FXML
    private void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Exams to CSV");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(examsTable.getScene().getWindow());

        if (file != null) {
            try {
                DatabaseManager.exportToCSV(file.getAbsolutePath());
                updateStatus("Exported " + examsData.size() + " exams to " + file.getName());
            } catch (SQLException e) {
                showAlert("Export Error", "Failed to export: " + e.getMessage());
                updateStatus("Export failed");
            }
        }
    }

    @FXML
    private void handleImport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Exams from CSV");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showOpenDialog(examsTable.getScene().getWindow());

        if (file != null) {
            showConfirmation("Confirm Import",
                    "This will add all exams from the file. Continue?", () -> {
                        try {
                            int count = DatabaseManager.importFromCSV(file.getAbsolutePath());
                            refreshTable();
                            updateStatus("Imported " + count + " exams from " + file.getName());
                        } catch (SQLException e) {
                            showAlert("Import Error", "Failed to import: " + e.getMessage());
                            updateStatus("Import failed");
                        }
                    });
        }
    }

    public Exam createExamFromFields() throws DateTimeParseException {
        return new Exam(
                0, // ID will be generated by database
                codeField.getText(),
                nameField.getText(),
                datePicker.getValue(),
                LocalTime.parse(timeField.getText(), timeFormatter),
                locationField.getText()
        );
    }

    public boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        if (codeField.getText() == null || codeField.getText().trim().isEmpty()) {
            errors.append("• Course code is required\n");
        }

        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            errors.append("• Course name is required\n");
        }

        if (datePicker.getValue() == null) {
            errors.append("• Exam date is required\n");
        } else if (datePicker.getValue().isBefore(LocalDate.now())) {
            errors.append("• Exam date cannot be in the past\n");
        }

        if (timeField.getText() == null || timeField.getText().trim().isEmpty()) {
            errors.append("• Exam time is required\n");
        } else {
            try {
                LocalTime.parse(timeField.getText(), timeFormatter);
            } catch (DateTimeParseException e) {
                errors.append("• Invalid time format (use HH:MM)\n");
            }
        }

        if (locationField.getText() == null || locationField.getText().trim().isEmpty()) {
            errors.append("• Location is required\n");
        }

        if (errors.length() > 0) {
            showAlert("Validation Error", "Please fix the following errors:\n\n" + errors.toString());
            return false;
        }

        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showConfirmation(String title, String message, Runnable onConfirm) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            onConfirm.run();
        }
    }

    @FXML
    private void handleShowUpcoming() {
        try {
            List<Exam> upcoming = DatabaseManager.getUpcomingExams();
            if (upcoming.isEmpty()) {
                Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
                infoAlert.setTitle("Upcoming Exams");
                infoAlert.setHeaderText(null);
                infoAlert.setContentText("No upcoming exams in the next 7 days");
                infoAlert.showAndWait();
            } else {

                Dialog<String> dialog = new Dialog<>();
                dialog.setTitle("Upcoming Exams");


                dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

                StringBuilder message = new StringBuilder();
                for (Exam exam : upcoming) {
                    message.append(String.format("• %s - %s\n   Date: %s\n   Time: %s\n   Location: %s\n\n",
                            exam.getCourseCode(),
                            exam.getCourseName(),
                            exam.getExamDate(),
                            exam.getExamTime(),
                            exam.getLocation()));
                }

                Label content = new Label(message.toString());
                content.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");
                dialog.getDialogPane().setContent(content);

                dialog.showAndWait();
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load upcoming exams: " + e.getMessage());
        }
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
    }
}