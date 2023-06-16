package ru.yandex.practicum.filmorate.storage.genres;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.LinkedHashSet;
import java.util.List;

public interface GenresStorage {
    Genre getGenreById(Integer genreId);

    List<Genre> getAllGenres();

    void createGenre(Genre genre);

    public void setGenresInDB(Long filmID, LinkedHashSet<Genre> genres);
}