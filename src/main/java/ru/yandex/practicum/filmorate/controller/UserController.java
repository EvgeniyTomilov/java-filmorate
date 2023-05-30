package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.service.ValidateService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@RequestBody User user) {
        log.info("create user: {}", user);
        ValidateService.validateId(user);
        return userService.createUser(user);

    }

    @PutMapping
    public User update(@RequestBody User user) {
        ValidateService.validateId(user);
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

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        log.info("Get user by id = {}", id);
        ValidateService.validateId(id);
        return userService.getUserById(id);
    }

    @GetMapping(value = "/{id}/friends")
    public Collection<User> getFriends(@PathVariable Long id) {
        log.info("Вызов друзей пользователя" + id + "...");
        return userService.getListOfFriends(id);
    }

    @GetMapping(value = "/{id}/friends/common/{userId}")
    public Collection<User> getMutualFriends( @PathVariable Long id, @PathVariable Long userId) {
        log.info("Вызов взаимных друзей пользователя " + id + " и пользователя " + userId + "...");
        return userService.getListOfMutualFriends(id, userId);
    }

    @PutMapping(value = "/{id}/friends/{userId}")
    public void addFriend( @PathVariable Long id, @PathVariable Long userId) {
        log.info("Добавление друга " + id + "...");
        userService.addFriend(id, userId);
        log.info("Друг добавлен");
    }

    @DeleteMapping(value = "/{id}/friends/{userId}")
    public void removeFriend( @PathVariable Long id, @PathVariable Long userId) {
        log.info("Добавление друга " + id + "...");
        userService.removeFriend(id, userId);
        log.info("Друг удален");
    }
}
