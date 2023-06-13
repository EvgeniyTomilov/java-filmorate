package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface UserStorage extends Storage<User> {

    void addFriend(Long userId, Long idFriend);

    void removeFriend(Long id, Long idRemoveFriend);

    List<User> getFriends(Long id);

    List<User> getCommonFriends(Long id, Long otherId);

    Map<Long, User> getUsersMap();

    Collection<Film> getRecommendations(Long id);

    void isExist(int userId);




}
