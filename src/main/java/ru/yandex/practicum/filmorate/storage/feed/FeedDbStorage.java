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
@Component("FeedDbStorage")
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
                .eventId(rs.getLong("event_id"))
                .userId(rs.getLong("user_id"))
                .entityId(rs.getLong("entity_id"))
                .eventType(EventTypes.valueOf(rs.getString("event_type")))
                .operation(Operations.valueOf(rs.getString("operation")))
                .timestamp(rs.getLong("timestamps"))
                .build();
    }

    @Override
    public Collection<Event> getFeedById(long userId) {
        final String sql = "SELECT * FROM events WHERE user_id = ?";
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

        final String sql = "INSERT INTO events (timestamp, user_id, event_type, operation, entity_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement p = connection.prepareStatement(sql, new String[]{"event_id"});
            p.setLong(1, event.getTimestamp());
            p.setLong(2, event.getUserId());
            p.setString(3, event.getEventType().toString());
            p.setString(4, event.getOperation().toString());
            p.setLong(5, event.getEntityId());
            return p;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            event.setEventId((Long) keyHolder.getKey());
        }
        log.info("Создано событие с id: {} ", event.getEventId());
        return event;
    }


}