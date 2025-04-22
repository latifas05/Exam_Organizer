# Exam Schedule Organizer

## Project Description
The Exam Schedule Organizer is a JavaFX application designed to help students and educators manage exam schedules. It provides a user-friendly interface to add, update, delete, and view exam information, including course details, exam dates/times, and locations. The application stores data in a PostgreSQL database and supports importing/exporting data via CSV files.

## Objectives
1. Create a centralized system for managing exam schedules
2. Provide conflict detection for exams at the same time/location
3. Enable easy data import/export functionality
4. Offer quick access to upcoming exams
5. Implement a clean, intuitive user interface

## Project Requirements List
1. Database connection to PostgreSQL for persistent data storage
2. CRUD operations for exam management
3. Table view to display all exams
4. Form for adding/editing exam details
5. Input validation for all fields
6. Conflict detection for exam scheduling
7. CSV import/export functionality
8. Upcoming exams view (next 7 days)
9. Responsive UI with modern styling

## Presentation:
[Exam_Schedule_Presantation.pdf](https://github.com/user-attachments/files/19855832/Exam_Schedule_Presantation.pdf)

## Test Cases and Outputs
1. Add New Exam
![image](https://github.com/user-attachments/assets/11265f1f-44f6-41dc-ad5b-fadd3175ce72)
2 Update Exam: 4/25/2026 - 4/15/2025
![image](https://github.com/user-attachments/assets/c738ef32-aca1-47cb-8096-061bec2cc1e7)
1. Show Upcoming Exams
![image](https://github.com/user-attachments/assets/1ed4041a-feef-4c32-ab7b-dd1d2d96ab1e)

## Documentation

### Algorithms and Data Structures
- **ObservableList**: Used to manage the collection of exams for the TableView
- **DateTimeFormatter**: For parsing and formatting date/time values
- **JDBC**: For database connectivity and operations
- **File I/O**: For CSV import/export functionality

### Key Functions/Modules
1. **DatabaseManager**: Handles all database operations (CRUD, import/export)
2. **Exam**: Model class representing exam data
3. **MainController**: Manages the UI and user interactions
4. **Input Validation**: Ensures all entered data meets requirements
5. **Conflict Detection**: Checks for scheduling conflicts before adding exams

### Challenges Faced
1. **Time Formatting**: Implementing proper time input validation and formatting
2. **Database Connection**: Ensuring stable connection handling and error recovery
3. **CSV Import/Export**: Handling various edge cases in file parsing

## Files
The project uses:
1. PostgreSQL database file (schema provided)
2. CSV files for import/export
3. FXML file for UI layout
4. Java class files for application logic


## Database Schema

The application uses a PostgreSQL database with the following schema:

```sql
CREATE TABLE exams (
    id SERIAL PRIMARY KEY,
    course_code VARCHAR(50) NOT NULL,
    course_name VARCHAR(100) NOT NULL,
    exam_date DATE NOT NULL,
    exam_time TIME NOT NULL,
    location VARCHAR(100) NOT NULL
);



