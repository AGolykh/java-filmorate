package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Mark;

import java.util.Optional;

public interface MarkStorage {

    Optional<Mark> find(Long filmId, Long userId);

    Optional<Mark> create(Long filmId, Long userId, Integer value);

    Optional<Mark> update(Long filmId, Long userId, Integer value);

    Optional<Mark> remove(Long filmId, Long userId);

    Boolean isExistMark(Long filmId, Long userId);
}
