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

    public static final String GET_ALL_QUERY = "SELECT * FROM FILMS F JOIN MPARATINGS ON F.RATINGMPAID = MPARATINGS.RATINGMPAID";
    public static final String GET_BY_ID_QUERY = "SELECT * FROM FILMS F JOIN MPARATINGS ON F.RATINGMPAID = MPARATINGS.RATINGMPAID WHERE ID = ?";

    private final JdbcTemplate jdbcTemplate;
    private final GenresStorage genreStorage;
    private final DirectorService directorService;
    private final RatingDbStorage ratingStorage;

    private Film rowMapFilms(ResultSet rs, int rowNum) throws SQLException {
        Long filmId = rs.getLong("ID");
        Genre genre = Genre.builder()
                .id(rs.getInt("GENREID"))
                .name(rs.getString("GENRE"))
                .build();
        LinkedHashSet<Genre> genres = new LinkedHashSet<>();
        if (genre.getId() == 0) {
            genres = new LinkedHashSet<>();
        } else {
            genres.add(genre);
        }
        Director director = Director.builder()
                .id(rs.getLong("DIRECTOR_ID"))
                .name(rs.getString("DIRECTORNAME"))
                .build();
        List<Director> directors = new ArrayList<>();
        if (director.getId() == 0) {
            directors = new ArrayList<>();
        } else {
            directors.add(director);
        }
        Film film = Film.builder()
                .name(rs.getString("NAME"))
                .description(rs.getString("DESCRIPTION"))
                .releaseDate(rs.getDate("RELEASEDATE").toLocalDate())
                .duration(rs.getInt("DURATION"))
                .mpa(MPA.builder()
                        .id(rs.getInt("RATINGMPAID"))
                        .name(rs.getString("RATINGNAME"))
                        .build())
                .genres(genres)
                .likes(rs.getInt("USERSLIKES"))
                .directors(directors)
                .build();
        film.setId(filmId);
        return film;
    }

    @Override
    public List<Film> searchFilms(String query, String[] searchParameters) {
        String queryByOneParam = searchParameters[0].equals("title") ?
                " LOWER(FILM.NAME) LIKE LOWER('%" + query + "%')" :
                " LOWER(DIRECTORS.NAME) LIKE LOWER('%" + query + "%')";
        String queryByTwoParams = searchParameters.length == 2 ?
                "OR LOWER(DIRECTORS.NAME) LIKE LOWER('%" + query + "%')" : "";

        String sqlQuery = "SELECT FILM.ID, FILM.NAME AS FILMNAME, FILM.DESCRIPTION, FILM.RELEASEDATE, FILM.DURATION, " +
                "R.RATINGMPAID, R.RATINGNAME, COUNT(LIKES.USERID) AS USERSLIKES, NAMES.GENRE, NAMES.GENREID, " +
                "DIRECTORS.DIRECTOR_ID, DIRECTORS.NAME DIRECTORNAME" +
                " FROM FILMS FILM " +
                "LEFT OUTER JOIN " +
                "(SELECT * FROM FILMS_DIRECTORS FD JOIN DIRECTORS D ON D.ID = FD.DIRECTOR_ID ) AS DIRECTORS " +
                "ON DIRECTORS.FILM_ID = FILM.ID " +
                "LEFT JOIN MPARATINGS R ON FILM.RATINGMPAID = R.RATINGMPAID " +
                "LEFT JOIN GENRE GENRE ON FILM.ID = GENRE.FILMID " +
                "LEFT JOIN GENRENAMES NAMES ON GENRE.GENREID = NAMES.GENREID " +
                "LEFT JOIN FILMLIKES LIKES ON FILM.ID = LIKES.FILMID " +
                "WHERE " + queryByOneParam + queryByTwoParams +
                " GROUP BY FILM.ID, NAMES.GENREID " +
                "ORDER BY USERSLIKES DESC";
        return jdbcTemplate.query(sqlQuery, this::rowMapFilms);
    }

    public Film rowMapFilm(ResultSet rs) throws SQLException {
        Long filmId = rs.getLong("ID");
        Film film = Film.builder()
                .name(rs.getString("NAME"))
                .description(rs.getString("DESCRIPTION"))
                .releaseDate(rs.getDate("RELEASEDATE").toLocalDate())
                .duration(rs.getInt("DURATION"))
                .mpa(MPA.builder()
                        .id(rs.getInt("RATINGMPAID"))
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
        film.setDirectors((directorService.findDirectorByFilm(film.getId())));
        if (film.getDirectors() == null) {
            film.setDirectors(new ArrayList<>());
        }
        return Optional.of(films.get(0));
    }

    @Override
    public Film add(Film film) {
        String filmSqlQuery =
                "INSERT INTO FILMS (NAME, DESCRIPTION, RELEASEDATE, DURATION, RATINGMPAID) " +
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
                "INSERT INTO GENRE (FILMID, GENREID) " +
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
                "UPDATE FILMS " +
                        "SET NAME = ?, DESCRIPTION = ?, RELEASEDATE = ?, DURATION = ?, RATINGMPAID = ? " +
                        "WHERE ID = ?";
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
                "DELETE FROM GENRE " +
                        "WHERE FILMID = ?";
        jdbcTemplate.update(genreDeleteSqlQuery, film.getId());

        if (film.getGenres() != null) {
            String genreSqlQuery =
                    "INSERT INTO GENRE (FILMID, GENREID) " + "VALUES (?, ?)";
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
                    "INSERT INTO FILMS_DIRECTORS (FILM_ID, DIRECTOR_ID) VALUES (?, ?)";
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
        String sqlQueryDelete = "DELETE FROM FILMS WHERE ID = ?";
        jdbcTemplate.update(sqlQueryDelete, id);
    }

    private LinkedHashSet<Genre> getGenresOfFilm(Long filmId) {
        String sqlQueryGetGenres = "SELECT GENREID FROM GENRE WHERE FILMID = ?";
        return jdbcTemplate.queryForList(sqlQueryGetGenres, Integer.class, filmId)
                .stream()
                .map(genreStorage::getGenreById)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public List<Film> getPopularsFilms(Integer count, Integer genreId, Integer year) {
        List<Film> films;
        if (genreId == null && year == null) {
            String sql = "SELECT F.*, MPARATINGS.RATINGNAME, COUNT(L.USERID) " +
                    "FROM FILMS AS F " +
                    "LEFT JOIN FILMLIKES AS L ON L.FILMID = F.ID " +
                    "LEFT JOIN MPARATINGS ON MPARATINGS.RATINGMPAID = F.RATINGMPAID\n" +
                    "LEFT JOIN FILMS_DIRECTORS AS FD ON FD.FILM_ID  = F.ID " +
                    "LEFT JOIN DIRECTORS AS D ON D.ID  = FD.DIRECTOR_ID " +
                    "GROUP BY F.ID " +
                    "ORDER BY COUNT(L.USERID) DESC " +
                    "LIMIT ?";

            films = jdbcTemplate.query(sql, (rs, rowNum) -> rowMapFilm(rs), count);
            directorService.setDirectorsListFilmsDB(films);
            return films;
        }

        if (genreId != null && year != null) {
            String sql = "SELECT F.*, MPARATINGS.RATINGNAME FROM FILMS F\n" +
                    "LEFT JOIN GENRE G ON F.ID = G.FILMID\n" +
                    "LEFT JOIN MPARATINGS ON MPARATINGS.RATINGMPAID = F.RATINGMPAID\n" +
                    "LEFT JOIN FILMLIKES L ON L.FILMID = F.ID\n" +
                    "LEFT JOIN FILMS_DIRECTORS AS FD ON FD.FILM_ID  = F.ID " +
                    "LEFT JOIN DIRECTORS AS D ON D.ID  = FD.DIRECTOR_ID " +
                    "WHERE G.GENREID = ? AND YEAR(F.RELEASEDATE) = ? " +
                    "GROUP BY F.ID " +
                    "ORDER BY COUNT(L.USERID) DESC\n" +
                    "LIMIT ? ";
            films = jdbcTemplate.query(sql, (rs, rowNum) -> rowMapFilm(rs), genreId, year, count);
            directorService.setDirectorsListFilmsDB(films);
            return films;
        }

        if (genreId == null) {
            String sql = "SELECT F.*, MPARATINGS.RATINGNAME FROM FILMS F\n" +
                    "LEFT JOIN GENRE G ON F.ID = G.FILMID\n" +
                    "LEFT JOIN MPARATINGS ON MPARATINGS.RATINGMPAID = F.RATINGMPAID\n" +
                    "LEFT JOIN FILMLIKES L ON L.FILMID = F.ID\n" +
                    "LEFT JOIN FILMS_DIRECTORS AS FD ON FD.FILM_ID  = F.ID " +
                    "LEFT JOIN DIRECTORS AS D ON D.ID  = FD.DIRECTOR_ID " +
                    "WHERE YEAR(F.RELEASEDATE) = ? " +
                    "GROUP BY F.ID " +
                    "ORDER BY COUNT(L.USERID) DESC\n" +
                    "LIMIT ? ";
            films = jdbcTemplate.query(sql, (rs, rowNum) -> rowMapFilm(rs), year, count);
            directorService.setDirectorsListFilmsDB(films);
            return films;
        }

        String sql = "SELECT F.*, MPARATINGS.RATINGNAME FROM FILMS F\n" +
                "LEFT JOIN GENRE G ON F.ID = G.FILMID\n" +
                "LEFT JOIN MPARATINGS ON MPARATINGS.RATINGMPAID = F.RATINGMPAID\n" +
                "LEFT JOIN FILMLIKES L ON L.FILMID = F.ID\n" +
                "LEFT JOIN FILMS_DIRECTORS AS FD ON FD.FILM_ID  = F.ID " +
                "LEFT JOIN DIRECTORS AS D ON D.ID  = FD.DIRECTOR_ID " +
                "WHERE G.GENREID = ? " +
                "GROUP BY F.ID " +
                "ORDER BY COUNT(L.USERID) DESC\n" +
                "LIMIT ? ";
        films = jdbcTemplate.query(sql, (rs, rowNum) -> rowMapFilm(rs), genreId, count);
        directorService.setDirectorsListFilmsDB(films);
        return films;
    }

    private String getNameMpaOfFilm(Long ratingId) {
        String sqlQueryGetMpaName = "SELECT RATINGNAME FROM MPARATINGS  WHERE RATINGMPAID = ?";
        return String.valueOf(jdbcTemplate.queryForObject(sqlQueryGetMpaName, Integer.class, ratingId));
    }

    @Override
    public List<Film> getFilmsSortedByYears(Long id) {
        String sqlQueryGetSortedByYear = "SELECT * FROM FILMS JOIN MPARATINGS ON FILMS.RATINGMPAID = MPARATINGS.RATINGMPAID AND FILMS.ID IN" +
                "(SELECT FILM_ID FROM FILMS_DIRECTORS WHERE DIRECTOR_ID = ?)" +
                "ORDER BY FILMS.RELEASEDATE";
        List<Film> films = jdbcTemplate.query(sqlQueryGetSortedByYear, (rs, rowNum) -> rowMapFilm(rs), id);
        directorService.setDirectorsListFilmsDB(films);
        return films;
    }

    @Override
    public List<Film> getFilmsSortedByLikes(Long id) {
        String sqlQuery =
                "SELECT F.*, M.*, COUNT(FL.USERID) AS TOP " +
                        "FROM FILMS AS F " +
                        "LEFT JOIN MPARATINGS AS M ON M.RATINGMPAID = F.RATINGMPAID " +
                        "LEFT JOIN FILMS_DIRECTORS AS D ON D.FILM_ID = F.ID " +
                        "LEFT JOIN FILMLIKES AS FL ON FL.FILMID = F.ID " +
                        "WHERE D.DIRECTOR_ID = ? " +
                        "GROUP BY F.ID " +
                        "ORDER BY TOP ASC;";
        List<Film> films = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> rowMapFilm(rs), id);
        directorService.setDirectorsListFilmsDB(films);
        return films;
    }

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        String sqlQuery = "SELECT F.*, COUNT(FL.USERID) AS TOP FROM FILMLIKES AS FL " +
                "JOIN FILMS AS F ON F.ID = FL.FILMID " +
                "WHERE FL.USERID  IN (?, ?) " +
                "GROUP BY FL.FILMID " +
                "HAVING COUNT(FL.USERID) > 1;";
        List<Film> films = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> getRowMapFilms(rs), userId, friendId);
        return films;
    }

    private Film getRowMapFilms(ResultSet rs) throws SQLException {
        Long id = rs.getLong("ID");
        Film film = Film.builder()
                .name(rs.getString("NAME"))
                .description(rs.getString("DESCRIPTION"))
                .releaseDate(rs.getDate("RELEASEDATE").toLocalDate())
                .duration(rs.getInt("DURATION"))
                .mpa(MPA.builder()
                        .id(rs.getInt("RATINGMPAID"))
                        .name(String.valueOf(ratingStorage.getNameMpa(rs.getInt("RATINGMPAID"))))
                        .build())
                .genres(getGenresOfFilm(id))
                .build();
        film.setId(id);
        return film;
    }
}


