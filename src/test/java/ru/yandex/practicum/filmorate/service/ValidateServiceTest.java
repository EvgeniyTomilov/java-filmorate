package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidateServiceTest {

    @Test
    void email() {
        ValidateService validateService = new ValidateService();
        User user = new User();
        user.setEmail(" ");
        assertThrows(RuntimeException.class, () -> validateService.validateUser(user));
    }

    @Test
    void login() {
        ValidateService validateService = new ValidateService();
        User user = new User();
        user.setEmail("email@list.ru");
        user.setLogin(" ");
        assertThrows(RuntimeException.class, () -> validateService.validateUser(user));
    }

    @Test
    void nameUser() {
        ValidateService validateService = new ValidateService();
        User user = new User();
        user.setEmail("email@list.ru");
        user.setLogin("login");
        user.setName(" ");
        assertThrows(RuntimeException.class, () -> validateService.validateUser(user));
    }

    @Test
    void birthday() {
        ValidateService validateService = new ValidateService();
        User user = new User();
        user.setEmail("email@list.ru");
        user.setLogin("login");
        user.setName("");
        user.setBirthday(LocalDate.of(2045, 5, 5));
        assertThrows(RuntimeException.class, () -> validateService.validateUser(user));
    }

    @Test
    void nameFilm() {
        ValidateService validateService = new ValidateService();
        Film film = new Film();
        film.setName(" ");
        assertThrows(RuntimeException.class, () -> validateService.validateFilm(film));
    }

    @Test
    void description() {
        ValidateService validateService = new ValidateService();
        Film film = new Film();
        film.setName("name film");
        film.setDescription("lkishjfgdkajsdhvashdkvbaslkhvbvdxfgbxcvbxvbxcvbxcvb lkasdhjbvlkalsblsllhsjbksdhvknbsvnb" +
                "ksbv dlkasdhjbvlkalsblsllhsjbksdhvknbsvnbksbvlkasdhjbvlkalsblsllhsjbksdhvknbsvnbksbvlkasdhjbvlkalsbl" +
                "sllhsjbksdhvknbsvnbksbvlkasdhjbvlkalsblsllhsjbksdhvknbsvnbksbvlkasdhjbvlkalsblsllhsjbksdhvknbsvnbks" +
                "bvvbfdgefbgsdfbsdfbsdfbsdgfbsbfdbgdsfbvgf nknxzbv kdfsvzcdldkbjsdkjbgpksdgfpbjsodpkgbjnvkv");
        assertThrows(RuntimeException.class, () -> validateService.validateFilm(film));
    }

    @Test
    void realiseDate() {
        ValidateService validateService = new ValidateService();
        Film film = new Film();
        film.setName("name film");
        film.setDescription("vgf nknxzbv kdfsvzcdldkbjsdkjbgpksdgfpbjsodpkgbjnvkv");
        film.setReleaseDate(LocalDate.of(1845, 5, 5));
        assertThrows(RuntimeException.class, () -> validateService.validateFilm(film));
    }

    @Test
    void duration() {
        ValidateService validateService = new ValidateService();
        Film film = new Film();
        film.setName("name film");
        film.setDescription("vgf nknxzbv kdfsvzcdldkbjsdkjbgpksdgfpbjsodpkgbjnvkv");
        film.setReleaseDate(LocalDate.of(1945, 5, 5));
        film.setDuration(-2L);
        assertThrows(RuntimeException.class, () -> validateService.validateFilm(film));
    }
}