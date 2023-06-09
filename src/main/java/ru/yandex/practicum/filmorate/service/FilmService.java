package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.likes.LikesStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {

    @Autowired
    @Qualifier(value = "filmDbStorage")
    private FilmStorage filmStorage;
    @Autowired
    @Qualifier(value = "userDbStorage")
    private UserStorage userStorage;
    private final LikesStorage likesStorage;

    public FilmService(LikesStorage likesStorage) {
        this.likesStorage = likesStorage;
    }

    public Film addFilm(Film film) {
        return filmStorage.add(film);
    }

    public Film updateFilm(Film film) {
        if (containsFilm(film.getId())) {
            return filmStorage.update(film).get();
        }
        log.info("Фильм " + film.getId() + " не найден");
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    public Film getFilmById(Long id) {
        if (containsFilm(id)) {
            return filmStorage.getById(id).get();
        }
        log.info("Фильм " + id + " не найден");
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    public void deleteFilm(Long id) {
        if (containsFilm(id)) {
            filmStorage.delete(id);
        }
        log.info("Фильм " + id + " не найден");
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getAll();
    }

    public void addLike(Long id, Long userId) {
        if (containsFilm(id)) {
            if (containsUser(userId)) {
                likesStorage.addLike(id, userId);
            } else {
                log.info("Пользователь " + userId + " не найден");
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
        } else {
            log.info("Фильм " + id + " не найден");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    public void deleteLike(Long id, Long userId) {
        if (containsFilm(id)) {
            if (containsUser(userId)) {
                likesStorage.removeLike(id, userId);
            } else {
                log.info("Пользователь " + userId + " не найден");
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
        } else {
            log.info("Фильм " + id + " не найден");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    public List<Film> getListPopularFilms(Integer count) {
        return likesStorage.getTopFilmLikes()
                .stream()
                .limit(count)
                .map(this::getFilmById)
                .collect(Collectors.toList());
    }

    private boolean containsUser(Long id) {
        return userStorage.getUsersMap().containsKey(id);
    }

    private boolean containsFilm(Long id) {
        return filmStorage.getById(id).isPresent();
    }
}