package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private long generatorId;
    private Map<Long, Film> films = new HashMap();

    @Override
    public long generateId() {
        return ++generatorId;
    }

    @Override
    public Film save(Film film) {
        film.setId(generateId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (films.containsKey(film.getId())) {
            films.replace(film.getId(), film);
            log.info("update film: {}", film);
        } else {
            throw new RuntimeException();
        }
        return film;
    }

    @Override
    public List<Film> getFilms() {
        return new ArrayList<Film>(films.values());
    }
}

