import org.example.organizer.controller.MainController;
import org.example.organizer.model.Exam;
import org.junit.jupiter.api.Test;
import javafx.collections.ObservableList;
import java.time.LocalDate;
import java.time.LocalTime;
import static org.junit.jupiter.api.Assertions.*;

class MainControllerTest {
    @Test
    void testCreateExamFromFields() {
        MainController controller = new MainController();

        controller.codeField.setText("CS101");
        controller.nameField.setText("Programming");
        controller.datePicker.setValue(LocalDate.of(2023, 12, 15));
        controller.timeField.setText("09:00");
        controller.locationField.setText("Room 101");

        Exam exam = controller.createExamFromFields();

        assertEquals("CS101", exam.getCourseCode());
        assertEquals("Programming", exam.getCourseName());
        assertEquals(LocalDate.of(2023, 12, 15), exam.getExamDate());
        assertEquals(LocalTime.of(9, 0), exam.getExamTime());
        assertEquals("Room 101", exam.getLocation());
    }

    @Test
    void testInputValidation() {
        MainController controller = new MainController();

        assertFalse(controller.validateInput());

        controller.codeField.setText("CS101");
        controller.nameField.setText("Programming");
        assertFalse(controller.validateInput());

        controller.datePicker.setValue(LocalDate.now().plusDays(1));
        controller.timeField.setText("09:00");
        controller.locationField.setText("Room 101");
        assertTrue(controller.validateInput());

        controller.timeField.setText("9:00");
        assertFalse(controller.validateInput());

        controller.timeField.setText("09:00");
        controller.datePicker.setValue(LocalDate.now().minusDays(1));
        assertFalse(controller.validateInput());
    }
}