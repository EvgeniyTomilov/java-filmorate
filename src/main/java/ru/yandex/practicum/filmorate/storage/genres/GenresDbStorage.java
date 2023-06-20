package ru.yandex.practicum.filmorate.storage.genres;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component("GenresDbStorage")
@Slf4j
public class GenresDbStorage implements GenresStorage {
    private final JdbcTemplate jdbcTemplate;

    public GenresDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private Genre rowMapToGenre(ResultSet resultSet, int i) throws SQLException {
        return Genre.builder()
                .id(resultSet.getInt("GENREID"))
                .name(resultSet.getString("GENRE"))
                .build();
    }

    @Override
    public Genre getGenreById(Integer genreId) {
        if (genreId == null) {
            return null;
        }
        Genre genre = null;
        String sqlQueryGetById = "SELECT * FROM GENRENAMES WHERE GENREID=?";
        try {
            genre = jdbcTemplate.queryForObject(sqlQueryGetById, this::rowMapToGenre, genreId);
        } catch (EmptyResultDataAccessException e) {
            log.info("В базе нет информации по запросу  {}.  id={}", sqlQueryGetById, genreId);
        }
        return genre;
    }

    @Override
    public List<Genre> getAllGenres() {
        List<Genre> genres = new ArrayList<>();
        String sqlQueryGetAllGenres = "SELECT * FROM GENRENAMES";
        try {
            genres = jdbcTemplate.query(sqlQueryGetAllGenres, this::rowMapToGenre);
        } catch (EmptyResultDataAccessException e) {
            log.info("В базе нет информации по запросу {}", sqlQueryGetAllGenres);

        }
        return genres;
    }

    @Override
    public void createGenre(Genre genre) {
        String sqlQueryCreateGenre = "INSERT INTO GENRENAMES(GENRE) VALUES(?)";
        jdbcTemplate.update(sqlQueryCreateGenre, genre.getName());
    }

    @Override
    public void setGenresInDB(Long filmID, LinkedHashSet<Genre> genres) {
        String sqlQuery = "DELETE FROM genre WHERE FILMID =?";
        jdbcTemplate.update(sqlQuery, filmID);
        if (!genres.isEmpty()) {
            Set<Genre> genresSet = new HashSet<>(genres);
            List<Genre> genresWODouble = new ArrayList<>(genresSet);
            jdbcTemplate.batchUpdate("INSERT INTO genre (FILMID, GENREID) VALUES(?, ?)", new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                    preparedStatement.setString(1, String.valueOf(filmID));
                    preparedStatement.setString(2, String.valueOf(genresWODouble.get(i).getId()));
                }

                @Override
                public int getBatchSize() {
                    return genresWODouble.size();
                }
            });
        }
    }
}