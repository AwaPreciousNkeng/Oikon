package com.codewithpcodes.oikon.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;

public class AgeValidator implements ConstraintValidator<Adult, LocalDate> {

    private int min;

    @Override
    public void initialize(Adult constraintAnnotation) {
        this.min = constraintAnnotation.min();
    }

    @Override
    public boolean isValid(LocalDate dob, ConstraintValidatorContext context) {
        if (dob == null) return true; // @NotNull should handle this
        return Period.between(dob, LocalDate.now()).getYears() >= min;
    }
}
