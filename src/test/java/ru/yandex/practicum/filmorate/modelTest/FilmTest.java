package ru.yandex.practicum.filmorate.modelTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;

public class FilmTest {
    private final Film correctFilmData = Film.builder().
            name("В бой идут одни старики")
            .description("Фильм, посвящённый тематике Великой Отечественной войны")
            .releaseDate(LocalDate.parse("1974-09-12"))
            .duration(87)
            .build();
    private final Film incorrectFilmData = Film.builder().
            name("В бой идут одни «старики»")
            .description("Эта эскадрилья стала «поющей» - так капитан Титаренко подбирал себе новичков. Его" +
                    " старикам было не больше двадцати, но желторотик ов, пополнение из летных училищ ускоренного" +
                    " выпуска, в бой все равно, по возможности, не пускали..")
            .releaseDate(LocalDate.parse("1125-09-10"))
            .duration(-87)
            .build();
    private final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = validatorFactory.getValidator();

    @Test
    void shouldCreateFilm() {
        Set<ConstraintViolation<Film>> validationViolations = validator.validate(correctFilmData);
        Assertions.assertTrue(validationViolations.isEmpty());
    }

    @Test
    void shouldNotCreateFilm() {
        Set<ConstraintViolation<Film>> validationViolations = validator.validate(incorrectFilmData);
        Assertions.assertEquals(3, validationViolations.size());
    }
}
