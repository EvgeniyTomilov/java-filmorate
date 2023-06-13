package ru.yandex.practicum.filmorate.storage.film;

//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ResponseStatusException;
//import ru.yandex.practicum.filmorate.model.Film;
//
//import javax.validation.Valid;
//import java.util.*;
//
//@Component
//@Slf4j
//public class InMemoryFilmStorage implements FilmStorage {
//    private final HashMap<Long, Film> filmHashMap = new HashMap<>();
//    private Long id = 0L;
//
//
//    @Override
//    public Film add(@Valid Film film) {
//        if (film != null) {
//            film.setId(++id);
//            filmHashMap.put(film.getId(), film);
//            log.info("Фильм добавлен");
//            return film;
//        }
//        log.info("Объект фильм был null");
//        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
//    }
//
//    @Override
//    public Optional<Film> update(@Valid Film film) {
//        if (film != null) {
//            if (filmHashMap.containsKey(film.getId())) {
//                filmHashMap.put(film.getId(), film);
//                log.info("Фильм обновлен");
//                return Optional.of(film);
//            }
//            log.info("Фильм " + film.getId() + " не найден");
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
//        }
//        log.info("Объект пользователь был null");
//        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
//    }
//
//    @Override
//    public Optional<Film> getById(Long id) {
//        if (filmHashMap.containsKey(id)) {
//            return Optional.of(filmHashMap.get(id));
//        }
//        log.info("Фильм " + id + " не найден");
//        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
//    }
//
//    @Override
//    public void delete(Long id) {
//        if (filmHashMap.containsKey(id)) {
//            filmHashMap.remove(id);
//        } else {
//            log.info("Фильм " + id + " не найден");
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
//        }
//    }
//
//    @Override
//    public Collection<Film> getAll() {
//        return new ArrayList<>(filmHashMap.values());
//    }
//
//
//    public Boolean contains(Long id) {
//        return filmHashMap.containsKey(id);
//    }
//
//
//    @Override
//    public List<Film> getPopularsFilms(Integer count, Integer genreId, Integer year) {
//        return null;
//    }
//
//    @Override
//    public List<Film> getFilmsSortedByYears(Long directorId) {
//        return null;
//    }
//
//    @Override
//    public List<Film> getFilmsSortedByLikes(Long directorId) {
//        return null;
//    }
//
//    @Override
//    public List<Film> getCommonFilms(Long userId, Long friendId) {
//        return null;
//    }
//
//
//    @Override
//    public List<Film> searchFilms(String query, String[] searchParameters) {
//        return null;
//    }
//
//
//
//}

