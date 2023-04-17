package ru.yandex.practicum.filmorate.dao;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

@Component
public class UserRepository {
    private long generatorId;

    public long getGeneratorId (){
        return  ++generatorId;
    }

    public void save (User user){
        user.setId(getGeneratorId());
    }
}
