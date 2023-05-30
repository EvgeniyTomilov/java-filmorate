package ru.yandex.practicum.filmorate.model;

import lombok.*;
import ru.yandex.practicum.filmorate.validation.customAnnotation.NoSpaces;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class User {
    private Long id;

    @NotBlank(message = "email не может быть пустым или null")
    @Email(message = "Неправильный формат email")
    private String email;

    @NotBlank(message = "login не может быть пустым или null")
    @NoSpaces(message = "login не может содержать пробелы")
    private String login;

    private String name;

    @PastOrPresent(message = "birthday не может быть в будущем")
    private LocalDate birthday;
}
