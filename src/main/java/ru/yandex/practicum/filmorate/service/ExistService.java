package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.IncorrectObjectIdException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExistService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final ReviewStorage reviewStorage;
    private final DirectorStorage directorStorage;
    private final MarkStorage markStorage;

    public void assertUserExists(Long userId) {
        Optional<User> existedUser = userStorage.findById(userId);
        if (existedUser.isEmpty()) {
            log.warn("User id: {} not found.", userId);
            throw new IncorrectObjectIdException(String.format("User id: %d not found.", userId));
        }
    }

    public void assertFilmExists(Long filmId) {
        Optional<Film> existedFilm = filmStorage.findById(filmId);
        if (existedFilm.isEmpty()) {
            log.warn("Film id: {} not found.", filmId);
            throw new IncorrectObjectIdException(String.format("Film id: %d not found.", filmId));
        }
    }

    public void assertReviewExists(Long reviewId) {
        Optional<Review> existedReview = reviewStorage.findReviewById(reviewId);
        if (existedReview.isEmpty()) {
            log.warn("Review id: {} not found.", reviewId);
            throw new IncorrectObjectIdException(String.format("Review id: %d not found.", reviewId));
        }
    }

    public void assertReviewMarkExists(Long reviewId, Long userId, Boolean isLike) {
        Optional<ReviewMark> existedMark = reviewStorage.findReviewMark(reviewId, userId, isLike);
        if (existedMark.isEmpty()) {
            log.warn("Mark of review id: {} from user id: {} with value {} not found.",
                    reviewId, userId, isLike);
            throw new IncorrectObjectIdException(String.format(
                    "Mark of review id: %d from user id: %d with value %b not found.",
                    reviewId, userId, isLike)
            );
        }
    }

    public void assertReviewNotExists(Long userId, Long filmId) {
        if (reviewStorage.isExistReview(userId, filmId)) {
            log.warn("Film {} already has a review from a user {}.", userId, filmId);
            throw new IncorrectObjectIdException(String.format("Film %d already has a review from a user %d.",
                    userId, filmId));
        }
    }

    public void assertReviewMarkNotExists(Long reviewId, Long userId, Boolean isLike) {
        Optional<ReviewMark> existedMark = reviewStorage.findReviewMark(reviewId, userId, isLike);
        if (existedMark.isPresent()) {
            log.warn("Mark of review id: {} from user id: {} with value {} already created.",
                    reviewId, userId, isLike);
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    String.format("Mark of review id: %d from user id: %d with value %b already created.",
                            reviewId, userId, isLike));
        }
    }


    public void assertDirectorExists(Long directorId) {
        Optional<Director> existedDirector = directorStorage.findById(directorId);
        if (existedDirector.isEmpty()) {
            log.warn("Director id: {} not found.", directorId);
            throw new IncorrectObjectIdException(String.format("Director id: %d not found.", directorId));
        }
    }

    public void assertMarkExists(Long filmId, Long userId) {
        Optional<Mark> existedMark = markStorage.find(filmId, userId);
        if (existedMark.isEmpty()) {
            log.warn("Mark for film id: {} from user id:  {}  not found.", filmId, userId);
            throw new IncorrectObjectIdException(String.format("Mark for film id: %d from user id: %d not found.",
                    filmId, userId));
        }
    }

    public void assertMarkNotExists(Long filmId, Long userId) {
        Optional<Mark> existedMark = markStorage.find(filmId, userId);
        if (existedMark.isPresent()) {
            log.warn("Mark of film id: {} from user id: {} already created.",
                    filmId, userId);
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    String.format("Mark of review id: %d from user id: %d already created.",
                            filmId, userId));
        }
    }
}
