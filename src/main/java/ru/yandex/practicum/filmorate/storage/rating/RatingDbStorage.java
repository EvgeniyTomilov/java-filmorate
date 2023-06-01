package ru.yandex.practicum.filmorate.storage.rating;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.MPA;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component("RatingDbStorage")
public class RatingDbStorage implements RatingStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public RatingDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private MPA rowMapToMpa(ResultSet resultSet, int i) throws SQLException {
        return MPA.builder()
                .id(resultSet.getInt("ratingMPAId"))
                .name(resultSet.getString("ratingName"))
                .build();
    }

    @Override
    public Optional<MPA> getRatingById(Integer mpaId) {
        String sqlQueryGetById = "SELECT * FROM MPARatings WHERE ratingMPAId = ?";
        List<MPA> mpas = jdbcTemplate.query(sqlQueryGetById, (rs, rowNum) -> rowMapToMpa(rs, mpaId), mpaId);
        if (mpas.isEmpty()) {
            log.info("Рейтинг с идентификатором {} не найден.", mpaId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        log.info("Найден рейтинг: {}", mpas.get(0));
        return Optional.of(mpas.get(0));
    }

    @Override
    public List<MPA> getRatingAll() {
        List<MPA> mpas = new ArrayList<>();
        String sqlQueryGetAll = "SELECT * FROM MPARatings";
        try {
            mpas = jdbcTemplate.query(sqlQueryGetAll, this::rowMapToMpa);
        } catch (EmptyResultDataAccessException e) {
            log.info("В базе нет информации по запросу {}", sqlQueryGetAll);
        }
        return mpas;
    }
}