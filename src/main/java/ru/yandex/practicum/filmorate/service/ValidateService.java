package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

@Component
public class ValidateService {
    void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || user.getEmail().contains("@")) {
            throw new RuntimeException("email не может быть пустой и должна содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isEmpty()) {
            throw new RuntimeException("login не может быть пустым и содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            throw new RuntimeException("name для отображения может быть пустым");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new RuntimeException("birthday не может быть в будущем");
        }
    }

    void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new RuntimeException("name название не может быть пустым");
        }
        if (film.getDescription().length() < 201) {
            throw new RuntimeException("максимальная длина description — 200 символов");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1985, 12, 28))) {
            throw new RuntimeException("realiseData — не раньше 28 декабря 1895 года");
        }
        if (film.getDuration().isNegative()) {
            throw new RuntimeException("duration фильма должна быть положительной.");

        }
    }

}
