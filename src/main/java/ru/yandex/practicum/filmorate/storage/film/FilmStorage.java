package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    long generateId();

    Film save(Film film);

    Film update(Film film);

    List<Film> getFilms();
}
