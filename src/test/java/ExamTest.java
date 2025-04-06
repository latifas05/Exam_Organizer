import org.example.organizer.model.Exam;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalTime;
import static org.junit.jupiter.api.Assertions.*;

class ExamTest {
    @Test
    void testExamCreation() {
        Exam exam = new Exam(1, "CS101", "Programming",
                LocalDate.of(2023, 12, 15),
                LocalTime.of(9, 0),
                "Room 101");

        assertEquals(1, exam.getId());
        assertEquals("CS101", exam.getCourseCode());
        assertEquals("Programming", exam.getCourseName());
        assertEquals(LocalDate.of(2023, 12, 15), exam.getExamDate());
        assertEquals(LocalTime.of(9, 0), exam.getExamTime());
        assertEquals("Room 101", exam.getLocation());
    }

    @Test
    void testPropertyBindings() {
        Exam exam = new Exam(1, "CS101", "Programming",
                LocalDate.now(),
                LocalTime.now(),
                "Room 101");

        assertNotNull(exam.courseCodeProperty());
        assertNotNull(exam.courseNameProperty());
        assertNotNull(exam.examDateProperty());
        assertNotNull(exam.examTimeProperty());
        assertNotNull(exam.locationProperty());
    }
}