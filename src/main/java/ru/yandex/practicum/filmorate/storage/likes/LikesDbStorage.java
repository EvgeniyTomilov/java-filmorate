package ru.yandex.practicum.filmorate.storage.likes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component("LikesDbStorage")
@RequiredArgsConstructor
public class LikesDbStorage implements LikesStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Set<Integer> getLikesByFilmId(Long filmId) {
        String sqlQuery =
                "SELECT USERID " +
                        "FROM FILMLIKES " +
                        "WHERE FILMID = ?";
        List<Integer> likes = jdbcTemplate.queryForList(sqlQuery, Integer.class, filmId);
        return new HashSet<>(likes);
    }

    @Override
    public void removeLike(Long idFilm, Long userId) {
        String sqlQueryRemoveLike = "DELETE FROM FILMLIKES WHERE FILMID = ? AND USERID = ?";
        jdbcTemplate.update(sqlQueryRemoveLike,
                idFilm,
                userId);
    }

    @Override
    public void addLike(Long id, Long userId) {
        String sqlQueryAdd = "MERGE INTO FILMLIKES(FILMID,USERID)" +
                " VALUES(?,?)";
        jdbcTemplate.update(sqlQueryAdd,
                id,
                userId);
    }

    @Override
    public Integer getAmountOfLikes(Long filmId, Long userId) {
        int amount = 0;
        String sqlAmountOfLikes = "SELECT COUNT(*) " +
                "FROM FILMLIKES WHERE FILMID=? AND USERID = ?";
        try {
            amount = jdbcTemplate.queryForObject(sqlAmountOfLikes, Integer.class, filmId, userId);
        } catch (EmptyResultDataAccessException e) {
            log.info("В базе нет информации по запросу {}. filmId={}, userId={}",
                    sqlAmountOfLikes, filmId, userId);
        }
        return amount;
    }

    @Override
    public Set<Long> getTopFilmLikes() {
        String sqlQueryTopFilmLikes =
                "SELECT T.ID FROM FILMS T LEFT JOIN " +
                        "(SELECT FILMID AS ID, COUNT(USERID) AS COUNT FROM FILMLIKES GROUP BY FILMID) AS CN ON T.ID = CN.ID " +
                        "ORDER BY (COALESCE(T.RATE, 0) + COALESCE(CN.COUNT, 0)) DESC";
        return new LinkedHashSet<>(jdbcTemplate.queryForList(sqlQueryTopFilmLikes, Long.class));
    }
}