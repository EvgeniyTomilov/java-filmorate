package ru.yandex.practicum.filmorate.model;


import lombok.Data;

import java.time.Duration;
import java.time.LocalDate;

@Data
public class Film {
    private Long Id;
    private final String name;
    private String description;
    private LocalDate releaseDate;
    private Duration duration;
}
