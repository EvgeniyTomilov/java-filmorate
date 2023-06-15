package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
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
        Long reviewId = rs.getLong("REVIEW_ID");
        Review review = Review.builder()
                .filmId(rs.getLong("FILM_ID"))
                .userId(rs.getLong("USER_ID"))
                .content(rs.getString("CONTENT"))
                .isPositive(rs.getBoolean("IS_POSITIVE"))
                .useful(rs.getInt("USEFUL"))
                .build();
        review.setReviewId(reviewId);
        return review;
    }

    @Override
    public Optional<Review> getById(Long id) {
        String query = "SELECT REVIEWS.REVIEW_ID, REVIEWS.FILM_ID, REVIEWS.USER_ID, REVIEWS.CONTENT, " +
                "REVIEWS.IS_POSITIVE, COALESCE(SUM(REVIEW_LIKES.ISLIKE),0) AS USEFUL " +
                "FROM REVIEWS " +
                "LEFT JOIN REVIEW_LIKES " +
                "ON REVIEWS.REVIEW_ID = REVIEW_LIKES.REVIEW_ID " +
                "WHERE REVIEWS.REVIEW_ID = ? " +
                "GROUP BY REVIEWS.REVIEW_ID";
        Optional<Review> reviews = null;
        try {
            reviews = Optional.of(jdbcTemplate.queryForObject(query, (rs, rowNum) -> rowMapReview(rs), id));
            log.info("В базе данных найден отзыв: {}", reviews.get());
            return reviews;
        } catch (EmptyResultDataAccessException e) {
            log.info("Отзыва с идентификатором {} нет.", id);
        }
        return Optional.empty();
    }


    @Override
    public Review add(Review review) {
        String reviewSqlQuery =
                "INSERT INTO REVIEWS (FILM_ID, USER_ID, CONTENT, IS_POSITIVE) " +
                        "VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int updatedRowsCount = jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(reviewSqlQuery, new String[]{"REVIEW_ID"});
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
                "UPDATE REVIEWS " +
                        "SET CONTENT = ?, IS_POSITIVE = ? " +
                        "WHERE REVIEW_ID = ?";
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
        String sqlQueryDelete = "DELETE FROM REVIEWS WHERE REVIEW_ID = ?";
        jdbcTemplate.update(sqlQueryDelete, id);
    }

    @Override
    public List<Review> getListReviewsFilm(Long filmId, int count) {
        List<Review> reviews;
        if (filmId == 0) {
            String reviewSqlQuery = "SELECT * FROM (SELECT REVIEWS.REVIEW_ID, REVIEWS.FILM_ID, REVIEWS.USER_ID, " +
                    "REVIEWS.CONTENT, REVIEWS.IS_POSITIVE, COALESCE(SUM(REVIEW_LIKES.ISLIKE),0) AS USEFUL" +
                    " FROM REVIEWS LEFT OUTER JOIN REVIEW_LIKES ON REVIEWS.REVIEW_ID = REVIEW_LIKES.REVIEW_ID " +
                    "GROUP BY REVIEWS.REVIEW_ID, REVIEWS.FILM_ID, REVIEWS.USER_ID, REVIEWS.CONTENT, REVIEWS.IS_POSITIVE) " +
                    "ORDER BY USEFUL DESC LIMIT ?";
            reviews = jdbcTemplate.query(reviewSqlQuery, (rs, rowNum) -> rowMapReview(rs), count);
        } else {
            String reviewSqlQuery = "SELECT * FROM (SELECT REVIEWS.REVIEW_ID, REVIEWS.FILM_ID, REVIEWS.USER_ID, " +
                    "REVIEWS.CONTENT, REVIEWS.IS_POSITIVE, COALESCE(SUM(REVIEW_LIKES.ISLIKE),0) AS USEFUL" +
                    " FROM REVIEWS LEFT OUTER JOIN REVIEW_LIKES ON REVIEWS.REVIEW_ID = REVIEW_LIKES.REVIEW_ID WHERE REVIEWS.FILM_ID = ? " +
                    " GROUP BY REVIEWS.REVIEW_ID, REVIEWS.FILM_ID, REVIEWS.USER_ID, REVIEWS.CONTENT, REVIEWS.IS_POSITIVE) " +
                    "ORDER BY USEFUL DESC LIMIT ?";
            reviews = jdbcTemplate.query(reviewSqlQuery, (rs, rowNum) -> rowMapReview(rs), filmId, count);
        }
        return reviews;
    }

    @Override
    public void addLike(Long id, Long userId) {
        String reviewSqlQuery =
                "INSERT INTO REVIEW_LIKES (REVIEW_ID, USER_ID, ISLIKE) " +
                        "VALUES (?, ?, 1)";
        jdbcTemplate.update(reviewSqlQuery, id, userId);
    }

    @Override
    public void addDislike(Long id, Long userId) {
        String reviewSqlQuery =
                "INSERT INTO REVIEW_LIKES (REVIEW_ID, USER_ID, ISLIKE) " +
                        "VALUES (?, ?, -1)";
        jdbcTemplate.update(reviewSqlQuery, id, userId);
    }

    @Override
    public void deleteLike(Long id, Long userId) {
        String reviewSqlQuery = "DELETE FROM REVIEW_LIKES WHERE REVIEW_ID = ? AND USER_ID = ? AND ISLIKE = 1";
        jdbcTemplate.update(reviewSqlQuery, id, userId);
    }

    @Override
    public void deleteDislike(Long id, Long userId) {
        String reviewSqlQuery = "DELETE FROM REVIEW_LIKES WHERE REVIEW_ID = ? AND USER_ID = ? AND ISLIKE = -1";
        jdbcTemplate.update(reviewSqlQuery, id, userId);
    }
}

