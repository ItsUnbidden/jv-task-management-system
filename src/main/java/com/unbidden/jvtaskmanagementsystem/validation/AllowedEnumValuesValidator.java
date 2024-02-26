package com.unbidden.jvtaskmanagementsystem.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AllowedEnumValuesValidator implements ConstraintValidator<AllowedValues, Object> {
    private String[] allowedValues;

    @Override
    public void initialize(AllowedValues constraintAnnotation) {
        allowedValues = constraintAnnotation.value();
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        for (String allowedValue : allowedValues) {
            if (allowedValue.equals(value.toString())) {
                return true;
            }
        }
        return false;
    }
}
