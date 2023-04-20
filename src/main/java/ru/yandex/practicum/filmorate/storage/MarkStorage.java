package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mark;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MarkStorage {

    Optional<Mark> find(Long filmId, Long userId);

    Optional<Mark> create(Long filmId, Long userId, Integer value);

    Optional<Mark> update(Long filmId, Long userId, Integer value);

    Optional<Mark> remove(Long filmId, Long userId);

    List<Film> findAllFilms(int size);

    Optional<Film> findById(Long id);

    Boolean isExistMark(Long filmId, Long userId);

    Map<Long, Map<Long, Double>> findDataForRecommendations();
}
