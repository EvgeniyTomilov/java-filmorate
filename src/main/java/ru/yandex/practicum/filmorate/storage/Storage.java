package ru.yandex.practicum.filmorate.storage;

import java.util.Collection;
import java.util.Optional;

public interface Storage<T> {

    T add(T obj);

    Optional<T> update(T obj);

    Optional<T> getById(Long id);

    void delete(Long id);

    Collection<T> getAll();
}
