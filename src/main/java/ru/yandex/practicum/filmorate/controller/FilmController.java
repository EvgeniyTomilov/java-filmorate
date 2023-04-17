package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

@RestController
@RequestMapping("/films")
public class FilmController {

    @PostMapping
    public void create(@RequestBody Film film) {

    }

    @PutMapping
    public void update(@RequestBody Film film){

    }

    @GetMapping
    public Collection<Film> findAll() {
        return filmMap.values();
    }
}
