package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.error.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorStorage directorStorage;

    public Director getDirectorById(int id) {
        Optional<Director> director = Optional.ofNullable(directorStorage.getDirectorById(id));
        if (director.isPresent()) {
            log.debug("Получить режиссера", director.get());
            return director.get();
        } else {
            throw new ObjectNotFoundException("Режиссер" + id + " не найден");
        }
    }

    public List<Director> getFilmDirectorsById(int id) {
        return directorStorage.getDirectorsByFilmId(id);
  }

    public Director createDirector(Director director) {
        int id = directorStorage.createDirector(director);
        return getDirectorById(id);
    }

    public Director updateDirector(Director director) {
        Director loadedDirector = getDirectorById(director.getId());
        loadedDirector.setName(director.getName());
        directorStorage.updateDirector(loadedDirector);
        return getDirectorById(director.getId());
    }

    public void deleteDirector(int id) {
        directorStorage.deleteDirector(id);
    }

    public List<Director> getAllDirectors() {
        List<Director> directors = directorStorage.getAllDirectors();
        log.debug("Получить список режиссеров" , directors.size());
        return directors;
    }
}