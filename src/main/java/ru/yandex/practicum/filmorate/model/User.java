package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class User {
    private Long id;
    private String email;
    private String login;
    private final String name;
    private final LocalDate birthday;
}
