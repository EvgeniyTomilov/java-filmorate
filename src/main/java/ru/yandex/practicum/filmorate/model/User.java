package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.validation.customAnnotation.NoSpaces;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@Validated
public class User {
    private Long id;

    @NotBlank(message = "электронная почта не может быть пустой")
    @Email(message = "электронная почта  должна содержать символ @")
    private String email;

    @NotBlank(message = "login не может быть пустым ")
    @NoSpaces(message = "login не может содержать пробелы")
    private String login;

    private String name;

    @PastOrPresent(message = "дата рождения не может быть в будущем")
    private LocalDate birthday;
}
