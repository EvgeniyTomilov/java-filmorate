package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface DirectorStorage {

    Director getDirectorById(Long directorId);

    List<Director> getAllDirectors();

    Long createDirector(Director director);

    void updateDirector(Director director);

    void deleteDirector(Long id);

    List<Director> getDirectorsByFilmId(Long id);

    void setDirectorInDB(Long filmID, List<Director> directors);

    void setDirectorsListFilmsDB(List<Film> films);

    List<Director> findDirectorByFilm(Long filmID);
}
