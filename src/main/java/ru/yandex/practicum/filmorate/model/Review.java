package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@Validated
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
