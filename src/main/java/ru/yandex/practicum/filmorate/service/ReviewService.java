package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.IncorrectObjectIdException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final EventService eventService;
    private final ExistService existService;

    public Review createReview(Review review) {
        existService.assertUserExists(review.getUserId());
        existService.assertFilmExists(review.getFilmId());
        existService.assertReviewNotExists(review.getUserId(), review.getFilmId());
        Optional<Review> result = reviewStorage.createReview(review);
        if (result.isEmpty()) {
            log.warn("Review of user: {} for film: {} is not created.", review.getUserId(), review.getFilmId());
            throw new IncorrectObjectIdException(String.format("Review of user %d for film %d is not created.",
                    review.getUserId(), review.getFilmId()));
        }
        Review rev = result.get();
        log.info("Добавление отзыва. Пользователь {} Отзыв: {}", review.getUserId(), review.getReviewId());

        addFeed(rev.getUserId(), rev.getReviewId(), Operation.ADD);
        return result.get();
    }

    public Review updateReview(Review review) {
        existService.assertUserExists(review.getUserId());
        existService.assertFilmExists(review.getFilmId());
        existService.assertReviewExists(review.getReviewId());
        log.info("Обновление отзыва. Пользователь {} Отзыв: {}", review.getUserId(), review.getReviewId());

        addFeed(reviewStorage.findReviewById(review.getReviewId()).orElseThrow().getUserId(),
                review.getReviewId(),
                Operation.UPDATE);
        return reviewStorage.updateReview(review).orElseThrow();
    }

    public void deleteReview(Long reviewId) {
        existService.assertReviewExists(reviewId);
        Review review = findReviewById(reviewId);
        Optional<Review> result = reviewStorage.deleteReview(reviewId);
        if (result.isPresent()) {
            log.warn("Review with id: {} is not deleted.", reviewId);
            throw new IncorrectObjectIdException(String.format("Review with id: %d is not deleted.", reviewId));
        }

        log.info("Удаление отзыва. Пользователь {} Отзыв: {}", review.getUserId(), review.getReviewId());
        addFeed(review.getUserId(), review.getReviewId(), Operation.REMOVE);
    }

    public Review findReviewById(Long reviewId) {
        Optional<Review> result = reviewStorage.findReviewById(reviewId);
        if (result.isEmpty()) {
            log.warn("Review id: {} not found.", reviewId);
            throw new IncorrectObjectIdException(String.format("Review id: %d not found.", reviewId));
        }
        return result.get();
    }

    public List<Review> findAllReviews(Long filmId, Integer count) {
        if (filmId != null) {
            existService.assertFilmExists(filmId);
            return reviewStorage.findAllReviewsByFilmId(filmId, count);
        }
        return reviewStorage.findAllReviews(count);
    }

    public void addReviewMark(Long reviewId, Long userId, Boolean isLike) {
        existService.assertReviewExists(reviewId);
        existService.assertUserExists(userId);
        existService.assertReviewMarkNotExists(reviewId, userId, isLike);
        if (reviewStorage.findReviewMark(reviewId, userId, !(isLike)).isPresent()) {
            updateReviewMark(reviewId, userId, isLike);
            return;
        }
        Optional<ReviewMark> result = reviewStorage.createReviewMark(reviewId, userId, isLike);
        if (result.isEmpty()) {
            log.warn("Mark of review id: {} from user id: {} with value {} is not created.",
                    reviewId, userId, isLike);
            throw new IncorrectObjectIdException(String.format(
                    "Mark of review id: %d from user id: %d with value %b is not created.",
                    reviewId, userId, isLike));
        }
    }

    public void updateReviewMark(Long reviewId, Long userId, Boolean isLike) {
        existService.assertReviewExists(reviewId);
        existService.assertUserExists(userId);
        existService.assertReviewMarkNotExists(reviewId, userId, isLike);
        Optional<ReviewMark> result = reviewStorage.updateReviewMark(reviewId, userId, isLike);
        if (result.isEmpty()) {
            log.warn("Mark of review id: {} from user id: {} with value {} is not updated.",
                    reviewId, userId, isLike);
            throw new IncorrectObjectIdException(String.format(
                    "Mark of review id: %d from user id: %d with value %b is updates.",
                    reviewId, userId, isLike));
        }
    }

    public void deleteReviewMark(Long reviewId, Long userId, Boolean isLike) {
        existService.assertReviewMarkExists(reviewId, userId, isLike);
        Optional<ReviewMark> result = reviewStorage.removeReviewMark(reviewId, userId, isLike);

        if (result.isPresent()) {
            log.warn("Mark of review id: {} from user id: {} with value {} is not deleted.",
                    reviewId, userId, isLike);
            throw new IncorrectObjectIdException(String.format(
                    "Mark of review id: %d from user id: %d with value %b is not deleted.",
                    reviewId, userId, isLike));
        }
    }

    private void addFeed(Long userId, Long reviewId, Operation operation) {
        eventService.addEvent(userId, reviewId, EventType.REVIEW, operation);
    }
}
