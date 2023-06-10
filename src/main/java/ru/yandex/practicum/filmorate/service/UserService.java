package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class UserService {
    private Set<Long> friends = new HashSet<>();

    @Autowired
    @Qualifier(value = "userDbStorage")
    private UserStorage userStorage;

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
            getAllUsers()
                    .stream()
                    .forEach(user -> {
                        deleteFriend(user.getId(), id);
                        userStorage.getCommonFriends(id, user.getId())
                                .stream()
                                .forEach(userCommon -> {
                                    deleteFriend(userCommon.getId(), id);
                                });
                    });
            userStorage.delete(id);
        } else {
            log.info("User с id " + id + " не найден");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
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

    public Collection<Film> getRecommendations(Long id) {
        return userStorage.getRecommendations(id);
    }
}
