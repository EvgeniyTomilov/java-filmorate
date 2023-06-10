package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.*;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final HashMap<Long, User> userHashMap = new HashMap<>();
    private Long id = 0L;

    @Override
    public User add(@Valid User user) {
        if (user != null) {
            user.setId(++id);
            userHashMap.put(user.getId(), user);
            log.info("Пользователь добавлен");
            return user;
        }
        log.info("Объект пользователь был null");
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public Optional<User> update(@Valid User user) {
        if (user != null) {
            if (userHashMap.containsKey(user.getId())) {
                userHashMap.put(user.getId(), user);
                log.info("Пользователь обновлен");
                return Optional.of(user);
            }
            log.info("Пользователь " + user.getId() + " не найден");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        log.info("Объект пользователь был null");
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public Optional<User> getById(Long id) {
        if (userHashMap.containsKey(id)) {
            return Optional.ofNullable(userHashMap.get(id));
        }
        log.info("Пользователь " + id + " не найден");
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @Override
    public void delete(Long id) {
        if (userHashMap.containsKey(id)) {
            userHashMap.remove(id);
        } else {
            log.info("Пользователь " + id + " не найден");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public Collection<User> getAll() {
        return new ArrayList<>(userHashMap.values());
    }


    @Override
    public Map<Long, User> getUsersMap() {
        return userHashMap;
    }

    @Override
    public Collection<Film> getRecommendations(Long id) {
        return null;
    }

    @Override
    public void addFriend(Long userId, Long idFriend) {
        User user = userHashMap.get(userId);
        user.addFriend(idFriend);
        User friend = userHashMap.get(idFriend);
        friend.addFriend(userId);
    }

    @Override
    public void removeFriend(Long userId, Long idFriend) {
        User user = userHashMap.get(userId);
        user.removeFriend(idFriend);
        User friend = userHashMap.get(idFriend);
        friend.removeFriend(userId);
    }

    @Override
    public List<User> getFriends(Long id) {
        User user = userHashMap.get(id);
        Set<Long> friendsIds = user.getFriends();

        List<User> friends = new ArrayList<>();

        for (Long friendsId : friendsIds) {
            friends.add(userHashMap.get(friendsId));
        }
        return friends;
    }

    @Override
    public List<User> getCommonFriends(Long firstUserId, Long secondUserId) {
        User firstUser = userHashMap.get(firstUserId);
        User secondUser = userHashMap.get(secondUserId);

        Set<Long> firstUserFriends = firstUser.getFriends();
        Set<Long> secondUserFriends = secondUser.getFriends();

        if (isNullOrEmpty(firstUserFriends) || isNullOrEmpty(secondUserFriends)) {
            return Collections.emptyList();
        }

        List<Long> commonFriendIds = new ArrayList<>(firstUserFriends);
        commonFriendIds.retainAll(secondUserFriends);
        if (commonFriendIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<User> commonFiends = new ArrayList<>();
        for (Long friendId : commonFriendIds) {
            User friend = userHashMap.get(friendId);
            if (Objects.nonNull(friend)) {
                commonFiends.add(friend);
            }
        }
        return commonFiends;
    }

    private boolean isNullOrEmpty(Set<Long> friendIds) {
        return Objects.isNull(friendIds) || friendIds.isEmpty();
    }
}
