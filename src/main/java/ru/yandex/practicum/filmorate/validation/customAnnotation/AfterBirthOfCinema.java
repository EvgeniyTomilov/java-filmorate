package ru.yandex.practicum.filmorate.validation.customAnnotation;


import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = AfterBirthOfCinemaAnnotationValidator.class)
@Documented
public @interface AfterBirthOfCinema {
    String message();

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
