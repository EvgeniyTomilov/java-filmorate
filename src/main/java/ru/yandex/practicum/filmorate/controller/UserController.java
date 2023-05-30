package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.user.UserService;

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

    @DeleteMapping
    public void deleteUser(@PathVariable Long id) {
        log.info("Удаление пользователя...");
        userService.deleteUser(id);
        log.info("Пользователь удален");
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

    @GetMapping(value = "/{id}/friends")
    public Collection<User> getFriends(@Valid @PathVariable Long id) {
        log.info("Вызов друзей пользователя" + id + "...");
        return userService.getListOfFriends(id);
    }

    @GetMapping(value = "/{id}/friends/common/{userId}")
    public Collection<User> getMutualFriends(@Valid @PathVariable Long id, @Valid @PathVariable Long userId) {
        log.info("Вызов взаимных друзей пользователя " + id + " и пользователя " + userId + "...");
        return userService.getListOfMutualFriends(id, userId);
    }

    @PutMapping(value = "/{id}/friends/{userId}")
    public void addFriend(@Valid @PathVariable Long id, @Valid @PathVariable Long userId) {
        log.info("Добавление друга " + id + "...");
        userService.addFriend(id, userId);
        log.info("Друг добавлен");
    }

    @DeleteMapping(value = "/{id}/friends/{userId}")
    public void removeFriend(@Valid @PathVariable Long id, @Valid @PathVariable Long userId) {
        log.info("Добавление друга " + id + "...");
        userService.removeFriend(id, userId);
        log.info("Друг удален");
    }
}