package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component("reviewDbStorage")
@Slf4j
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;

    private Review rowMapReview(ResultSet rs) throws SQLException {
        Long reviewId = rs.getLong("review_id");
        Review review = Review.builder()
                .filmId(rs.getLong("film_id"))
                .userId(rs.getLong("user_id"))
                .content(rs.getString("content"))
                .isPositive(rs.getBoolean("is_positive"))
                .useful(rs.getInt("USEFUL"))
                .build();
        review.setReviewId(reviewId);
        return review;
    }

    @Override
    public Optional<Review> getById(Long id) {
        String query = "SELECT REVIEWS.REVIEW_ID, REVIEWS.FILM_ID, REVIEWS.USER_ID, REVIEWS.CONTENT, REVIEWS.IS_POSITIVE, coalesce(SUM(REVIEW_LIKES.ISLIKE),0) as USEFUL FROM REVIEWS LEFT JOIN REVIEW_LIKES on REVIEWS.REVIEW_ID = REVIEW_LIKES.REVIEW_ID WHERE REVIEWS.REVIEW_ID = ? group by REVIEWS.REVIEW_ID";
        List<Review> reviews = jdbcTemplate.query(query, (rs, rowNum) -> rowMapReview(rs), id);
        if (reviews.isEmpty()) {
            log.info("Отзыва с идентификатором {} нет.", id);
            return Optional.empty();
        }
        log.info("В базе данных найден отзыв: {}", reviews.get(0));
        return Optional.of(reviews.get(0));
    }

    @Override
    public Review add(Review review) {
        String reviewSqlQuery =
                "INSERT INTO reviews (film_id, user_id, content, is_positive) " +
                        "VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int updatedRowsCount = jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(reviewSqlQuery, new String[]{"review_id"});
            stmt.setLong(1, review.getFilmId());
            stmt.setLong(2, review.getUserId());
            stmt.setString(3, review.getContent());
            stmt.setBoolean(4, review.getIsPositive());
            return stmt;
        }, keyHolder);

        if (updatedRowsCount == 0) {
            log.info("При добавлении отзыва {} в базу данных произошла ошибка", review);
            return null;
        }
        Long reviewId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        Review createdReview = getById(reviewId).orElse(null);
        log.info("Отзыв {} добавлен в базу данных", createdReview);
        return createdReview;
    }

    @Override
    public Optional<Review> update(Review review) {
        String reviewSqlQuery =
                "UPDATE reviews " +
                        "SET content = ?, is_positive = ? " +
                        "WHERE review_id = ?";
        int updatedRowsCount = jdbcTemplate.update(reviewSqlQuery,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId());

        if (updatedRowsCount == 0) {
            log.info("Отзыва с идентификатором {} нет.", review.getReviewId());
            return Optional.empty();
        }
        Optional<Review> updatedReview = this.getById(review.getReviewId());
        log.info("Отзыв {} обновлен в базе данных", updatedReview);
        return updatedReview;
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            return;
        }
        String sqlQueryDelete = "DELETE FROM REVIEWS WHERE review_id = ?";
        jdbcTemplate.update(sqlQueryDelete, id);
    }

    @Override
    public List<Review> getListReviewsFilm(Long filmId, int count) {
        List<Review> reviews;
        if (filmId == 0) {
            String reviewSqlQuery = "SELECT * FROM (SELECT REVIEWS.REVIEW_ID, REVIEWS.FILM_ID, REVIEWS.USER_ID, REVIEWS.CONTENT, REVIEWS.IS_POSITIVE, coalesce(SUM(REVIEW_LIKES.ISLIKE),0) as USEFUL" +
                    " FROM REVIEWS left outer join REVIEW_LIKES on REVIEWS.REVIEW_ID = REVIEW_LIKES.REVIEW_ID " +
                    "group by REVIEWS.REVIEW_ID, REVIEWS.FILM_ID, REVIEWS.USER_ID, REVIEWS.CONTENT, REVIEWS.IS_POSITIVE) " +
                    "ORDER BY USEFUL DESC limit ?";
            reviews = jdbcTemplate.query(reviewSqlQuery, (rs, rowNum) -> rowMapReview(rs), count);
        } else {
            String reviewSqlQuery = "SELECT * FROM (SELECT REVIEWS.REVIEW_ID, REVIEWS.FILM_ID, REVIEWS.USER_ID, REVIEWS.CONTENT, REVIEWS.IS_POSITIVE, coalesce(SUM(REVIEW_LIKES.ISLIKE),0) as USEFUL" +
                    " FROM REVIEWS left outer join REVIEW_LIKES on REVIEWS.REVIEW_ID = REVIEW_LIKES.REVIEW_ID WHERE REVIEWS.film_ID = ? " +
                    " group by REVIEWS.REVIEW_ID, REVIEWS.FILM_ID, REVIEWS.USER_ID, REVIEWS.CONTENT, REVIEWS.IS_POSITIVE) " +
                    "ORDER BY USEFUL DESC limit ?";
            reviews = jdbcTemplate.query(reviewSqlQuery, (rs, rowNum) -> rowMapReview(rs), filmId, count);
        }
        return reviews;
    }

    @Override
    public void addLike(Long id, Long userId) {
        String reviewSqlQuery =
                "INSERT INTO review_likes (review_id, user_id, islike) " +
                        "VALUES (?, ?, 1)";
        jdbcTemplate.update(reviewSqlQuery, id, userId);
    }

    @Override
    public void addDislike(Long id, Long userId) {
        String reviewSqlQuery =
                "INSERT INTO review_likes (review_id, user_id, islike) " +
                        "VALUES (?, ?, -1)";
        jdbcTemplate.update(reviewSqlQuery, id, userId);
    }

    @Override
    public void deleteLike(Long id, Long userId) {
        String reviewSqlQuery = "DELETE FROM REVIEW_LIKES WHERE review_id = ? and user_id = ? and islike = 1";
        jdbcTemplate.update(reviewSqlQuery, id, userId);
    }

    @Override
    public void deleteDislike(Long id, Long userId) {
        String reviewSqlQuery = "DELETE FROM REVIEW_LIKES WHERE review_id = ? and user_id = ? and islike = -1";
        jdbcTemplate.update(reviewSqlQuery, id, userId);
    }

}

