package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface Storage<T> {

    T add(T obj);

    Optional<T> update(T obj);

    Optional<T> getById(Long id);

    void delete(Long id);

    Collection<T> getAll();

}
