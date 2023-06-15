package ru.yandex.practicum.filmorate.storage.genres;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
}