package ru.yandex.practicum.filmorate.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.error.exception.ObjectNotFoundException;

import javax.validation.ValidationException;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice("ru.yandex.practicum.filmorate.controller")
@Slf4j
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIncorrectFormatError(final ValidationException e) {
        log.info(" код 400");
        return Map.of(
                "error", "Ошибка Ошибка валидации.",
                "errorMessage", e.getMessage()
        );
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFoundError(final NoSuchElementException e) {
        log.info("код 404");
        return Map.of(
                "error", "искомый объект не найден",
                "errorMessage", e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleObjectNotFoundException(final ObjectNotFoundException e) {
        log.debug("Объект не найден", e.getMessage(), e);
        return new ErrorResponse("404", e.getMessage());
    }
}

