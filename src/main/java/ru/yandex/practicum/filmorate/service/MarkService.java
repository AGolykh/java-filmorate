package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.IncorrectObjectIdException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.MarkStorage;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarkService {

    private final MarkStorage markStorage;
    private final ExistService existService;


    public void addMark(Long filmId, Long userId, Integer value) {
        existService.assertFilmExists(filmId);
        existService.assertUserExists(userId);
        if (markStorage.find(filmId, userId).isPresent()) {
            updateMark(filmId, userId, value);
            return;
        }
        Optional<Mark> result = markStorage.create(filmId, userId, value);
        if (result.isEmpty()) {
            log.warn("Mark of film id: {} from user id: {} with value {} is not created.",
                    filmId, userId, value);
            throw new IncorrectObjectIdException(String.format(
                    "Mark of film id: %d from user id: %d with value %b is not created.",
                    filmId, userId, value));
        }
    }

    public void updateMark(Long filmId, Long userId, Integer value) {
        existService.assertFilmExists(filmId);
        existService.assertUserExists(userId);
        existService.assertMarkNotExists(filmId, userId);
        Optional<Mark> result = markStorage.update(filmId, userId, value);
        if (result.isEmpty()) {
            log.warn("Mark of film id: {} from user id: {} with value {} is not updated.",
                    filmId, userId, value);
            throw new IncorrectObjectIdException(String.format(
                    "Mark of film id: %d from user id: %d with value %b is updates.",
                    filmId, userId, value));
        }
    }

    public void deleteMark(Long filmId, Long userId) {
        existService.assertMarkExists(filmId, userId);
        Optional<Mark> result = markStorage.remove(filmId, userId);

        if (result.isPresent()) {
            log.warn("Mark of review id: {} from user id: {} is not deleted.",
                    filmId, userId);
            throw new IncorrectObjectIdException(String.format(
                    "Mark of review id: %d from user id: %d is not deleted.",
                    filmId, userId));
        }
    }


}

