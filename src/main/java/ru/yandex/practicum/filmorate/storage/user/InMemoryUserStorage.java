package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class InMemoryUserStorage  implements UserStorage {
    private long generatorId;
    private Map<Long, User> users = new HashMap<>();

    @Override
    public long generateId() {
        return ++generatorId;
    }

    @Override
    public User save(User user) {
        user.setId(generateId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        if (users.containsKey(user.getId())) {
            users.replace(user.getId(), user);
            log.info("update user: {}", user);
        } else {
            throw new RuntimeException();
        }
        return user;
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<User>(users.values());
    }
}


