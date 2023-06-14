package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.error.exception.InvalidSearchParameters;
import ru.yandex.practicum.filmorate.error.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.EventTypes;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Operations;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
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
    private final DirectorService directorService;
    private final FeedStorage feedStorage;
    private static final String FILM_NOT_FOUND = "Фильм не найден № ";

    public FilmService(FilmStorage filmStorage, UserStorage userStorage, LikesStorage likesStorage, DirectorService directorService, FeedStorage feedStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.likesStorage = likesStorage;
        this.directorService = directorService;
        this.feedStorage = feedStorage;
    }

    public Film addFilm(Film film) {
        return filmStorage.add(film);
    }

    public Film updateFilm(Film film) {
        if (containsFilm(film.getId())) {
            return filmStorage.update(film).get();
        }
        log.info(FILM_NOT_FOUND + film.getId());
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    public Film getFilmById(Long id) {
        if (containsFilm(id)) {
            return filmStorage.getById(id).get();
        }
        log.info(FILM_NOT_FOUND + id);
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    public void deleteFilm(Long id) {
        if (containsFilm(id)) {
            filmStorage.delete(id);
        } else {
            log.info("Фильм " + id + " не найден");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getAll();
    }

    public void addLike(Long id, Long userId) {
        if (containsFilm(id)) {
            if (containsUser(userId)) {
                feedStorage.addEvent(userId, EventTypes.LIKE, Operations.ADD, id);
                likesStorage.addLike(id, userId);
            } else {
                log.info("Пользователь " + userId + " не найден");
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
        } else {
            log.info(FILM_NOT_FOUND + id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    public void deleteLike(Long id, Long userId) {
        if (containsFilm(id)) {
            if (containsUser(userId)) {
                feedStorage.addEvent(userId, EventTypes.LIKE, Operations.REMOVE, id);
                likesStorage.removeLike(id, userId);
            } else {
                log.info("Пользователь " + userId + " не найден");
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
        } else {
            log.info(FILM_NOT_FOUND + id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    public List<Film> getListPopularFilms(Integer count, Integer genreId, Integer year) {
        return likesStorage.getTopFilmLikes()
                .stream()
                .limit(count)
                .map(this::getFilmById)
                .collect(Collectors.toList());
    }

    public List<Film> getTopPopularFilms(Integer count, Integer genreId, Integer year) {
        return filmStorage.getPopularsFilms(count, genreId, year);
    }

    private boolean containsUser(Long id) {
        return userStorage.getUsersMap().containsKey(id);
    }

    private boolean containsFilm(Long id) {
        return filmStorage.getById(id).isPresent();
    }

    public List<Film> getDirectorFilms(Long id, String sortBy) {
        if (directorService.getFilmDirectorsById(id).isEmpty()) {
            throw new ObjectNotFoundException("Режиссер у фильма не указан ");
        }
        switch (sortBy) {
            case "year":
                List<Film> films = filmStorage.getFilmsSortedByYears(id);
                return films;
            case "likes":
                films = filmStorage.getFilmsSortedByLikes(id);
                return films;
            default:
                throw new ObjectNotFoundException("Задан не корректный параметр сортировки");
        }
    }

    public List<Film> getFriendsCommonFilms(Long userId, Long friendId) {
        return filmStorage.getCommonFilms(userId, friendId);
    }

    public List<Film> searchFilms(String query, String[] searchParameters) {
        if (query == null || searchParameters == null || searchParameters.length > 2) {
            throw new InvalidSearchParameters("В параметрах поиска ошибка.");
        }
        log.info("Service.searchFilms: {} - query, {} - by", query, searchParameters);
        List<Film> findFilms = filmStorage.searchFilms(query, searchParameters);
        log.info("Service.searchFilms: {} - Finished", findFilms);
        return findFilms;
    }
}


