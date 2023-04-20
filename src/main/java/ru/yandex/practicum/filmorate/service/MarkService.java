package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.IncorrectObjectIdException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarkService {

    private final LikesStorage likesStorage;
    private final GenreStorage genreStorage;
    private final DirectorStorage directorStorage;
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

    public List<Film> getAllWithMarks(int size) {
        List<Film> result = markStorage.findAllFilms(size);
        addDataFilms(result);
        log.info("Found {} movie(s).", result.size());
        return result;
    }

    public Film findById(Long filmId) {
        Optional<Film> result = markStorage.findById(filmId);
        if (result.isEmpty()) {
            log.warn("Film {} is not found.", filmId);
            throw new IncorrectObjectIdException(String.format("Film %d is not found.", filmId));
        }
        addDataFilms(List.of(result.get()));
        log.info("Film {} is found.", result.get().getId());
        return result.get();
    }

    public Map<Long, Map<Long, Double>> getDataForRecommendations() {
        return markStorage.findDataForRecommendations();
    }

    private void addDataFilms(Collection<Film> films) {
        Map<Long, Film> filmsMap = films
                .stream()
                .collect(Collectors.toMap(Film::getId, Function.identity()));
        Map<Long, Set<Genre>> genresMap = genreStorage.findByFilms(filmsMap.keySet());
        Map<Long, Set<Long>> likesMap = likesStorage.findByFilms(filmsMap.keySet());
        Map<Long, Set<Director>> directorsMap = directorStorage.findByFilms(filmsMap.keySet());
        films.forEach(film -> {
            film.setGenres(new HashSet<>());
            film.setLikes(new HashSet<>());
            film.setDirectors(new HashSet<>());
            if (Objects.requireNonNull(genresMap).containsKey(film.getId())) {
                film.setGenres(genresMap.get(film.getId()));
            }
            if (Objects.requireNonNull(likesMap).containsKey(film.getId())) {
                film.setLikes(likesMap.get(film.getId()));
            }
            if (Objects.requireNonNull(directorsMap).containsKey(film.getId())) {
                film.setDirectors(directorsMap.get(film.getId()));
            }
        });
    }
}

