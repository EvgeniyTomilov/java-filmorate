package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;

    public Film save(Film film) {
        return filmStorage.save(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film addLike(Long id, Long userId) {
        Film film = filmStorage.getFilmById(id);
        ValidateService.validateId(id);
        ValidateService.validateId(userId);
        Set<Long> like = film.getLike();

        if (userId == null) {
            throw new UserNotFoundException(String.format("Пользователь %s не найден", userId));
        }
        like.add(userId);
        return filmStorage.update(film);
    }

    public Film deleteLikeByUserId(Long id, Long userId) {
        Film film = filmStorage.getFilmById(id);
        ValidateService.validateId(id);
        ValidateService.validateId(userId);
        Set<Long> like = film.getLike();
        like.remove(userId);
        return filmStorage.update(film);
    }

    public Film getFilmById(Long id) {
        return filmStorage.getFilmById(id);
    }

    public List<Film> getTopByLikes(Long count) {

        List<Film> films = filmStorage.getFilms();
        Comparator<Film> comparator = (film1, film2) -> {
            if (film1.getLike().size() > film2.getLike().size()) {
                return -1;
            } else if (film1.getLike().size() == film2.getLike().size()) {
                return 0;
            }
            return 1;
        };
        return films.stream()
                .sorted(comparator)
                .limit(count)
                .collect(Collectors.toList());
    }

}
