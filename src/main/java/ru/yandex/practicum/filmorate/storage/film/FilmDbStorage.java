package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.storage.genres.GenresDbStorage;
import ru.yandex.practicum.filmorate.storage.genres.GenresStorage;
import ru.yandex.practicum.filmorate.storage.rating.RatingDbStorage;
import ru.yandex.practicum.filmorate.storage.rating.RatingStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Component("filmDbStorage")
@Slf4j
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    public static final String GET_ALL_QUERY = "SELECT * FROM films f join MPARatings on f.ratingMPAId = MPARatings.ratingMPAId";
    public static final String GET_BY_ID_QUERY = "SELECT * FROM films f join MPARatings on f.ratingMPAId = MPARatings.ratingMPAId WHERE id = ?";

    private final JdbcTemplate jdbcTemplate;
    private final GenresStorage genreStorage;
    private final DirectorService directorService;


    public Film rowMapFilm(ResultSet rs) throws SQLException {
        Long filmId = rs.getLong("id");
        Film film = Film.builder()
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("releaseDate").toLocalDate())
                .duration(rs.getInt("duration"))
                .mpa(MPA.builder()
                        .id(rs.getInt("ratingMPAId"))
                        .name(rs.getString("ratingName"))
                        .build())
                .genres(getGenresOfFilm(filmId))
                .build();
        film.setId(filmId);
        return film;
    }

    public Film rowMapFilm1(ResultSet rs) throws SQLException {
        Long filmId = rs.getLong("id");
        Film film = Film.builder()
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("releaseDate").toLocalDate())
                .duration(rs.getInt("duration"))
                .genres(getGenresOfFilm(filmId))
                .build();
        film.setId(filmId);
        return film;
    }


    @Override
    public Collection<Film> getAll() {
        Collection<Film> films = jdbcTemplate.query(GET_ALL_QUERY, (rs, rowNum) -> rowMapFilm(rs));
        directorService.setDirectorsListFilmsDB((List<Film>) films);
        return films;
    }


    @Override
    public Optional<Film> getById(Long id) {
        List<Film> films = jdbcTemplate.query(GET_BY_ID_QUERY, (rs, rowNum) -> rowMapFilm(rs), id);
        if (films.isEmpty()) {
            log.info("Фильма с идентификатором {} нет.", id);
            return Optional.empty();
        }
        log.info("В базе данных найден фильм: {}", films.get(0));
        Film film = films.get(0);
        film.setDirectors(directorService.findDirectorByFilm(film.getId()));
        if (film.getDirectors() == null) {
            film.setDirectors(new ArrayList<>());
        }
        return Optional.of(films.get(0));
    }

    @Override
    public Film add(Film film) {
        String filmSqlQuery =
                "INSERT INTO films (name, description, releaseDate, duration, ratingMPAId) " +
                        "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int updatedRowsCount = jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(filmSqlQuery, new String[]{"id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            stmt.setInt(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);

        if (updatedRowsCount == 0) {
            log.info("При добавлении фильма {} в базу данных произошла ошибка", film);
            return null;
        }
        Long filmId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        if (film.getGenres() == null) {
            Film createdFilm = getById(filmId).orElse(null);
            log.info("Фильм {} добавлен в базу данных", createdFilm);
            return createdFilm;
        }

        String genreSqlQuery =
                "INSERT INTO genre (filmId, genreId) " +
                        "VALUES (?, ?)";

        film.getGenres().forEach(genre -> {
            jdbcTemplate.update(genreSqlQuery,
                    filmId,
                    genre.getId());
        });
        if (film.getDirectors() == null) {
            Film createdFilm = getById(filmId).orElse(null);
            log.info("Фильм {} добавлен в базу данных", createdFilm);
            return createdFilm;
        }
        directorService.setDirectorInDB(filmId, film.getDirectors());
        Film createdFilm = getById(filmId).orElse(null);
        log.info("Фильм {} добавлен в базу данных", createdFilm);
        return createdFilm;
    }

    @Override
    public Optional<Film> update(Film film) {
        String filmSqlQuery =
                "UPDATE films " +
                        "SET name = ?, description = ?, releaseDate = ?, duration = ?, ratingMPAId = ? " +
                        "WHERE id = ?";
        int updatedRowsCount = jdbcTemplate.update(filmSqlQuery,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        if (updatedRowsCount == 0) {
            log.info("Фильма с идентификатором {} нет.", film.getId());
            return Optional.empty();
        }

        String genreDeleteSqlQuery =
                "DELETE FROM genre " +
                        "WHERE filmId = ?";
        jdbcTemplate.update(genreDeleteSqlQuery, film.getId());


        if (film.getGenres() != null) {
            String genreSqlQuery =
                    "INSERT INTO genre (filmId, genreId) " + "VALUES (?, ?)";
            film.getGenres().forEach(genre -> {
                jdbcTemplate.update(genreSqlQuery,
                        film.getId(),
                        genre.getId());
            });
        }

        String directorsDeleteSqlQuery =
                "DELETE FROM FILMS_DIRECTORS " +
                        "WHERE film_Id = ?";
        jdbcTemplate.update(directorsDeleteSqlQuery, film.getId());

        if (film.getDirectors() != null) {
            String updateDirectorQuery =
                    "INSERT INTO films_directors (film_id, director_id) VALUES (?, ?)";
            film.getDirectors().forEach(director -> {
                jdbcTemplate.update(updateDirectorQuery,
                        film.getId(),
                        director.getId());
            });
        }
        Optional<Film> updatedFilm = this.getById(film.getId());
        log.info("Фильм {} обновлен в базе данных", updatedFilm);
        return updatedFilm;
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            return;
        }
        String sqlQueryDelete = "DELETE FROM films WHERE id = ?";
        jdbcTemplate.update(sqlQueryDelete, id);
    }

    private LinkedHashSet<Genre> getGenresOfFilm(Long filmId) {
        String sqlQueryGetGenres = "SELECT genreId FROM genre WHERE filmId = ?";
        return jdbcTemplate.queryForList(sqlQueryGetGenres, Integer.class, filmId)
                .stream()
                .map(genreStorage::getGenreById)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public List<Film> getPopularsFilms(Integer count, Integer genreId, Integer year) {
        if (genreId == null && year == null) {
            String sql = "SELECT f.*, MPARatings.ratingName, COUNT(l.userId) " +
                    "FROM films AS f " +
                    "LEFT JOIN filmLikes AS l ON l.filmId = f.id " +
                    "LEFT JOIN MPARatings ON MPARatings.ratingMPAId = f.ratingMPAId\n" +
                    "LEFT JOIN films_directors AS fd ON fd.film_id  = f.id " +
                    "LEFT JOIN directors AS d ON d.id  = fd.director_id " +
                    "GROUP BY f.id " +
                    "ORDER BY COUNT(l.userId) DESC " +
                    "LIMIT ?";
            return jdbcTemplate.query(sql, (rs, rowNum) -> rowMapFilm(rs), count);
        }

        if (genreId != null && year != null) {
            String sql = "SELECT f.*, MPARatings.ratingName FROM films f\n" +
                    "LEFT JOIN genre g on f.id = g.filmId\n" +
                    "LEFT JOIN MPARatings ON MPARatings.ratingMPAId = f.ratingMPAId\n" +
                    "LEFT JOIN filmLikes l ON l.filmId = f.id\n" +
                    "LEFT JOIN films_directors AS fd ON fd.film_id  = f.id " +
                    "LEFT JOIN directors AS d ON d.id  = fd.director_id " +
                    "WHERE g.genreId = ? AND YEAR(f.releaseDate) = ? " +
                    "GROUP BY f.id " +
                    "ORDER BY COUNT(l.userId) DESC\n" +
                    "LIMIT ? ";
            return jdbcTemplate.query(sql, (rs, rowNum) -> rowMapFilm(rs), genreId, year, count);
        }

        if (genreId == null) {
            String sql = "SELECT f.*, MPARatings.ratingName FROM films f\n" +
                    "LEFT JOIN genre g on f.id = g.filmId\n" +
                    "LEFT JOIN MPARatings ON MPARatings.ratingMPAId = f.ratingMPAId\n" +
                    "LEFT JOIN filmLikes l ON l.filmId = f.id\n" +
                    "LEFT JOIN films_directors AS fd ON fd.film_id  = f.id " +
                    "LEFT JOIN directors AS d ON d.id  = fd.director_id " +
                    "WHERE YEAR(f.releaseDate) = ? " +
                    "GROUP BY f.id " +
                    "ORDER BY COUNT(l.userId) DESC\n" +
                    "LIMIT ? ";
            return jdbcTemplate.query(sql, (rs, rowNum) -> rowMapFilm(rs), year, count);
        }

        String sql = "SELECT f.*, MPARatings.ratingName FROM films f\n" +
                "LEFT JOIN genre g on f.id = g.filmId\n" +
                "LEFT JOIN MPARatings ON MPARatings.ratingMPAId = f.ratingMPAId\n" +
                "LEFT JOIN filmLikes l ON l.filmId = f.id\n" +
                "LEFT JOIN films_directors AS fd ON fd.film_id  = f.id " +
                "LEFT JOIN directors AS d ON d.id  = fd.director_id " +
                "WHERE g.genreId = ? " +
                "GROUP BY f.id " +
                "ORDER BY COUNT(l.userId) DESC\n" +
                "LIMIT ? ";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rowMapFilm(rs), genreId, count);
    }

    @Override
    public List<Film> getFilmsSortedByYears(Long id) {
        String sqlQueryGetSortedByYear = "SELECT * FROM films join MPARatings on films.ratingMPAId = MPARatings.ratingMPAId AND films.id in" +
                "(SELECT FILM_ID from FILMS_DIRECTORS WHERE DIRECTOR_ID = ?)" +
                "ORDER BY films.releasedate";
        List<Film> films = jdbcTemplate.query(sqlQueryGetSortedByYear, (rs, rowNum) -> rowMapFilm(rs), id);
        directorService.setDirectorsListFilmsDB(films);
        return films;
    }

    @Override
    public List<Film> getFilmsSortedByLikes(Long id) {
        String sqlQuery =
                "SELECT f.*, m.*, count(fl.USERID) AS top " +
                        "FROM FILMS AS f " +
                        "LEFT JOIN MPARATINGS AS m ON m.RATINGMPAID = f.RATINGMPAID " +
                        "LEFT JOIN FILMS_DIRECTORS AS d ON d.FILM_ID = f.ID " +
                        "LEFT JOIN FILMLIKES AS fl ON fl.FILMID = f.ID " +
                        "WHERE d.DIRECTOR_ID = ? " +
                        "GROUP BY f.ID " +
                        "ORDER BY top ASC;";
        List<Film> films = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> rowMapFilm(rs), id);
        directorService.setDirectorsListFilmsDB(films);
        return films;
    }
}

