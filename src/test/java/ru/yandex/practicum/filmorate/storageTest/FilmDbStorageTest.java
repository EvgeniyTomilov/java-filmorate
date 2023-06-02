package ru.yandex.practicum.filmorate.storageTest;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmDbStorageTest {
    private final FilmDbStorage filmStorage;

    @BeforeEach
    public void createUpdateData() {
        Film filmTestBeforeEach = Film.builder()
                .id(1L)
                .name("name_test")
                .description("testtest")
                .releaseDate(LocalDate.of(2023, 1, 1))
                .duration(50)
                .mpa(MPA.builder().id(1).build())
                .build();
        if (filmStorage.getById(1L).isPresent()) {
            filmStorage.add(filmTestBeforeEach);
            return;
        }
        filmStorage.update(filmTestBeforeEach);
    }

    @AfterEach
    public void deleteFilmAfterEach() {
        List<Film> films = (List<Film>) filmStorage.getAll();
        for (Film film : films) {
            if (film.getId() != 1L) {
                filmStorage.delete(film.getId());
            }
        }
    }

    @Test
    public void update() {
        Film update = Film.builder()
                .id(1L)
                .name("name_test1_update")
                .description("des_test_update")
                .releaseDate(LocalDate.of(2011, 5, 2))
                .duration(20)
                .mpa(MPA.builder().id(1).build())
                .build();
        filmStorage.update(update);
        Optional<Film> filmOptional = filmStorage.getById(1L);
        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue("name", "name_test1_update"));
    }

    @Test
    public void updateFail() {
        Film update = Film.builder()
                .id(2L)
                .name("name_test_update")
                .description("des_test_update")
                .releaseDate(LocalDate.of(2013, 1, 1))
                .duration(89)
                .mpa(MPA.builder().id(1).build())
                .build();
        filmStorage.update(update);
        Optional<Film> film = filmStorage.getById(2L);
        assertThat(film)
                .isNotPresent();
    }

    @Test
    public void delete() {
        Film filmTestDelete = Film.builder()
                .id(2L)
                .name("name_test_update")
                .description("des_test_update")
                .releaseDate(LocalDate.of(2021, 1, 1))
                .duration(10)
                .mpa(MPA.builder().id(1).build())
                .build();
        filmStorage.add(filmTestDelete);
        filmStorage.delete(filmTestDelete.getId());
        Optional<Film> film = filmStorage.getById(2L);
        assertThat(film)
                .isNotPresent();
    }

    @Test
    public void deleteFail() {
        Film filmTestDelete = Film.builder()
                .id(2L)
                .name("name_test_update")
                .description("des_test")
                .releaseDate(LocalDate.of(2020, 2, 2))
                .duration(10)
                .mpa(MPA.builder().id(1).build())
                .build();
        filmStorage.delete(filmTestDelete.getId());
        Optional<Film> film = filmStorage.getById(2L);
        assertThat(film)
                .isNotPresent();
    }

    @Test
    public void getAll() {
        Film filmTestGetAll = Film.builder()
                .id(2L)
                .name("name_test_update")
                .description("des_test")
                .releaseDate(LocalDate.of(2020, 8, 1))
                .duration(10)
                .mpa(MPA.builder().id(1).build())
                .build();
        filmStorage.add(filmTestGetAll);
        List<Film> result = (List<Film>) filmStorage.getAll();
        Assertions.assertEquals(3, result.size());
    }
}