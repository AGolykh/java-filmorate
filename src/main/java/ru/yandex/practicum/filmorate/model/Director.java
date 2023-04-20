package ru.yandex.practicum.filmorate.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Director {

    @NotNull
    @Positive(message = "Идентификатор директора не может быть отрицательным.")
    Long id;
    @NotBlank(message = "Имя директора не может быть пустым.")
    String name;
}
