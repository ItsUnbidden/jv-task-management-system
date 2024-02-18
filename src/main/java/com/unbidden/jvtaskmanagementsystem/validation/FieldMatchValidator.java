package com.unbidden.jvtaskmanagementsystem.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.util.Objects;

public class FieldMatchValidator implements ConstraintValidator<FieldMatch, Object> {
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        Class<?> clazz = value.getClass();
        Field[] fields = clazz.getDeclaredFields();
        Field.setAccessible(fields, true);
        boolean isFirstValueSet = false;
        Object firstValue = null;

        for (Field field : fields) {
            if (field.isAnnotationPresent(ApplyMatching.class)) {
                try {
                    if (!isFirstValueSet) {
                        firstValue = field.get(value);
                        isFirstValueSet = true;
                    } else {
                        Object secondValue = field.get(value);
                        return Objects.equals(firstValue, secondValue);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(String.format(
                            "Unable to get field value. Field: %s, Class: %s",
                            field, clazz.toString()), e);
                }
            }
        }
        return false;
    }
}
