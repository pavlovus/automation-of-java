package org.example;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class GradeBookTest {
    private Student student;
    private GradeBook gradeBook;

    @BeforeEach
    void setUp() {
        student = new Student("Ivan", 2);
        gradeBook = new GradeBook(student);
    }

    @Test
    @Tag("basic")
    @DisplayName("Simple test: add grade and check average")
    void testAddGradeAndAverage() {
        gradeBook.addGrade(90);
        gradeBook.addGrade(100);
        assertEquals(95.0, gradeBook.getAverage(), "Average grade should be 95.0");
    }

    @ParameterizedTest
    @ValueSource(ints = { -5, 105, 200 })
    @Tag("basic")
    @DisplayName("Parameterized test (1 param): check exception for invalid grade")
    void testAddInvalidGradeThrowsException(int invalidGrade) {
        assertThrows(IllegalArgumentException.class, () -> {
            gradeBook.addGrade(invalidGrade);
        }, "Should throw an exception for grade: " + invalidGrade);
    }

    @ParameterizedTest
    @CsvSource({
            "95, Excellent (A)",
            "85, Good (B)",
            "70, Satisfactory",
            "50, Unsatisfactory"
    })
    @Tag("advanced")
    @DisplayName("Parameterized test (set): check academic status")
    void testAcademicStatus(int grade, String expectedStatus) {
        gradeBook.addGrade(grade);
        assertEquals(expectedStatus, gradeBook.getAcademicStatus());
    }

    @Test
    @Tag("advanced")
    @DisplayName("Scholarship test: uses Assumptions")
    void testScholarshipEligibility() {
        gradeBook.addGrade(95);
        gradeBook.addGrade(100);

        assumeTrue(student.isActive(), "Skipping test because student is not active");

        assertTrue(gradeBook.isEligibleForScholarship(), "Student with a high grade should be eligible for scholarship");
        
        student.deactivate();
        assertFalse(gradeBook.isEligibleForScholarship(), "Inactive student should not be eligible for scholarship");
    }

    @TestFactory
    @Tag("advanced")
    @DisplayName("Dynamic test: check student creation with various parameters")
    Collection<DynamicTest> dynamicTestsForStudentCreation() {
        return Arrays.asList(
                DynamicTest.dynamicTest("Valid student", () -> assertDoesNotThrow(() -> new Student("Maria", 3))),
                DynamicTest.dynamicTest("Empty name", () -> assertThrows(IllegalArgumentException.class, () -> new Student("", 1))),
                DynamicTest.dynamicTest("Course greater than 6", () -> assertThrows(IllegalArgumentException.class, () -> new Student("Petro", 7))),
                DynamicTest.dynamicTest("Course less than 1", () -> assertThrows(IllegalArgumentException.class, () -> new Student("Oksana", 0)))
        );
    }
}