package ru.yandex.practicum.filmorate.storage.director;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.error.exception.NullException;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Component("DirectorDbStorage")
@Slf4j
public class DirectorDbStorage implements DirectorStorage {

    private final JdbcTemplate jdbcTemplate;

    public DirectorDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private Director rowMapToDirector(ResultSet resultSet, int i) throws SQLException {
        return Director.builder()
                .id(resultSet.getInt("id"))
                .name(resultSet.getString("name"))
                .build();
    }

    @Override
    public Director getDirectorById(Integer directorId) {
        if (directorId == null) {
            return null;
        }
        Director director = null;
        String sqlQueryGetById = "SELECT * FROM DIRECTORS WHERE id = ?;";
        try {
            director = jdbcTemplate.queryForObject(sqlQueryGetById, this::rowMapToDirector, directorId);
        } catch (EmptyResultDataAccessException e) {
            log.info("В базе нет информации по запросу  {}.  id={}", sqlQueryGetById, directorId);
        }
        return director;
    }

    @Override
    public List<Director> getAllDirectors() {
        String sqlQuery = "SELECT * FROM DIRECTORS;";
        log.info("Cписок режиссеров");
        return jdbcTemplate.query(sqlQuery, this::rowMapToDirector);
    }

    @Override
    public int createDirector(Director director) {
        String sqlQuery = "INSERT INTO DIRECTORS (name) VALUES (?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sqlQuery, new String[]{"id"});
            statement.setString(1, director.getName());
            return statement;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    @Override
    public void updateDirector(Director director) {
        int id = director.getId();
        isExist(id);
        String sqlQuery = "UPDATE DIRECTORS SET name = ? WHERE id = ?;";
        jdbcTemplate.update(sqlQuery, director.getName(), director.getId());
    }

    @Override
    public void deleteDirector(int id) {
        isExist(id);
        String sqlQuery = "DELETE FROM directors WHERE id = ?;";
        jdbcTemplate.update(sqlQuery, id);
    }

    public void isExist(int directorId) {
        final String checkUserQuery = "SELECT * FROM directors WHERE id = ?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(checkUserQuery, directorId);
        if (!userRows.next()) {
            log.warn("Режиссер с идентификатором {} не найден.", directorId);
            throw new NullException("Режисер с идентификатором " + directorId + " не найден.");
        }
    }

    @Override
    public List<Director> getDirectorsByFilmId(int id) {
        String sqlQuery =
                "SELECT * FROM DIRECTORS d " +
                        "JOIN FILMS_DIRECTORS f ON f.director_id = d.id " +
                        "WHERE f.film_id = ?;";
       return jdbcTemplate.query(sqlQuery, this::rowMapToDirector, id);
    }
}


