package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.IncorrectObjectIdException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DirectorTests {
    private final DirectorService directorService;
    private final FilmService filmService;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    void beforeEach() {
        jdbcTemplate.update("DELETE FROM LIKES;");
        jdbcTemplate.update("DELETE FROM FILM_GENRES;");
        jdbcTemplate.update("DELETE FROM FILM_DIRECTORS;");
        jdbcTemplate.update("DELETE FROM USERS;");
        jdbcTemplate.execute("ALTER TABLE USERS ALTER COLUMN USER_ID RESTART WITH 1;");
        jdbcTemplate.update("DELETE FROM DIRECTORS;");
        jdbcTemplate.execute("ALTER TABLE DIRECTORS ALTER COLUMN DIRECTOR_ID RESTART WITH 1;");
        jdbcTemplate.update("DELETE FROM FILMS;");
        jdbcTemplate.execute("ALTER TABLE FILMS ALTER COLUMN FILM_ID RESTART WITH 1;");
    }

    @Test
    void getAllDirectors_return0Director_withoutData() {
        Collection<Director> result = directorService.getAll();
        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    void getAllDirectors_return0Director_added2Directors() {
        addDataDirectors();
        Collection<Director> result = directorService.getAll();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).asList().contains(new Director(1L, "Ivanov Ivan"));
        assertThat(result).asList().contains(new Director(2L, "Petrov Petr"));
    }

    @Test
    void getByIdDirector_returnSecondDirector_added2Directors() {
        addDataDirectors();
        Director result = directorService.getById(2L);
        assertThat(result).isEqualTo(new Director(2L, "Petrov Petr"));
    }

    @Test
    void getByIdDirector_throwIncorrectParameterException_withoutData() {
        addDataDirectors();
        final IncorrectObjectIdException e = assertThrows(IncorrectObjectIdException.class,
                () -> directorService.getById(3L));
        assertEquals("Director 3 is not found.", e.getMessage());
    }

    @Test
    void createDirector_returnNewDirector_withoutData() {
        Director result = directorService.createDirector(new Director(10L, "Ivanov Ivan"));
        assertThat(result).isEqualTo(new Director(1L, "Ivanov Ivan"));
    }

    @Test
    void updateDirector_returnUpdatedDirector_added2Directors() {
        addDataDirectors();
        Director result = directorService.updateDirector(new Director(1L, "Sidorov Ivan"));
        assertEquals(result.getName(), "Sidorov Ivan");
    }

    @Test
    void updateDirector_throwIncorrectParameterException_withoutData() {
        addDataDirectors();
        final IncorrectObjectIdException e = assertThrows(IncorrectObjectIdException.class,
                () -> directorService.updateDirector(new Director(3L, "Sidorov Ivan")));
        assertEquals("Director id: 3 not found.", e.getMessage());
    }

    @Test
    void deleteDirector_return1Director_added2Directors() {
        addDataDirectors();
        assertThat(directorService.getAll()).asList().size().isEqualTo(2);
        directorService.deleteDirector(1L);
        assertThat(directorService.getAll()).asList().size().isEqualTo(1);
        assertThat(directorService.getAll()).asList().contains(new Director(2L, "Petrov Petr"));
    }

    @Test
    void deleteDirector_throwIncorrectParameterException_withoutData() {
        final IncorrectObjectIdException e = assertThrows(IncorrectObjectIdException.class,
                () -> directorService.deleteDirector(1L));
        assertEquals("Director id: 1 not found.", e.getMessage());
    }

    @Test
    void getFilmDirectorSorted_returnSortedByYearAndLikesFilms_added2DirectorsAnd5Films() {
        addDataDirectors();
        addDataFilms();
        Collection<Film> result = filmService.getFilmDirectorSorted(1L, "year");
        assertThat(result).asList().size().isEqualTo(2);
        assertEquals(result.stream().findFirst().get().getReleaseDate(),
                LocalDate.of(1994, 9, 24));

        filmService.like(2L, 1L);
        filmService.like(2L, 2L);
        result = filmService.getFilmDirectorSorted(1L, "likes");
        assertEquals(result.stream().findFirst().get().getId(), 2);
    }

    private void addDataDirectors() {
        jdbcTemplate.update("INSERT INTO DIRECTORS (DIRECTOR_NAME) " +
                "VALUES ('Ivanov Ivan'), ('Petrov Petr')");
    }

    private void addDataFilms() {
        filmService.create(Film.builder()
                .name("Побег из Шоушенка")
                .duration(142)
                .releaseDate(LocalDate.of(1994, 9, 24))
                .mpa(new Mpa(2L, "PG"))
                .genres(new HashSet<>(List.of(new Genre(2L, "Драма"))))
                .description("Бухгалтер Энди Дюфрейн обвинён в убийстве собственной жены")
                .directors(new HashSet<>(List.of(new Director(1L, "Ivanov Ivan")))).build());

        filmService.create(Film.builder()
                .name("Карты, деньги, два ствола")
                .duration(142)
                .releaseDate(LocalDate.of(1998, 9, 24))
                .mpa(new Mpa(2L, "PG"))
                .genres(new HashSet<>(List.of(new Genre(1L, "Комедия"))))
                .description("Стейтем не бьет морды")
                .directors(new HashSet<>(List.of(new Director(1L, "Ivanov Ivan")))).build());

        jdbcTemplate.update("INSERT INTO USERS (EMAIL, LOGIN, USER_NAME, BIRTHDAY ) " +
                "VALUES ('email@yandex.ru', 'trulala', 'Trexo', '2011-03-08')," +
                "('ema@mail.ru', 'login', 'Name', '2001-06-05')");
    }
}
