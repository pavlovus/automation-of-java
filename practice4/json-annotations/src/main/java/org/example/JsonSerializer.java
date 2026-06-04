package org.example;

import java.lang.reflect.Field;

public class JsonSerializer {
    public static void validate(Object obj) {
        if (obj == null) throw new IllegalArgumentException("Об'єкт для серіалізації не може бути null");

        Class<?> clazz = obj.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);

            if (field.isAnnotationPresent(JsonIgnore.class)) continue;

            if (field.isAnnotationPresent(JsonRequired.class)) {
                Object value = null;

                try {
                    value = field.get(obj);
                } catch (IllegalAccessException e) {
                    throw new IllegalArgumentException("Не вдалося прочитати поле '" + field.getName() + "': " + e.getMessage());
                }

                if (value == null)
                    throw new IllegalArgumentException("Поле '" + field.getName() + "' позначено @JsonRequired але має значення null");
            }
        }
    }
}