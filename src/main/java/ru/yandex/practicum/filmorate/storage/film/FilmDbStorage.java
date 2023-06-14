package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
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

    public static final String GET_ALL_QUERY = "SELECT * FROM films f join MPARatings on f.ratingMPAId = MPARatings.ratingMPAId";
    public static final String GET_BY_ID_QUERY = "SELECT * FROM films f join MPARatings on f.ratingMPAId = MPARatings.ratingMPAId WHERE id = ?";

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
        LinkedHashSet<Genre> genres = new LinkedHashSet<>();
        if (genre.getId() == 0) {
            genres = new LinkedHashSet<>();
        } else {
            genres.add(genre);
        }
        Director director = Director.builder()
                .id(rs.getLong("director_id"))
                .name(rs.getString("directorname"))
                .build();
        List<Director> directors = new ArrayList<>();
        if (director.getId() == 0) {
            directors = new ArrayList<>();
        } else {
            directors.add(director);
        }
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
                .directors(directors)
                .build();
        film.setId(filmId);
        return film;
    }

    @Override
    public List<Film> searchFilms(String query, String[] searchParameters) {
        String queryByOneParam = searchParameters[0].equals("title") ?
                " lower(film.name) like lower('%" + query + "%')" :
                " lower(directors.name) like lower('%" + query + "%')";
        String queryByTwoParams = searchParameters.length == 2 ?
                "or lower(directors.name) like lower('%" + query + "%')" : "";

        String sqlQuery = "SELECT film.id, film.name as filmname, film.description, film.releasedate, film.duration, " +
                "r.ratingMPAId, r.ratingname, count(likes.userid) as usersLikes, names.genre, names.genreid, " +
                "directors.director_id, directors.name directorname" +
                " FROM films film " +
                "LEFT OUTER JOIN " +
                "(SELECT * FROM FILMS_DIRECTORS fd JOIN directors d ON d.id = fd.director_id ) as directors " +
                "ON directors.film_id = film.id " +
                "LEFT JOIN MPARatings r on film.ratingMPAId = r.ratingMPAId " +
                "LEFT JOIN genre genre on film.id = genre.filmid " +
                "LEFT JOIN genrenames names on genre.genreid = names.genreid " +
                "LEFT JOIN filmlikes likes on film.id = likes.filmid " +
                "WHERE " + queryByOneParam + queryByTwoParams +
                " GROUP BY film.id, names.genreid " +
                "ORDER BY usersLikes desc";
        return jdbcTemplate.query(sqlQuery, this::rowMapFilms);
    }

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
        film.setDirectors((directorService.findDirectorByFilm(film.getId())));
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
        List<Film> films;
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

            films = jdbcTemplate.query(sql, (rs, rowNum) -> rowMapFilm(rs), count);
            directorService.setDirectorsListFilmsDB(films);
            return films;
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
            films = jdbcTemplate.query(sql, (rs, rowNum) -> rowMapFilm(rs), genreId, year, count);
            directorService.setDirectorsListFilmsDB(films);
            return films;
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
            films = jdbcTemplate.query(sql, (rs, rowNum) -> rowMapFilm(rs), year, count);
            directorService.setDirectorsListFilmsDB(films);
            return films;
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
        films = jdbcTemplate.query(sql, (rs, rowNum) -> rowMapFilm(rs), genreId, count);
        directorService.setDirectorsListFilmsDB(films);
        return films;
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
}


