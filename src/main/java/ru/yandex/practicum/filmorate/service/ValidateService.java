package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;


@Slf4j
@Component
public class ValidateService {
    public static void validateId(User user) throws ValidationException {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("email пустой или не содержит символ @: {}", user);
            throw new ValidationException("email не может быть пустой и должна содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().contains(" ") || user.getLogin().isEmpty()) {
            log.warn("login пустой или  содержит пробелы: {}", user);
            throw new ValidationException("login не может быть пустым и содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            log.warn("name пустое: {}", user);
            user.setName(user.getLogin());
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("birthday еще не наступило: {}", user);
            throw new ValidationException("birthday не может быть в будущем");
        }
    }

    public static void validateFilm(Film film) throws ValidationException {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("name пустое: {}", film);
            throw new ValidationException("name название не может быть пустым");
        }
        if (film.getDescription().length() > 200) {
            log.warn("description больше 200 символов: {}", film);
            throw new ValidationException("максимальная длина description — 200 символов");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("realiseData раньше появления кинематографа : {}", film);
            throw new ValidationException("realiseData — не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() < 0) {
            log.warn("duration меньше ноля: {}", film);
            throw new ValidationException("duration фильма должна быть положительной.");
        }
    }

    public static void validateId(Long id) throws NullPointerException {
        if (id <= 0) {
            log.warn("id  не может быть отрицательным : {}", id);
            throw new NullPointerException("id  не может быть отрицательным : {}");
        }
    }
}
