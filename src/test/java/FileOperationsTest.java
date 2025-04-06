import org.example.organizer.model.DatabaseManager;
import org.example.organizer.model.Exam;
import org.junit.jupiter.api.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import static org.junit.jupiter.api.Assertions.*;

class FileOperationsTest {
    private static final String TEST_FILE = "test_exams.csv";
    private static final String TEST_CODE = "CS101";
    private static final String TEST_NAME = "Programming Fundamentals";
    private static final String TEST_LOCATION = "Building A, Room 101";

    @BeforeEach
    @AfterEach
    void cleanUp() throws Exception {
        Files.deleteIfExists(Path.of(TEST_FILE));

        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM exams WHERE course_code = '" + TEST_CODE + "'");
        } catch (Exception e) {
        }
    }

    @Test
    void testExportImportCycle() throws Exception {
        Exam testExam = new Exam(0, TEST_CODE, TEST_NAME,
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 30),
                TEST_LOCATION);

        DatabaseManager.addExam(testExam);

        DatabaseManager.exportToCSV(TEST_FILE);
        assertTrue(Files.exists(Path.of(TEST_FILE)), "CSV file should be created");

        int initialCount = DatabaseManager.getAllExams().size();

        int importedCount = DatabaseManager.importFromCSV(TEST_FILE);
        assertTrue(importedCount > 0, "Should import at least one record");

        int newCount = DatabaseManager.getAllExams().size();
        assertEquals(initialCount + importedCount, newCount,
                "Record count should increase by imported amount");

        Files.deleteIfExists(Path.of(TEST_FILE));
    }
}