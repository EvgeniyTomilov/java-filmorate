package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
public class Review {

    private Long reviewId;
    private Long filmId;
    private Long userId;
    @NotBlank
    private String content;
    @JsonSetter("isPositive")
    @NonNull
    private Boolean isPositive;
    private int useful;
}
