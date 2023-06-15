package ru.yandex.practicum.filmorate.storage.director;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.error.exception.NullException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component("DirectorDbStorage")
@Slf4j
public class DirectorDbStorage implements DirectorStorage {

    private final JdbcTemplate jdbcTemplate;

    public DirectorDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private Director rowMapToDirector(ResultSet resultSet, int i) throws SQLException {
        return Director.builder()
                .id(resultSet.getLong("id"))
                .name(resultSet.getString("name"))
                .build();
    }

    @Override
    public Director getDirectorById(Long directorId) {
        if (directorId == null) {
            return null;
        }
        Director director = null;
        String sqlQueryGetById = "SELECT * FROM DIRECTORS WHERE ID = ?;";
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
    public Long createDirector(Director director) {
        String sqlQuery = "INSERT INTO DIRECTORS (NAME) VALUES (?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sqlQuery, new String[]{"id"});
            statement.setString(1, director.getName());
            return statement;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    @Override
    public void updateDirector(Director director) {
        Long id = director.getId();
        checkExist(id);
        String sqlQuery = "UPDATE DIRECTORS SET NAME = ? WHERE ID = ?;";
        jdbcTemplate.update(sqlQuery, director.getName(), director.getId());
    }

    @Override
    public void deleteDirector(Long id) {
        checkExist(id);
        String sqlQuery = "DELETE FROM DIRECTORS WHERE ID = ?;";
        jdbcTemplate.update(sqlQuery, id);
    }

    public void checkExist(Long directorId) {
        final String checkUserQuery = "SELECT * FROM DIRECTORS WHERE ID = ?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(checkUserQuery, directorId);
        if (!userRows.next()) {
            log.warn("Режиссер с идентификатором {} не найден.", directorId);
            throw new NullException("Режисер с идентификатором " + directorId + " не найден.");
        }
    }

    @Override
    public List<Director> getDirectorsByFilmId(Long id) {
        String sqlQuery =
                "SELECT * FROM DIRECTORS d " +
                        "JOIN FILMS_DIRECTORS F ON F.DIRECTOR_ID = D.ID " +
                        "WHERE F.FILM_ID = ?;";
        return jdbcTemplate.query(sqlQuery, this::rowMapToDirector, id);
    }

    @Override
    public void setDirectorInDB(Long filmID, List<Director> directors) {
        String sqlQuery3 = "DELETE FROM FILMS_DIRECTORS WHERE FILM_ID =?";
        jdbcTemplate.update(sqlQuery3, filmID);
        if (!directors.isEmpty()) {
            Set<Director> directorsSet = new HashSet<>(directors);
            List<Director> directorsWODouble = new ArrayList<>(directorsSet);
            jdbcTemplate.batchUpdate("INSERT INTO FILMS_DIRECTORS (FILM_ID, DIRECTOR_ID) VALUES(?, ?)", new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                    preparedStatement.setString(1, String.valueOf(filmID));
                    preparedStatement.setString(2, String.valueOf(directorsWODouble.get(i).getId()));
                }

                @Override
                public int getBatchSize() {
                    return directorsWODouble.size();
                }
            });
        }
    }

    @Override
    public void setDirectorsListFilmsDB(List<Film> films) {
        List<Long> listID = new ArrayList<>();
        for (Film film : films) {
            listID.add(film.getId());
            if (film.getDirectors() == null) {
                film.setDirectors(new ArrayList<>());
            }
        }
        String sep = ",";
        String str = listID.stream().map(Object::toString)
                .collect(Collectors.joining(sep));
        String sqlQueryFilmDirectors = "SELECT FILMS_DIRECTORS.FILM_ID, DIRECTORS.ID, " +
                "DIRECTORS.NAME FROM FILMS_DIRECTORS, " +
                "DIRECTORS WHERE FILMS_DIRECTORS.DIRECTOR_ID = DIRECTORS.ID AND FILMS_DIRECTORS.FILM_ID IN " +
                "(" + str + ") ORDER BY FILMS_DIRECTORS.FILM_ID";
        Map<Long, Film> mapedFilms = films.stream()
                .collect(Collectors.toMap(Film::getId,
                        Function.identity()));

        List<Map<String, Object>> direcotrsList = jdbcTemplate.queryForList(sqlQueryFilmDirectors);

        for (Map<String, Object> t : direcotrsList) {
            Long l = Long.parseLong(String.valueOf(t.get("film_id")));
            Film film = mapedFilms.get(l);
            Director director = Director.builder()
                    .id(Long.parseLong(String.valueOf(t.get("id"))))
                    .name(t.get("name").toString())
                    .build();
            film.getDirectors().add(director);
        }

    }

    @Override
    public List<Director> findDirectorByFilm(Long filmID) {
        String sqlQuery = "SELECT * FROM DIRECTORS AS D " +
                "INNER JOIN FILMS_DIRECTORS AS FD ON FD.DIRECTOR_ID = D.ID WHERE FD.FILM_ID = ?";
        return jdbcTemplate.query(sqlQuery, this::rowMapToDirector, filmID);
    }
}
