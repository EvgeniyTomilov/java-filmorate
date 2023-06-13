package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.util.List;


public interface FilmStorage extends Storage<Film> {

    List<Film> getFilmsSortedByYears(Long directorId);

    List<Film> getFilmsSortedByLikes(Long directorId);

    List<Film> getCommonFilms(Long userId, Long friendId);
    List<Film> searchFilms(String query, String[] searchParameters);
}
