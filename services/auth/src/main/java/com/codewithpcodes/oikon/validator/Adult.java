package com.codewithpcodes.oikon.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AgeValidator.class)
@Documented
public @interface Adult {
    String message() default "You must be at least {min} years old";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    int min() default 18;
}
