package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.ValidateService;

import java.util.List;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
@Slf4j
public class FilmController {
    final FilmService filmService;

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable Long id) {
        log.info("Get film by id = {}", id);
        ValidateService.validateId(id);
        return filmService.getFilmById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film create(@RequestBody Film film) {
        log.info("create film: {}", film);
        ValidateService.validateFilm(film);
        return filmService.save(film);

    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        ValidateService.validateFilm(film);
        return filmService.update(film);
    }

    @GetMapping
    public List<Film> getFilms() {
        log.info("Текущее количество films: {}", filmService.getFilms().size());
        return filmService.getFilms();
    }

    @PutMapping("/{id}/like/{userId}")
    public Film addLike(@PathVariable Long id, @PathVariable Long userId) {
        return filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film deleteLikeByUserId(@PathVariable Long id, @PathVariable Long userId) {
        return filmService.deleteLikeByUserId(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> filmCount(@RequestParam(defaultValue = "10", value = "count") String count) {
        return filmService.getTopByLikes(Long.parseLong(count));
    }
}

