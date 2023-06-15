package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.service.RatingService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/mpa")
public class RatingController {
    private final RatingService ratingService;

    @GetMapping("/{id}")
    public MPA getMpaById(@PathVariable Integer id) {
        if (ratingService.getRatingById(id).isPresent()) {
            log.info("Получение рейтинга с id: " + id);
            return ratingService.getRatingById(id).get();
        }
        log.error("Рейтинга с таким id: " + id + " не существует");
        return null;
    }

    @GetMapping
    public List<MPA> getMpaAll() {
        log.info("Получение всех рейтингов");
        return ratingService.getRatingAll();
    }
}