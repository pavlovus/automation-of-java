package org.example;

public class Main {

    public static void main(String[] args) {
        Student user = new Student(1, "Вус Павло", "ivan@ukma.edu.ua", "ФІ", 2, "secret123", true);
        System.out.println("Вхідний об'єкт: " + user);
        System.out.println();

        StudentMapper mapper = new StudentMapper();

        try {
            String json = mapper.toJson(user);
            System.out.println("Результат JSON:");
            System.out.println(json);
        } catch (IllegalArgumentException e) {
            System.out.println("Помилка: " + e.getMessage());
        }
    }
}