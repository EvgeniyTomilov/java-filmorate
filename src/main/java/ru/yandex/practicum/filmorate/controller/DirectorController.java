package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/directors")
public class DirectorController {

    private final DirectorService directorService;

    @GetMapping
    public List<Director> getAllDirectors() {
        log.info("Получение всех режисёров");
        return directorService.getAllDirectors();
    }

    @GetMapping("/{id}")
    public Director get(@PathVariable Long id) {
        log.info("Получение режисёра с id: " + id);
        return directorService.getDirectorById(id);
    }

    @PostMapping
    public Director createDirector(@Valid @RequestBody Director director) {
        log.info("Добавление режисёра");
        return directorService.createDirector(director);
    }

    @PutMapping
    public Director update(@Valid @RequestBody Director director) {
        log.info("Обновление режисёра");
        return directorService.updateDirector(director);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("Удаление режисёра с id: " + id);
        directorService.deleteDirector(id);
    }
}

