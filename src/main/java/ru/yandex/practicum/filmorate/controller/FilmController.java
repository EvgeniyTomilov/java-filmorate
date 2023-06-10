package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        log.info("Добавление фильма");
        return filmService.addFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Обновление фильма " + film.getId() + "...");
        return filmService.updateFilm(film);
    }

    @GetMapping
    public Collection<Film> getAllFilms() {
        log.info("Вызов списка всех фильмов.");
        return filmService.getAllFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        log.info("Вызов фильма по id:" + id + "...");
        return filmService.getFilmById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Добавление лайка пользователем " + userId + " фильму " + id + "...");
        filmService.addLike(id, userId);
        log.info("Лайк добавлен");
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Удаление лайка пользователем " + userId + " фильму " + id + "...");
        filmService.deleteLike(id, userId);
        log.info("Лайк удален");
    }

    @GetMapping("/popular")
    public List<Film> getListPopularFilms(@RequestParam(value = "count", required = false, defaultValue = "10") Integer count) {
        log.info("Вызов списка популярных фильмов...");
        return filmService.getListPopularFilms(count);
    }


    @GetMapping("/director/{directorId}")
    public List<Film> getByDirectorId(@PathVariable("directorId") Long directorId, @RequestParam String sortBy) {
        return filmService.getDirectorFilms(directorId, sortBy);
    }
  
    @DeleteMapping("/{filmId}")
    public void deleteFilmById(@PathVariable Long filmId) {
        log.info("Удаление фильма по id:" + filmId + "...");
        filmService.deleteFilm(filmId);
        log.info("Фильм удален");
    }
}