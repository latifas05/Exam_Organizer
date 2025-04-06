import org.example.organizer.model.DatabaseManager;
import org.example.organizer.model.Exam;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseManagerTest {
    private static final String TEST_CODE = "TEST001";

    @BeforeEach
    void setUp() throws SQLException {
        DatabaseManager.getConnection().createStatement()
                .execute("DELETE FROM exams WHERE course_code = '" + TEST_CODE + "'");
    }

    @Test
    void testAddAndGetExam() throws SQLException {
        Exam testExam = new Exam(0, TEST_CODE, "Test Course",
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 30),
                "Test Room");

        DatabaseManager.addExam(testExam);
        List<Exam> exams = DatabaseManager.getAllExams();

        assertFalse(exams.isEmpty());
        assertTrue(exams.stream().anyMatch(e -> e.getCourseCode().equals(TEST_CODE)));
    }

    @Test
    void testConflictDetection() throws SQLException {
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime time = LocalTime.of(14, 0);
        String location = "Conflict Room";

        Exam exam1 = new Exam(0, TEST_CODE, "Course 1", date, time, location);
        DatabaseManager.addExam(exam1);

        Exam exam2 = new Exam(0, "TEST002", "Course 2", date, time, location);
        assertTrue(DatabaseManager.hasConflict(exam2));
    }

    @AfterAll
    static void cleanUp() throws SQLException {
        DatabaseManager.getConnection().createStatement()
                .execute("DELETE FROM exams WHERE course_code LIKE 'TEST%'");
    }
}