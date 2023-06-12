package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@Slf4j
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.info("Создание пользователя...");
        return userService.createUser(user);
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.info("Обновление пользователя " + user.getId() + "...");
        return userService.updateUser(user);
    }

    @GetMapping
    public Collection<User> getAllUsers() {
        log.info("Вызов всех пользователей...");
        return userService.getAllUsers();
    }

    @GetMapping(value = "/{id}")
    public User getUserById(@Valid @PathVariable Long id) {
        log.info("Вызов пользователя по ID:" + id + "...");
        return userService.getUserById(id);
    }

    @PutMapping("/{id}/friends/{userId}")
    public void addFriend(@Valid @PathVariable Long id, @Valid @PathVariable Long userId) {
        log.info("Добавление друга " + id + "...");
        userService.addFriend(id, userId);
        log.info("Друг добавлен");
    }

    @DeleteMapping("/{id}/friends/{userId}")
    public void deleteFriend(@Valid @PathVariable Long id, @Valid @PathVariable Long userId) {
        log.info("Добавление друга " + id + "...");
        userService.deleteFriend(id, userId);
        log.info("Друг удален");
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getListOfFriends(@Valid @PathVariable Long id) {
        log.info("Вызов друзей пользователя" + id + "...");
        return userService.getListOfFriends(id);
    }


    @GetMapping("/{id}/friends/common/{userId}")
    public Collection<User> getListSharedFriends(@Valid @PathVariable Long id, @Valid @PathVariable Long userId) {
        log.info("Вызов взаимных друзей пользователя " + id + " и пользователя " + userId + "...");
        return userService.getListSharedFriends(id, userId);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        log.info("Удаление пользователя по id:" + userId + "...");
        userService.deleteUser(userId);
        log.info("Пользователь удален");
    }

    @GetMapping("/{id}/recommendations")
    public Collection<Film> getRecommendations(@Valid @PathVariable Long id) {
        log.info("Получение рекомендаций пользователя " + id);
        return userService.getRecommendations(id);
    }
}