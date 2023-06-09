package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

public interface DirectorStorage {

    Director getDirectorById(Integer directorId);

    List<Director> getAllDirectors();

    int createDirector(Director director);

    void updateDirector(Director director);

    void deleteDirector(int id);

    List<Director> getDirectorsByFilmId(int id);

}
