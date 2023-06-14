package ru.yandex.practicum.filmorate.error.exception;

public class InvalidSearchParameters extends RuntimeException {
    public InvalidSearchParameters(String s) {
        super(s);
    }
}
