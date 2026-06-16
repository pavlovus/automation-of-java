package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GradeBook {
    public static final double SCHOLARSHIP_THRESHOLD = 91;

    private final Student student;
    private final List<Integer> grades = new ArrayList<>();

    public GradeBook(Student student) {
        if (student == null) throw new IllegalArgumentException("Student cannot be null");
        this.student = student;
    }

    public void addGrade(int grade) {
        if (grade < 0 || grade > 100) throw new IllegalArgumentException("Grade must be between 0 and 100, received: " + grade);
        grades.add(grade);
    }

    public double getAverage() {
        if (grades.isEmpty()) return 0.0;
        return grades.stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }

    public boolean isEligibleForScholarship() { return student.isActive() && getAverage() >= SCHOLARSHIP_THRESHOLD; }

    public String getAcademicStatus() {
        double avg = getAverage();
        if (avg >= 91)  return "Excellent (A)";
        if (avg >= 81)  return "Good (B)";
        if (avg >= 60)  return "Satisfactory";
        return "Unsatisfactory";
    }

    public int getHighestGrade() { return grades.isEmpty() ? -1 : Collections.max(grades); }

    public int getLowestGrade() { return grades.isEmpty() ? -1 : Collections.min(grades); }

    public Student getStudent() { return student; }
    public List<Integer> getGrades() { return Collections.unmodifiableList(grades); }
}