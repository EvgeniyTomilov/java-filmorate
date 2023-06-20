package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.error.exception.NullException;
import ru.yandex.practicum.filmorate.error.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.likes.LikesDbStorage;

import java.sql.Date;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final LikesDbStorage likesDbStorage;
    private final FilmDbStorage filmDbStorage;

    private User rowMapToUser(ResultSet resultSet, int i) throws SQLException {
        LocalDate birthday;
        if (resultSet.getDate("birthday") == null) {
            birthday = LocalDate.of(0, 1, 1);
        } else {
            birthday = resultSet.getDate("birthday").toLocalDate();
        }
        return User.builder()
                .id(resultSet.getLong("id"))
                .email((resultSet.getString("email")))
                .login(resultSet.getString("login"))
                .name(resultSet.getString("name"))
                .birthday(birthday)
                .build();
    }

    @Override
    public User add(User user) {
        if (user == null) {
            throw new NullException("Пользователь не может быть null");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        String sqlQueryToUsers = "INSERT INTO users (email, login, name, birthday) VALUES (?,?,?,?)";
        PreparedStatementCreator psc = con -> {
            PreparedStatement ps = con.prepareStatement(sqlQueryToUsers, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf((user.getBirthday())));
            return ps;
        };
        GeneratedKeyHolder gkh = new GeneratedKeyHolder();
        jdbcTemplate.update(psc, gkh);
        user.setId(Objects.requireNonNull(gkh.getKey()).longValue());
        return user;
    }

    @Override
    public Optional<User> update(User user) {
        if (user == null) {
            throw new NullException("Пользователь не может быть null");
        }
        String name;
        if (user.getName() == null || user.getName().isBlank()) {
            name = user.getLogin();
        } else {
            name = user.getName();
        }
        String sqlQueryUpdate = "UPDATE users " +
                "SET email = ?, login = ?, name = ?, birthday = ?" +
                "WHERE id = ?";
        jdbcTemplate.update(sqlQueryUpdate,
                user.getEmail(),
                user.getLogin(),
                name,
                user.getBirthday(),
                user.getId());
        return Optional.of(user);
    }

    @Override
    public void delete(Long id) {
        String sqlQueryDelete = "DELETE FROM users WHERE id = ?";
        try {
            jdbcTemplate.update(sqlQueryDelete, id);
        } catch (EmptyResultDataAccessException e) {
            log.info("В базе нет информации по запросу {}", sqlQueryDelete);
        }
    }

    @Override
    public List<User> getAll() {
        String sqlQueryGetAll = "SELECT * FROM users";
        List<User> users = new ArrayList<>();
        try {
            users = jdbcTemplate.query(sqlQueryGetAll, this::rowMapToUser);
        } catch (EmptyResultDataAccessException e) {
            log.info("В базе нет информации по запросу {}", sqlQueryGetAll);
        }
        return users;
    }

    @Override
    public Optional<User> getById(Long id) {
        String sqlQueryGetById = "SELECT * FROM users WHERE id = ?";
        User user = null;
        try {
            user = jdbcTemplate.queryForObject(sqlQueryGetById, this::rowMapToUser, id);
        } catch (EmptyResultDataAccessException e) {
            log.info("В базе нет информации по запросу {}.  id={}", sqlQueryGetById, id);
            return Optional.empty();
        }
        return Optional.ofNullable(user);
    }

    @Override
    public Map<Long, User> getUsersMap() {
        String sqlQueryUsersMap = "SELECT * FROM users";
        List<User> userMap = jdbcTemplate.query(sqlQueryUsersMap, this::rowMapToUser);
        Map<Long, User> result = new HashMap<>();
        for (User user : userMap) {
            result.put(user.getId(), user);
        }
        return result;
    }

    @Override
    public void addFriend(Long userId, Long idFriend) {
        String sqlQueryAddFriend = "MERGE INTO userFriends(userId, friendsId) VALUES (?,?)";
        jdbcTemplate.update(sqlQueryAddFriend, userId, idFriend);
    }

    @Override
    public void removeFriend(Long id, Long idRemoveFriend) {
        if (id == null || idRemoveFriend == null || id.equals(idRemoveFriend)) {
            return;
        }
        String sqlQueryRemoveFriend = "DELETE FROM userFriends f WHERE f.userId = ? AND f.friendsId = ?";
        try {
            jdbcTemplate.update(sqlQueryRemoveFriend, id, idRemoveFriend);
        } catch (EmptyResultDataAccessException e) {
            log.info("В базе нет информации по запросу {}.  userId={}, friendsId={}",
                    sqlQueryRemoveFriend, id, idRemoveFriend);
        }
    }

    @Override
    public List<User> getFriends(Long id) {
        List<User> friends = new ArrayList<>();
        if (id == null) {
            return friends;
        }
        String sqlQueryGetFriends = "SELECT u.* FROM userFriends f LEFT JOIN users u ON f.friendsId = u.id" +
                " WHERE f.userId = ?";
        try {
            friends = jdbcTemplate.query(sqlQueryGetFriends, this::rowMapToUser, id);
        } catch (EmptyResultDataAccessException e) {
            log.info("В базе нет информации по запросу {}", sqlQueryGetFriends);
        }
        return friends;
    }

    @Override
    public List<User> getCommonFriends(Long id, Long otherId) {
        List<User> commonFriends = new ArrayList<>();
        String sqlQueryCommonFriends = "SELECT u.* FROM userFriends f LEFT JOIN users u on f.friendsId = u.id " +
                " WHERE f.userId= ? AND f.friendsId IN (SELECT ff.friendsId FROM userFriends ff WHERE ff.userId=?)";
        try {
            commonFriends = jdbcTemplate.query(sqlQueryCommonFriends, this::rowMapToUser, id, otherId);
        } catch (EmptyResultDataAccessException e) {
            log.info("В базе нет информации по запросу {}.  userId={}, friendsId={}",
                    sqlQueryCommonFriends, id, otherId);
        }
        return commonFriends;
    }

    @Override
    public Collection<Film> getRecommendations(Long id) {
        Collection<Film> result = new HashSet<>();
        Collection<Integer> resultId = filmDbStorage.getUserRecommendations(id.intValue());
        if (resultId.size() == 0) {
            return result;
        }
        Map<Long, Film> mappedFilms = filmDbStorage.getAll().stream()
                .collect(Collectors.toMap(Film::getId,
                        Function.identity()));
        resultId.forEach(id1 -> {
            result.add(mappedFilms.get(id1.longValue()));
        });
        return result;
    }


    public void isExist(int userId) {
        final String checkUserQuery = "SELECT * FROM users WHERE id = ?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(checkUserQuery, userId);

        if (!userRows.next()) {
            log.warn("Пользователь с идентификатором {} не найден.", userId);
            throw new ObjectNotFoundException("Пользователь с идентификатором " + userId + " не найден.");
        }

    }
}
