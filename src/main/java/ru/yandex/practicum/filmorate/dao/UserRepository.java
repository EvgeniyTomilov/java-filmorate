package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
@Slf4j
public class UserRepository {
    private long generatorId;
    private Map<Long, User> users = new HashMap<>();

    public long generateId (){
        return  ++generatorId;
    }

    public User save (User user){
        user.setId(generateId());
        users.put(user.getId(), user);
        return user;
    }

    public User update (User user) {
        if (users.containsKey(user.getId())) {
            users.replace(user.getId(), user);
            log.info("update user: {}", user);
        }else {
            throw new RuntimeException();
        }
        return user;
    }

    public List<User> getUsers() {
        return new ArrayList<User>(users.values());
    }
}


