package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

@RestController
@RequestMapping("/users")
public class UserController {

    @PostMapping
    public void create(@RequestBody User user) {

    }

    @PutMapping
    public void update(@RequestBody User user){

    }

    @GetMapping
    public Collection<User> findAll() {
        return userMap.values();
    }



}
