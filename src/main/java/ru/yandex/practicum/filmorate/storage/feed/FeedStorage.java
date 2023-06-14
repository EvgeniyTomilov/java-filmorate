package ru.yandex.practicum.filmorate.storage.feed;

import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventTypes;
import ru.yandex.practicum.filmorate.model.Operations;

import java.util.Collection;

public interface FeedStorage {
    Collection<Event> getFeedById(long userId);

    Event addEvent(long userId, EventTypes eventType, Operations operation, long entityId);

}
