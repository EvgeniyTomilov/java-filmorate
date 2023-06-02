package ru.yandex.practicum.filmorate.modelTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;

public class UserTest {
    private final User correctUserData = User.builder()
            .id(1L)
            .login("blat13")
            .name("Evgeniy Tomilov")
            .email("blat1985@mail.ru")
            .birthday(LocalDate.parse("1985-12-13"))
            .build();

    private final User incorrectUserData = User.builder()
            .id(1L)
            .login(" ")
            .name("Evgeniy Tomilov")
            .email("mail@/////.")
            .birthday(null)
            .build();

    private final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = validatorFactory.getValidator();

    @Test
    void shouldCreateUser() {
        Set<ConstraintViolation<User>> validationViolations = validator.validate(correctUserData);
        Assertions.assertTrue(validationViolations.isEmpty());
    }

    @Test
    void shouldNotCreateUser() {
        Set<ConstraintViolation<User>> validationViolations = validator.validate(incorrectUserData);
        Assertions.assertEquals(3, validationViolations.size());
    }
}
