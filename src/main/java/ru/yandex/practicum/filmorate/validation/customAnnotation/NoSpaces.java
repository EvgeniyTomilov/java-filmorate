package ru.yandex.practicum.filmorate.validation.customAnnotation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NoSpacesAnnotationValidator.class)
@Documented
public @interface NoSpaces {
    String message();

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
