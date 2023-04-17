package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class FilmRepository {
    private long generatorId;
    private Map<Long, Film> films = new HashMap();

    public long generateId (){
        return  ++generatorId;
    }

    public Film save(Film film){
        film.setId(generateId());
        films.put(film.getId(), film);
        return film;
    }

    public Film update (Film film) {
        if (films.containsKey(film.getId())) {
            films.replace(film.getId(), film);
            log.info("update film: {}", film);
        }else {
                throw new RuntimeException();
            }
       return film;
    }

    public List<Film> getFilms() {
        return new ArrayList<Film>(films.values());
    }
}

