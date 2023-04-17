package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dao.FilmRepository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.ValidateService;

import java.util.List;

@RestController
@RequestMapping("films")
@Slf4j
public class FilmController {
    final ValidateService validateService;
    FilmRepository repository;

    public FilmController(ValidateService validateService, FilmRepository repository) {
        this.validateService = validateService;
        this.repository = repository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film create(@RequestBody Film film) {
        log.info("create film: {}", film);
        validateService.validateFilm(film);
       return repository.save(film);

    }

    @PutMapping
    public Film update(@RequestBody Film film){
        validateService.validateFilm(film);
        return repository.update(film);
    }

    @GetMapping
    public List<Film> getFilms() {
        log.info("Текущее количество films: {}", repository.getFilms().size());
        return repository.getFilms();
    }
}
