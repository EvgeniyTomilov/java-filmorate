package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.ValidateService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
@Slf4j
public class FilmController {
    final FilmService filmService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film create(@RequestBody Film film) {
        log.info("create film: {}", film);
        ValidateService.validateFilm(film);
        return filmService.addFilm(film);

    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        ValidateService.validateFilm(film);
        return filmService.updateFilm(film);
    }

    @DeleteMapping
    public void deleteFilm(@PathVariable Long id) {
        log.info("Удаление фильма " + id + "...");
        filmService.deleteFilm(id);
        log.info("Фильм удален");
    }

    @GetMapping
    public Collection<Film> getAllFilms() {
        log.info("Вызов всех фильмов...");
        return filmService.getAllFilms();
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable Long id) {
        log.info("Get film by id = {}", id);
        ValidateService.validateId(id);
        return filmService.getFilmById(id);
    }

    @PutMapping(value = "/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Добавление лайка пользователем " + userId + " фильму " + id + "...");
        filmService.addLike(id, userId);
        log.info("Лайк добавлен");
    }

    @DeleteMapping(value = "/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Удаление лайка пользователем " + userId + " фильму " + id + "...");
        filmService.removeLike(id, userId);
        log.info("Лайк удален");
    }

    @GetMapping(value = "/popular")
    public List<Film> getPopular(@RequestParam(value = "count", required = false, defaultValue = "10") Integer count) {
        log.info("Вызов популярных фильмов...");
        return filmService.getPopularFilms(count);
    }
}

