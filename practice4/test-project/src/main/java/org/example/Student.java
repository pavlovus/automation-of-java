package org.example;

@JsonSerializable
public class Student {
    private int id;
    @JsonField(name = "full_name")
    private String name;
    private String email;
    private String faculty;
    private int year;
    @JsonIgnore
    private String password;
    @JsonField(name = "is_active")
    private boolean active;

    public Student(int id, String name, String email, String faculty, int year, String password, boolean active) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.faculty = faculty;
        this.year = year;
        this.password = password;
        this.active = active;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getFaculty() { return faculty; }
    public int getYear() { return year; }
    public String getPassword() { return password; }
    public boolean getActive() { return active; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setFaculty(String faculty) { this.faculty = faculty; }
    public void setYear(int year) { this.year = year; }
    public void setPassword(String password) { this.password = password; }
    public void setActive(boolean active) { this.active = active; }
}