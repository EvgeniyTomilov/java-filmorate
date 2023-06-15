package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.EventTypes;
import ru.yandex.practicum.filmorate.model.Operations;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service("ReviewService")
@Slf4j
public class ReviewService {

    @Autowired
    @Qualifier(value = "filmDbStorage")
    private FilmStorage filmStorage;
    @Autowired
    @Qualifier(value = "userDbStorage")
    private UserStorage userStorage;
    @Autowired
    private FeedStorage feedStorage;
    @Autowired
    private final ReviewStorage reviewStorage;


    public ReviewService(@Qualifier("reviewDbStorage") ReviewStorage reviewStorage) {
                this.reviewStorage = reviewStorage;
            }

    public Review getReviewById(Long reviewId) {
        if (containsReview(reviewId)) {
            return reviewStorage.getById(reviewId).get();
        }
        log.info("Отзыв " + reviewId + " не найден");
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    public Review addReview(Review review) {

        if (review.getFilmId() == null || review.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (!containsFilm(review.getFilmId()) || !containsUser(review.getUserId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Review reviewDb = reviewStorage.add(review);
        feedStorage.addEvent(reviewDb.getUserId(), EventTypes.REVIEW, Operations.ADD, reviewDb.getReviewId());
        return reviewDb;
    }

    public Review updateReview(Review review) {
        if (containsReview(review.getReviewId())) {
            Review reviewDb = reviewStorage.update(review).get();
            feedStorage.addEvent(reviewDb.getUserId(), EventTypes.REVIEW, Operations.UPDATE, reviewDb.getReviewId());
            return reviewDb;
        }
        log.info("Отзыв " + review.getReviewId() + " не найден");
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    public void deleteReview(Long id) {
        if (!containsReview(id)) {
            log.info("Отзыв " + id + " не найден");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Long userId = getReviewById(id).getUserId();
        feedStorage.addEvent(userId, EventTypes.REVIEW, Operations.REMOVE, id);
        reviewStorage.delete(id);
    }

    public List<Review> getListReviewsFilm(Long filmId, int count) {
        return reviewStorage.getListReviewsFilm(filmId, count);
    }

    public void addLike(Long id, Long userId) {
        if (containsUser(userId) && containsReview(id)) {
            reviewStorage.addLike(id, userId);
        }
    }

    public void addDislike(Long id, Long userId) {
        if (containsUser(userId) && containsReview(id)) {
            reviewStorage.addDislike(id, userId);
        }
    }

    public void deleteLike(Long id, Long userId) {
        reviewStorage.deleteLike(id, userId);
    }

    public void deleteDislike(Long id, Long userId) {
        reviewStorage.deleteDislike(id, userId);
    }

    private boolean containsReview(Long id) {
        return reviewStorage.getById(id).isPresent();
    }

    private boolean containsUser(Long id) {
        return userStorage.getUsersMap().containsKey(id);
    }

    private boolean containsFilm(Long id) {
        return filmStorage.getById(id).isPresent();
    }
}
