package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Review {

    @Positive(message = "Идентификатор отзыва не может быть отрицательным.")
    Long reviewId;
    @NotBlank
    String content;
    @JsonProperty("isPositive")
    @NotNull(message = "Тип отзыва не может быть null.")
    Boolean positive;
    @NotNull
    Long userId;
    @NotNull
    Long filmId;
    Long useful;
}
