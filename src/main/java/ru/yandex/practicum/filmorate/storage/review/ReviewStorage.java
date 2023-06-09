package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {

    Optional<Review> getById(Long id);

    Review add(Review review);

    Optional<Review> update(Review review);

    void delete(Long id);

    List<Review> getListReviewsFilm(Long filmId, int count);

    void addLike(Long id, Long userId);

    void addDislike(Long id, Long userId);

    void deleteLike(Long id, Long userId);

    void deleteDislike(Long id, Long userId);
}
