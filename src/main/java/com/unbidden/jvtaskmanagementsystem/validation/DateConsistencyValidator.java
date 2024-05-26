package com.unbidden.jvtaskmanagementsystem.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.time.LocalDate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("null")
public class DateConsistencyValidator implements ConstraintValidator<DateConsistency, Object> {
    private static final Logger LOGGER = LogManager.getLogger(DateConsistencyValidator.class);

    private String startDateFieldName;

    private String endDateFieldName;

    @Override
    public void initialize(DateConsistency constraintAnnotation) {
        startDateFieldName = constraintAnnotation.startDateName();
        endDateFieldName = constraintAnnotation.endDateName();

        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        Class<?> clazz = value.getClass();
        Field[] fields = clazz.getDeclaredFields();
        Field.setAccessible(fields, true);
        LocalDate startDate = null;
        LocalDate endDate = null;

        for (Field field : fields) {
            try {
                if (field.getName().equals(startDateFieldName)) {
                    Object potentialStartDate = field.get(value);
                    if (potentialStartDate != null) {
                        if (potentialStartDate instanceof LocalDate) {
                            startDate = (LocalDate)potentialStartDate;
                            LOGGER.info("startDate aquired.");
                        } else {
                            throw new UnsupportedOperationException("Field with name "
                                    + endDateFieldName + " in class " + clazz.getName()
                                    + " is supposed to be of type " + LocalDate.class.getName()
                                    + ", but is currently "
                                    + potentialStartDate.getClass().getName());
                        }
                    }
                }
                if (field.getName().equals(endDateFieldName)) {
                    Object potentialEndDate = field.get(value);
                    if (potentialEndDate != null) {
                        if (potentialEndDate instanceof LocalDate) {
                            endDate = (LocalDate)potentialEndDate;
                            LOGGER.info("endDate aquired.");
                        } else {
                            throw new UnsupportedOperationException("Field with name "
                                    + endDateFieldName + " in class " + clazz.getName()
                                    + " is supposed to be of type " + LocalDate.class.getName()
                                    + ", but is currently "
                                    + potentialEndDate.getClass().getName());
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Unable to access field data. Class: "
                        + clazz.getName() + "; Field: " + field.getName(), e);
            }
            
        }

        if (startDate == null && endDate == null) {
            LOGGER.info("Dates are not specified. Validation skipped.");
            return true;
        }
        if (endDate == null) {
            LOGGER.info("End date is not specified.");
            if (startDate.isEqual(LocalDate.now()) || startDate.isAfter(LocalDate.now())) {
                LOGGER.info("Start date is ligit. Validation successful.");
                return true;
            }
        }
        if (startDate == null) {
            LOGGER.info("Start date is not specified.");
            if (endDate.isAfter(LocalDate.now())) {
                LOGGER.info("End date is ligit. Validation successful.");
                return true;
            }
        }
        if (startDate != null && endDate != null) {
            LOGGER.info("Both dates are specified.");
            if ((startDate.isEqual(LocalDate.now()) || startDate.isAfter(LocalDate.now()))
                    && endDate.isAfter(startDate)) {
                LOGGER.info("Dates are ligit. Validation successful.");
                return true;
            }
        }
        LOGGER.info("Validation failed.");
        return false;
    }
}
