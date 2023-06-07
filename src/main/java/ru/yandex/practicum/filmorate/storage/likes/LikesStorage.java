package ru.yandex.practicum.filmorate.storage.likes;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Set;

public interface LikesStorage {
    Integer getAmountOfLikes(Long filmId, Long userId);

    Set<Long> getTopFilmLikes();

    Set<Integer> getLikesByFilmId(Long filmId);

    void removeLike(Long idFilm, Long delIdUser);

    void addLike(Long id, Long userId);

    //вывод популярного фильма по годам и режиссеру
    List<Film> getPopularsFilms(Integer count, Integer genreId, Integer year);


}
