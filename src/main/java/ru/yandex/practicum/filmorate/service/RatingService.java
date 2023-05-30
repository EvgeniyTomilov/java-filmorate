package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.rating.RatingStorage;

import java.util.List;
import java.util.Optional;

@Service("RatingService")
@Slf4j
public class RatingService {

    private final RatingStorage ratingStorage;

    @Autowired
    public RatingService(@Qualifier("RatingDbStorage") RatingStorage ratingStorage) {
        this.ratingStorage = ratingStorage;
    }

    public Optional<MPA> getRatingById(Integer id) {
        if (ratingStorage.getRatingById(id).isPresent()) {
            return ratingStorage.getRatingById(id);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    public List<MPA> getRatingAll() {
        return ratingStorage.getRatingAll();
    }
}
