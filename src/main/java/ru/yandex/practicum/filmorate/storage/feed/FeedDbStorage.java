package ru.yandex.practicum.filmorate.storage.feed;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventTypes;
import ru.yandex.practicum.filmorate.model.Operations;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collection;


@Slf4j
@Component("feedDbStorage")
@Primary
public class FeedDbStorage implements FeedStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FeedDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

    }

    public Event mapRowFeed(ResultSet rs, int rowNum) throws SQLException {
        LocalDate birthday;

        return Event.builder()
                .eventId(rs.getLong("EVENT_ID"))
                .userId(rs.getLong("USER_ID"))
                .entityId(rs.getLong("ENTITY_ID"))
                .eventType(EventTypes.valueOf(rs.getString("EVENT_TYPE")))
                .operation(Operations.valueOf(rs.getString("OPERATION")))
                .timestamp(rs.getLong("TIMESTAMP"))
                .build();
    }

    @Override
    public Collection<Event> getFeedById(long userId) {
        final String sql = "SELECT * FROM EVENTS WHERE USER_ID = ?";
        log.info("Лента событий пользователя с id {} :", userId);
        return jdbcTemplate.query(sql, this::mapRowFeed, userId);
    }

    @Override
    public Event addEvent(long userId, EventTypes eventType, Operations operation, long entityId) {
        Event event = Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .eventType(eventType)
                .operation(operation)
                .entityId(entityId)
                .eventId(0L)
                .build();

        final String sql = "INSERT INTO EVENTS (TIMESTAMP, USER_ID, EVENT_TYPE, OPERATION, ENTITY_ID) " +
                "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement p = connection.prepareStatement(sql, new String[]{"EVENT_ID"});
            p.setLong(1, event.getTimestamp());
            p.setLong(2, event.getUserId());
            p.setString(3, event.getEventType().toString());
            p.setString(4, event.getOperation().toString());
            p.setLong(5, event.getEntityId());
            return p;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            event.setEventId(Long.parseLong(String.valueOf(keyHolder.getKey())));
        }
        log.info("Создано событие с id: {} ", event.getEventId());
        return event;
    }


}