package ru.yandex.practicum.filmorate.storage.RowMapper;

import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.rating.RatingDbStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class RowMapper {
    private final RatingDbStorage ratingStorage;
    private final FilmDbStorage filmDbStorage;

    public RowMapper(RatingDbStorage ratingStorage, FilmDbStorage filmDbStorage) {
        this.ratingStorage = ratingStorage;
        this.filmDbStorage = filmDbStorage;
    }

    private User rowMapToUser(ResultSet resultSet, int i) throws SQLException {
        LocalDate birthday;
        if (resultSet.getDate("birthday") == null) {
            birthday = LocalDate.of(0, 1, 1);
        } else {
            birthday = resultSet.getDate("birthday").toLocalDate();
        }
        return User.builder()
                .id(resultSet.getLong("id"))
                .email((resultSet.getString("email")))
                .login(resultSet.getString("login"))
                .name(resultSet.getString("name"))
                .birthday(birthday)
                .build();
    }

    private Review rowMapReview(ResultSet rs) throws SQLException {
        Long reviewId = rs.getLong("REVIEW_ID");
        Review review = Review.builder()
                .filmId(rs.getLong("FILM_ID"))
                .userId(rs.getLong("USER_ID"))
                .content(rs.getString("CONTENT"))
                .isPositive(rs.getBoolean("IS_POSITIVE"))
                .useful(rs.getInt("USEFUL"))
                .build();
        review.setReviewId(reviewId);
        return review;
    }

    public MPA rowMapToMpa(ResultSet resultSet, int i) throws SQLException {
        return MPA.builder()
                .id(resultSet.getInt("ratingMPAId"))
                .name(resultSet.getString("ratingName"))
                .build();
    }

    private Genre rowMapToGenre(ResultSet resultSet, int i) throws SQLException {
        return Genre.builder()
                .id(resultSet.getInt("GENREID"))
                .name(resultSet.getString("GENRE"))
                .build();
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
                .genres(filmDbStorage.getGenresOfFilm(id))
                .build();
        film.setId(id);
        return film;
    }

    public Event mapRowFeed(ResultSet rs, int rowNum) throws SQLException {
        return Event.builder()
                .eventId(rs.getLong("EVENT_ID"))
                .userId(rs.getLong("USER_ID"))
                .entityId(rs.getLong("ENTITY_ID"))
                .eventType(EventTypes.valueOf(rs.getString("EVENT_TYPE")))
                .operation(Operations.valueOf(rs.getString("OPERATION")))
                .timestamp(rs.getLong("TIMESTAMP"))
                .build();
    }

    private Director rowMapToDirector(ResultSet resultSet, int i) throws SQLException {
        return Director.builder()
                .id(resultSet.getLong("id"))
                .name(resultSet.getString("name"))
                .build();
    }

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
                .genres(filmDbStorage.getGenresOfFilm(filmId))
                .build();
        film.setId(filmId);
        return film;
    }
}
