package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public User save(User user) {
        return userStorage.save(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public User addFriend(Long id, Long friendId) {
        User user = userStorage.getUserById(id);
        ValidateService.validateId(id);
        ValidateService.validateId(friendId);
        Set<Long> friends = user.getFriends();
        friends.add(friendId);
        User friend = userStorage.getUserById(friendId);
        Set<Long> friends1 = friend.getFriends();
        friends1.add(id);
        userStorage.update(friend);
        return userStorage.update(user);
    }

    public User deleteFriendById(Long id, Long friendId) {
        User user = userStorage.getUserById(id);
        Set<Long> friends = user.getFriends();
        friends.remove(friendId);
        return userStorage.update(user);
    }

    public List<User> getFriendsList(Long id) {
        List<User> users = new ArrayList<>();

        User user = userStorage.getUserById(id);
        Set<Long> friends = user.getFriends();
        for (Long friendId : friends) {
            User friend = userStorage.getUserById(friendId);
            if (Objects.nonNull(friend)) {
                users.add(friend);
            }
        }
        return users;
    }


    public List<User> getCommonFriends(Long firstUserId, Long secondUserId) {
        User firstUser = userStorage.getUserById(firstUserId);
        User secondUser = userStorage.getUserById(secondUserId);

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
            User friend = userStorage.getUserById(friendId);
            if (Objects.nonNull(friend)) {
                commonFiends.add(friend);
            }
        }
        return commonFiends;
    }

    public User getUserById(Long id) {

        return userStorage.getUserById(id);
    }

    private boolean isNullOrEmpty(Set<Long> friendIds) {
        return Objects.isNull(friendIds) || friendIds.isEmpty();
    }
}
