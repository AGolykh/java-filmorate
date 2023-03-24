package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Genre {
    private Long id;
    private String name;

    public Genre(Long genreId) {
        this.id = genreId;
    }

    public Genre(String name) {
        this.name = name;
    }
}