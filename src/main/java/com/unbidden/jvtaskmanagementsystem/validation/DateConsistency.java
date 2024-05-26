package com.unbidden.jvtaskmanagementsystem.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Checks whether field {@code startDateName} value is before or equal to
 * field {@code endDateName} value.
 */
@Constraint(validatedBy = DateConsistencyValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DateConsistency {
    String startDateName() default "startDate";
    String endDateName() default "endDate";

    String message() default "Start date and end date must be consistent.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
