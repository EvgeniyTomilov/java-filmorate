package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.storage.genres.GenresStorage;
import ru.yandex.practicum.filmorate.storage.rating.RatingDbStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component("filmDbStorage")
@Slf4j
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    public static final String GET_ALL_QUERY = "SELECT * FROM FILMS F " +
            "JOIN MPARATINGS ON F.RATINGMPAID = MPARATINGS.RATINGMPAID";
    public static final String GET_BY_ID_QUERY = "SELECT * FROM FILMS F " +
            "JOIN MPARATINGS ON F.RATINGMPAID = MPARATINGS.RATINGMPAID WHERE ID = ?";
    
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
                .releaseDate(rs.getDate("RELEASEDATE")
                        .toLocalDate()).duration(rs.getInt("DURATION"))
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
                .releaseDate(rs.getDate("RELEASEDATE")
                        .toLocalDate())
                .duration(rs.getInt("DURATION"))
                .mpa(MPA.builder().id(rs.getInt("RATINGMPAID"))
                        .name(rs.getString("RATINGNAME"))
                        .build())
                .build();
        film.setId(filmId);
        return film;
    }

    private void fillFilmGenres(Collection<Film> films, List<FilmGenre> filmGenres) {
        films.forEach(film -> {
            LinkedHashSet<Genre> genres = new LinkedHashSet<>();
            filmGenres.stream().filter(filmGenre -> filmGenre.getFilmId() == film.getId())
                    .forEach(fg -> genres.add(fg.getGenre()));
            film.setGenres(genres);
        });
    }

    @Override
    public Collection<Film> getAll() {
        List<FilmGenre> filmGenres = getAllGenresOfFilm();
        Collection<Film> films = jdbcTemplate.query(GET_ALL_QUERY, (rs, rowNum) -> rowMapFilm(rs));
        fillFilmGenres(films, filmGenres);
        directorService.setDirectorsListFilmsDB((List<Film>) films);
        return films;
    }

    @Override
    public Optional<Film> getById(Long id) {
        List<FilmGenre> filmGenres = getAllGenresOfFilm();
        List<Film> films = jdbcTemplate.query(GET_BY_ID_QUERY, (rs, rowNum) -> rowMapFilm(rs), id);
        fillFilmGenres(films, filmGenres);
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
        String filmSqlQuery = "INSERT INTO FILMS (NAME, DESCRIPTION, RELEASEDATE, DURATION, RATINGMPAID) " +
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
        if (film.getGenres() == null && film.getDirectors() == null) {
            Film createdFilm = getById(filmId).orElse(null);
            log.info("Фильм {} добавлен в базу данных", createdFilm);
            return createdFilm;
        }
        LinkedHashSet<Genre> genres = film.getGenres();
        List<Director> directors = film.getDirectors();
        if (genres != null) {
            genreStorage.setGenresInDB(filmId, genres);
        }
        if (directors != null) {
            directorService.setDirectorInDB(filmId, directors);
        }
        Film createdFilm = getById(filmId).orElse(null);
        log.info("Фильм {} добавлен в базу данных", createdFilm);
        return createdFilm;
    }

    @Override
    public Optional<Film> update(Film film) {
        String filmSqlQuery = "UPDATE FILMS " +
                "SET NAME = ?, DESCRIPTION = ?, RELEASEDATE = ?, DURATION = ?, RATINGMPAID = ? " +
                "WHERE ID = ?";
        int updatedRowsCount = jdbcTemplate.update(filmSqlQuery, film.getName(), film.getDescription(),
                Date.valueOf(film.getReleaseDate()), film.getDuration(), film.getMpa().getId(), film.getId());

        if (updatedRowsCount == 0) {
            log.info("Фильма с идентификатором {} нет.", film.getId());
            return Optional.empty();
        }

        LinkedHashSet<Genre> genres = film.getGenres();
        List<Director> directors = film.getDirectors();
        if (genres != null) {
            genreStorage.setGenresInDB(film.getId(), genres);
        } else {
            String genreDeleteSqlQuery = "DELETE FROM GENRE " + "WHERE FILMID = ?";
            jdbcTemplate.update(genreDeleteSqlQuery, film.getId());
        }
        if (directors != null) {
            directorService.setDirectorInDB(film.getId(), directors);
        } else {
            String dirDeleteSqlQuery = "DELETE FROM FILMS_DIRECTORS " + "WHERE FILM_ID = ?";
            jdbcTemplate.update(dirDeleteSqlQuery, film.getId());
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

    public List<FilmGenre> getAllGenresOfFilm() {
        String GET_ALL_GENRES = "SELECT * FROM GENRE G " +
                "JOIN GENRENAMES ON G.GENREID = GENRENAMES.GENREID";
        List<FilmGenre> genres = new ArrayList<>();
        try {
            genres = jdbcTemplate.query(GET_ALL_GENRES, this::rowMapToFilmGenre);
        } catch (EmptyResultDataAccessException e) {
            log.info("В базе нет информации по запросу {}", GET_ALL_GENRES);
        }
        return genres;
    }

    private FilmGenre rowMapToFilmGenre(ResultSet resultSet, int i) throws SQLException {
        return FilmGenre.builder()
                .filmId(resultSet.getInt("FILMID"))
                .genre(Genre.builder()
                        .id(resultSet.getInt("GENREID"))
                        .name(resultSet.getString("GENRE"))
                        .build())
                .build();
    }

    @Override
    public List<Film> getPopularsFilms(Integer count, Integer genreId, Integer year) {
        List<Film> films;
        if (genreId == null && year == null) {
            String sql = "SELECT F.*, MPARATINGS.RATINGNAME, COUNT(L.USERID) " +
                    "FROM FILMS AS F " + "LEFT JOIN FILMLIKES AS L ON L.FILMID = F.ID " +
                    "LEFT JOIN MPARATINGS ON MPARATINGS.RATINGMPAID = F.RATINGMPAID " +
                    "LEFT JOIN FILMS_DIRECTORS AS FD ON FD.FILM_ID  = F.ID " +
                    "LEFT JOIN DIRECTORS AS D ON D.ID  = FD.DIRECTOR_ID " +
                    "GROUP BY F.ID " + "ORDER BY COUNT(L.USERID) DESC " +
                    "LIMIT ?";

            List<FilmGenre> filmGenres = getAllGenresOfFilm();
            films = jdbcTemplate.query(sql, (rs, rowNum) -> rowMapFilm(rs), count);
            fillFilmGenres(films, filmGenres);
            directorService.setDirectorsListFilmsDB(films);
            return films;
        }

        if (genreId != null && year != null) {
            String sql = "SELECT F.*, MPARATINGS.RATINGNAME FROM FILMS F " +
                    "LEFT JOIN GENRE G ON F.ID = G.FILMID " +
                    "LEFT JOIN MPARATINGS ON MPARATINGS.RATINGMPAID = F.RATINGMPAID " +
                    "LEFT JOIN FILMLIKES L ON L.FILMID = F.ID " +
                    "LEFT JOIN FILMS_DIRECTORS AS FD ON FD.FILM_ID  = F.ID " +
                    "LEFT JOIN DIRECTORS AS D ON D.ID  = FD.DIRECTOR_ID " +
                    "WHERE G.GENREID = ? AND YEAR(F.RELEASEDATE) = ? " +
                    "GROUP BY F.ID " + "ORDER BY COUNT(L.USERID) DESC " +
                    "LIMIT ? ";
            List<FilmGenre> filmGenres = getAllGenresOfFilm();
            films = jdbcTemplate.query(sql, (rs, rowNum) -> rowMapFilm(rs), genreId, year, count);
            fillFilmGenres(films, filmGenres);
            directorService.setDirectorsListFilmsDB(films);
            return films;
        }

        if (genreId == null) {
            String sql = "SELECT F.*, MPARATINGS.RATINGNAME FROM FILMS F " +
                    "LEFT JOIN GENRE G ON F.ID = G.FILMID " +
                    "LEFT JOIN MPARATINGS ON MPARATINGS.RATINGMPAID = F.RATINGMPAID " +
                    "LEFT JOIN FILMLIKES L ON L.FILMID = F.ID " +
                    "LEFT JOIN FILMS_DIRECTORS AS FD ON FD.FILM_ID  = F.ID " +
                    "LEFT JOIN DIRECTORS AS D ON D.ID  = FD.DIRECTOR_ID " +
                    "WHERE YEAR(F.RELEASEDATE) = ? " +
                    "GROUP BY F.ID " +
                    "ORDER BY COUNT(L.USERID) DESC " +
                    "LIMIT ? ";
            List<FilmGenre> filmGenres = getAllGenresOfFilm();
            films = jdbcTemplate.query(sql, (rs, rowNum) -> rowMapFilm(rs), year, count);
            fillFilmGenres(films, filmGenres);
            directorService.setDirectorsListFilmsDB(films);
            return films;
        }

        String sql = "SELECT F.*, MPARATINGS.RATINGNAME FROM FILMS F " +
                "LEFT JOIN GENRE G ON F.ID = G.FILMID " +
                "LEFT JOIN MPARATINGS ON MPARATINGS.RATINGMPAID = F.RATINGMPAID " +
                "LEFT JOIN FILMLIKES L ON L.FILMID = F.ID " +
                "LEFT JOIN FILMS_DIRECTORS AS FD ON FD.FILM_ID  = F.ID " +
                "LEFT JOIN DIRECTORS AS D ON D.ID  = FD.DIRECTOR_ID " +
                "WHERE G.GENREID = ? " +
                "GROUP BY F.ID " +
                "ORDER BY COUNT(L.USERID) DESC " +
                "LIMIT ? ";
        List<FilmGenre> filmGenres = getAllGenresOfFilm();
        films = jdbcTemplate.query(sql, (rs, rowNum) -> rowMapFilm(rs), genreId, count);
        fillFilmGenres(films, filmGenres);
        directorService.setDirectorsListFilmsDB(films);
        return films;
    }


    @Override
    public List<Film> getFilmsSortedByYears(Long id) {
        String sqlQueryGetSortedByYear = "SELECT * FROM FILMS " +
                "JOIN MPARATINGS ON FILMS.RATINGMPAID = MPARATINGS.RATINGMPAID AND FILMS.ID IN" +
                "(SELECT FILM_ID FROM FILMS_DIRECTORS WHERE DIRECTOR_ID = ?)" +
                "ORDER BY FILMS.RELEASEDATE";
        List<FilmGenre> filmGenres = getAllGenresOfFilm();
        List<Film> films = jdbcTemplate.query(sqlQueryGetSortedByYear, (rs, rowNum) -> rowMapFilm(rs), id);
        fillFilmGenres(films, filmGenres);
        directorService.setDirectorsListFilmsDB(films);
        return films;
    }

    @Override
    public List<Film> getFilmsSortedByLikes(Long id) {
        String sqlQuery = "SELECT F.*, M.*, COUNT(FL.USERID) AS TOP " +
                "FROM FILMS AS F " +
                "LEFT JOIN MPARATINGS AS M ON M.RATINGMPAID = F.RATINGMPAID " +
                "LEFT JOIN FILMS_DIRECTORS AS D ON D.FILM_ID = F.ID " +
                "LEFT JOIN FILMLIKES AS FL ON FL.FILMID = F.ID " +
                "WHERE D.DIRECTOR_ID = ? " +
                "GROUP BY F.ID " +
                "ORDER BY TOP ASC;";
        List<FilmGenre> filmGenres = getAllGenresOfFilm();
        List<Film> films = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> rowMapFilm(rs), id);
        fillFilmGenres(films, filmGenres);
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
        List<FilmGenre> filmGenres = getAllGenresOfFilm();
        List<Film> films = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> getRowMapFilms(rs), userId, friendId);
        fillFilmGenres(films, filmGenres);
        return films;
    }

    private Film getRowMapFilms(ResultSet rs) throws SQLException {
        Long id = rs.getLong("ID");
        Film film = Film.builder()
                .name(rs.getString("NAME"))
                .description(rs.getString("DESCRIPTION"))
                .releaseDate(rs.getDate("RELEASEDATE")
                        .toLocalDate())
                .duration(rs.getInt("DURATION"))
                .mpa(MPA.builder()
                        .id(rs.getInt("RATINGMPAID"))
                        .name(String.valueOf(ratingStorage.getNameMpa(rs.getInt("RATINGMPAID"))))
                        .build())
                .build();
        film.setId(id);
        return film;
    }

    @Override
    public Collection<Integer> getUserRecommendations(int userId) {
        final String sql = "SELECT f.id " +
                "FROM FILMLIKES AS l1 " +
                "INNER JOIN films AS f ON l1.filmid = f.id " +
                "WHERE l1.userid = (" +
                "SELECT l2.userid FROM FILMLIKES AS l2 WHERE l2.userid <> ? " +
                "AND l2.filmid IN (" +
                "SELECT l3.filmid FROM FILMLIKES AS l3 WHERE l3.userid = ?)" +
                "GROUP BY l2.userid " +
                "ORDER BY COUNT (l2.filmid) DESC " +
                "LIMIT 1)" +
                "AND l1.filmid NOT IN (SELECT l4.filmid FROM FILMLIKES AS l4 WHERE l4.userid = ?)";
        return jdbcTemplate.queryForList(sql, Integer.class, userId, userId, userId);
    }
}