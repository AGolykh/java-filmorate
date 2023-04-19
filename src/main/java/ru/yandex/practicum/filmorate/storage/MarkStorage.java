package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Mark;

public interface MarkStorage {

    Mark find(Long filmId, Long userId);

    Mark create(Long filmId, Long userId, Integer value);

    Mark update(Long filmId, Long userId, Integer value);

    void remove(Long filmId, Long userId, Integer value);

    Boolean isExistMark(Long filmId, Long userId);

}
