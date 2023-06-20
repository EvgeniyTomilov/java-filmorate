package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public Review addReview(@Valid @RequestBody Review review) {
        log.info("Добавление отзыва");
        return reviewService.addReview(review);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        log.info("Обновление отзыва " + review.getReviewId() + "...");
        return reviewService.updateReview(review);
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable Long id) {
        log.info("Вызов отзыва по id:" + id + "...");
        return reviewService.getReviewById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Long id) {
        log.info("Удаление отзыва " + id + "...");
        reviewService.deleteReview(id);
        log.info("Отзыв удален");
    }

    @GetMapping
    public List<Review> getListReviewsFilm(@RequestParam(value = "filmId", defaultValue = "0") Long filmId,
                                           @RequestParam(value = "count", defaultValue = "10") Integer count) {
        log.info("Вызов отзывов для фильмов...");
        return reviewService.getListReviewsFilm(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Добавление лайка пользователем " + userId + " отзыву " + id + "...");
        reviewService.addLike(id, userId);
        log.info("Лайк добавлен");
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Добавление дизлайка пользователем " + userId + " отзыву " + id + "...");
        reviewService.addDislike(id, userId);
        log.info("Дизлайк добавлен");
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Удаление лайка пользователем " + userId + " отзыву " + id + "...");
        reviewService.deleteLike(id, userId);
        log.info("Лайк удален");
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Удаление дизлайка пользователем " + userId + " отзыву " + id + "...");
        reviewService.deleteDislike(id, userId);
        log.info("Дизлайк удален");
    }
}
