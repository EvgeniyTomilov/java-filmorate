package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

public class FilmRepository {
    private long generatorId = 1000;

    public long getGeneratorId (){
        return  ++generatorId;
    }

    public void save (Film film){
        film.setId(getGeneratorId());
    }
}

