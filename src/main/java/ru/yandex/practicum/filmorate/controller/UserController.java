package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.service.ValidateService;

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
        return userService.save(user);

    }

    @PutMapping
    public User update(@RequestBody User user) {
        ValidateService.validateId(user);
        return userService.update(user);
    }

    @GetMapping
    public List<User> getUsers() {
        log.info("Текущее количество users: {}", userService.getUsers().size());
        return userService.getUsers();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        log.info("Get user by id = {}", id);
        ValidateService.validateId(id);
        return userService.getUserById(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        return userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User deleteFriendById(@PathVariable Long id, @PathVariable Long friendId) {
        return userService.deleteFriendById(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriendById(@PathVariable Long id) {
        return userService.getFriendsList(id);
    }

    @GetMapping("/{id}/friends/common/{friendId}")
    public List<User> getCommonFriends(@PathVariable Long id, @PathVariable Long friendId) {
        return userService.getCommonFriends(id, friendId);
    }
}
