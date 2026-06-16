package org.example;

public class Student {
    private final String name;
    private final int course;
    private boolean active;

    public Student(String name, int course) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name of student can't be empty");
        if (course < 1 || course > 6) throw new IllegalArgumentException("Year of study should be between 1 and 6");
        this.name = name;
        this.course = course;
        this.active = true;
    }

    public String getName() { return name;   }
    public int getCourse() { return course; }
    public boolean isActive() { return active; }
    public void deactivate() { active = false; }

    @Override
    public String toString() { return "Student [name=" + name + ", course=" + course + ", active=" + active + "]"; }
}