package org.tpkprav.dbconnector.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SafeSqlInputValidator.class)
@Documented
public @interface SafeSqlInput {
    String message() default "Input contains characters or patterns that are not permitted";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}