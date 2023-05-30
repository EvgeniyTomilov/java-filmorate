package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.customAnnotation.AfterBirthOfCinema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.LinkedHashSet;

@Data
@Builder
@AllArgsConstructor
public class Film {

    private Long id;

    @NotBlank(message = "name не может быть пустым или null")
    private String name;

    @Size(message = "количество знаков для description не должно превышать 200", max = 200)
    private String description;

    @AfterBirthOfCinema(message = "releaseDate не может быть до даты появления кино")
    private LocalDate releaseDate;

    @Positive(message = "duration не может быть отрицательным значением")
    private int duration;

    private MPA mpa;

    private double rate;

    private LinkedHashSet<Genre> genres;
}
