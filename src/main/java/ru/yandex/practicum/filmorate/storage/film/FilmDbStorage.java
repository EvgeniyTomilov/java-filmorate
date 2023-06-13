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
import ru.yandex.practicum.filmorate.storage.genres.GenresStorage;
import ru.yandex.practicum.filmorate.storage.rating.RatingDbStorage;

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
    public static final String GET_ALL_QUERY =
            "SELECT * FROM films join MPARatings on films.ratingMPAId = MPARatings.ratingMPAId";
    public static final String GET_BY_ID_QUERY =
            "SELECT * FROM films join MPARatings on films.ratingMPAId = MPARatings.ratingMPAId WHERE id = ?";

    private final JdbcTemplate jdbcTemplate;
    private final GenresStorage genreStorage;
    private final DirectorService directorService;
    private final RatingDbStorage ratingStorage;

    private Film rowMapFilms(ResultSet rs, int rowNum) throws SQLException {
        Long filmId = rs.getLong("id");
        Genre genre = Genre.builder()
                .id(rs.getInt("genreid"))
                .name(rs.getString("genre"))
                .build();
        HashSet<Genre> genres = new LinkedHashSet<>();
        genres.add(genre);
        Film film = Film.builder()
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("releaseDate").toLocalDate())
                .duration(rs.getInt("duration"))
                .mpa(MPA.builder()
                        .id(rs.getInt("ratingMPAId"))
                        .name(rs.getString("RATINGNAME"))
                        .build())
                .genres(genres)
                .likes(rs.getInt("usersLikes"))
                .build();
        film.setId(filmId);
        return film;
    }

    private Film rowMapFilm(ResultSet rs) throws SQLException {
        Long filmId = rs.getLong("id");
        Film film = Film.builder()
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("releaseDate").toLocalDate())
                .duration(rs.getInt("duration"))
                .mpa(MPA.builder()
                        .id(rs.getInt("ratingMPAId"))
                        .name(rs.getString("RATINGNAME"))
                        .build())
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

    private String getNameMpaOfFilm(Long ratingId) {
        String sqlQueryGetMpaName = "SELECT RATINGNAME FROM MPARatings  WHERE RATINGMPAID = ?";
        return String.valueOf(jdbcTemplate.queryForObject(sqlQueryGetMpaName, Integer.class, ratingId));
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

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        String sqlQuery = "SELECT f.*, count(fl.USERID) AS top FROM FILMLIKES AS fl " +
                "JOIN FILMS AS f ON f.ID = fl.FILMID " +
                "WHERE fl.USERID  in (?, ?) " +
                "GROUP BY fl.FILMID " +
                "HAVING COUNT(fl.USERID) > 1;";
        List<Film> films = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> getRowMapFilms(rs), userId, friendId);
        return films;
    }

    private Film getRowMapFilms(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        Film film = Film.builder()
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("releaseDate").toLocalDate())
                .duration(rs.getInt("duration"))
                .mpa(MPA.builder()
                        .id(rs.getInt("ratingMPAId"))
                        .name(String.valueOf(ratingStorage.getNameMpa(rs.getInt("ratingMPAId"))))
                        .build())
                .genres(getGenresOfFilm(id))
                .build();
        film.setId(id);
        return film;
    }


    @Override
    public List<Film> searchFilms(String query, String[] searchParameters) {
        String queryByOneParam = searchParameters[0].equals("title") ?
                " lower(film.name) like lower('%" + query + "%')" :
                " lower(d.name) like lower('%" + query + "%')"; //
        String queryByTwoParams = searchParameters.length == 2 ?
                "or lower(name) like lower('%" + query + "%')" : "";// изменить по факту имя директора name

        String sqlQuery = "SELECT film.id, film.name, film.description, film.releasedate, film.duration, " +
                "MPARatings.ratingMPAId, MPARatings.ratingname" +// добавить все поля для фильма
                "count(likes.userid) as usersLikes, names.genre, names.genreid, fd.director_id, d.name " +//fd.director_id, d.name
                "FROM films film " +
                "JOIN MPARatings on films.ratingMPAId = MPARatings.ratingMPAId " +
                "JOIN genre genre on film.id = genre.filmid " +
                "JOIN genrenames names on genre.genreid = names.genreid " +
                "JOIN film_directors as fd on film.film_id = fd.film_id " +// JOIN film_directors as fd on film.film_id = fd.film_id
                "JOIN directors d ON d.id = fd.director_id " + //JOIN directors d ON d.id = fd.director_id
                "WHERE " + queryByOneParam + queryByTwoParams +
                " GROUP BY film.id, names.genreid " +
                "ORDER BY usersLikes desc";
        return jdbcTemplate.query(sqlQuery,this::rowMapFilms); //  дописать после добавления фичи режисера
    }
}
