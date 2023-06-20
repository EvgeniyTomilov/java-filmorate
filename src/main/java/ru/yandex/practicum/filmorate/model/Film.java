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
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class Film {

    private Long id;

    @NotBlank(message = "name не может быть пустым ")
    private String name;

    @Size(message = "максимальная длина описания — 200 символов", max = 200)
    private String description;

    @AfterBirthOfCinema(message = "дата релиза — не раньше 28 декабря 1895")
    private LocalDate releaseDate;

    @Positive(message = "продолжительность фильма  не может быть отрицательным значением")
    private int duration;

    private MPA mpa;

    private int rate;

    private Integer likes;
    private LinkedHashSet<Genre> genres;
    private List<Director> directors;
}
