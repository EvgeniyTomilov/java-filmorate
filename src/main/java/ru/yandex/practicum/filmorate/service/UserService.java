package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public User createUser(User user) {
        return userStorage.add(user);
    }

    public User updateUser(User user) {
        if (contains(user.getId())) {
            return userStorage.update(user).get();
        }
        log.info("User с id " + user.getId() + " не найден");
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    public void deleteUser(Long id) {
        if (contains(id)) {
            userStorage.delete(id);
        }
        log.info("User с id " + id + " не найден");
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    public Collection<User> getAllUsers() {
        return userStorage.getAll();
    }

    public User getUserById(Long id) {
        if (contains(id)) {
            return userStorage.getById(id).get();
        }
        log.info("User с id " + id + " не найден");
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    public void addFriend(Long id, Long friendId) {
        if (contains(id)) {
            if (contains(friendId)) {
                userStorage.addFriend(id, friendId);
            } else {
                log.info("Пользователь " + friendId + " не найден");
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
        } else {
            log.info("Пользователь " + id + " не найден");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    public void deleteFriend(Long id, Long friendId) {
        if (contains(id)) {
            if (contains(friendId)) {
                userStorage.removeFriend(id, friendId);
            } else {
                log.info("Пользователь " + friendId + " не найден");
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
        } else {
            log.info("Пользователь " + id + " не найден");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    public Collection<User> getListOfFriends(Long id) {
        if (contains(id)) {
            return userStorage.getFriends(id);
        }
        log.info("Пользователь " + id + " не найден");
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    public Collection<User> getListSharedFriends(Long id, Long otherId) {
        if (contains(id)) {
            if (contains(otherId)) {
                return userStorage.getCommonFriends(id, otherId);
            } else {
                log.info("Пользователь " + otherId + " не найден");
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
        }
        log.info("Пользователь " + id + " не найден");
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    private boolean contains(Long id) {
        return userStorage.getUsersMap().containsKey(id);
    }
}
