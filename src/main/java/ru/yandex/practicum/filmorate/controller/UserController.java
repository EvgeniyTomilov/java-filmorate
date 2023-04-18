package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dao.UserRepository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.ValidateService;

import java.util.List;

@RestController
@RequestMapping("users")
@Slf4j
public class UserController {
    final ValidateService validateService;
    UserRepository repository;

    public UserController(ValidateService validateService, UserRepository repository) {
        this.validateService = validateService;
        this.repository = repository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@RequestBody User user) {
        log.info("create user: {}", user);
        validateService.validateUser(user);
        return repository.save(user);

    }

    @PutMapping
    public User update(@RequestBody User user) {
        validateService.validateUser(user);
        return repository.update(user);
    }

    @GetMapping
    public List<User> getUsers() {
        log.info("Текущее количество users: {}", repository.getUsers().size());
        return repository.getUsers();
    }


}
