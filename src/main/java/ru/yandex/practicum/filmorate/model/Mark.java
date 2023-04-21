package ru.yandex.practicum.filmorate.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Mark {

    @Positive(message = "Идентификатор фильма не может быть отрицательным.")
    Long filmId;
    @NotNull
    @Positive(message = "Идентификатор пользователя не может быть отрицательным.")
    Long userId;
    @NotNull
    Integer value;
}
