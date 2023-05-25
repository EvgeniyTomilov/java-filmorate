package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenreService {
    private final GenreService genreService;
    public Genre getGenreById(Integer genreId) {
        if (genreStorage.getGenreById(genreId) == null) {
            log.info("Жанр с id " + genreId + " не найден");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return genreStorage.getGenreById(genreId);
    }

    public List<Genre> getAllGenre() {
        return genreStorage.getAllGenres();
    }
}
