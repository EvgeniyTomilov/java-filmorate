package ru.yandex.practicum.filmorate.storage.likes;

import java.util.Set;

public interface LikesStorage {
    Integer getAmountOfLikes(Long filmId, Long userId);

    Set<Long> getTopFilmLikes();

    void removeLike(Long idFilm, Long delIdUser);

    void addLike(Long id, Long userId);

    Set<Integer> getLikesByFilmId(Long filmId);
}
