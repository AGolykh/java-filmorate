package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.*;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class MarkServiceTest {

    private final FilmService filmService;
    private final UserService userService;
    private final MarkService markService;
    private final RecommendationService recommendationService;
    private final JdbcTemplate jdbcTemplate;

    private Film film1, film2, film3, film4, film5, film6;
    private User user1, user2, user3, user4, user5, user6;
    private Long user1Id, user2Id, user3Id, user4Id, user5Id, user6Id;
    private Long film1Id, film2Id, film3Id, film4Id, film5Id, film6Id;

    @BeforeEach
    void createInitialData() {
        jdbcTemplate.update("DELETE FROM FILM_MARKS;");
        jdbcTemplate.update("DELETE FROM FILM_GENRES;");

        jdbcTemplate.update("DELETE FROM FILMS;");
        jdbcTemplate.execute("ALTER TABLE FILMS ALTER COLUMN FILM_ID RESTART WITH 1;");

        jdbcTemplate.update("DELETE FROM USERS;");
        jdbcTemplate.execute("ALTER TABLE USERS ALTER COLUMN USER_ID RESTART WITH 1;");

        createEntities();
        film1 = filmService.create(film1);
        film1Id = film1.getId();
        film2 = filmService.create(film2);
        film2Id = film2.getId();
        film3 = filmService.create(film3);
        film3Id = film3.getId();

        user1 = userService.create(user1);
        user1Id = user1.getId();
        user2 = userService.create(user2);
        user2Id = user2.getId();
        user3 = userService.create(user3);
        user3Id = user3.getId();
        user4 = userService.create(user4);
        user4Id = user4.getId();
        user5 = userService.create(user5);
        user5Id = user5.getId();
        user6 = userService.create(user6);
        user6Id = user6.getId();
    }

    @Test
    void addMark_addMarksToFilm_added3FilmsAndUsers() {
        markService.addMark(film1Id, user1Id, 10);
        markService.addMark(film1Id, user2Id, 7);
        markService.addMark(film1Id, user3Id, 3);
        Film filmWithMark = markService.findById(film1Id);
        assertThat(filmWithMark.getRate()).isEqualTo((10.0 + 7.0 + 3.0) / 3.0);
    }

    @Test
    void updateMark_add2MarksToFilm_added3FilmsAndUsers() {
        markService.addMark(film1Id, user1Id, 10);
        markService.addMark(film1Id, user2Id, 9);
        assertThat(markService.findById(film1Id).getRate()).isEqualTo((10.0 + 9.0) / 2.0);
        markService.addMark(film1Id, user1Id, 7);
        assertThat(markService.findById(film1Id).getRate()).isEqualTo((7.0 + 9.0) / 2.0);
    }

    @Test
    void deleteMark_add2MarksToFilm_added3FilmsAndUsers() {
        markService.addMark(film1Id, user1Id, 10);
        markService.addMark(film1Id, user2Id, 9);
        assertThat(markService.findById(film1Id).getRate()).isEqualTo((10.0 + 9.0) / 2.0);
        markService.deleteMark(film1Id, user1Id);
        assertThat(markService.findById(film1Id).getRate()).isEqualTo((9.0));
    }

    @Test
    void getAllWithMarks_add3MarksToEveryFilm_added3FilmsAndUsers() {
        markService.addMark(film1Id, user1Id, 3);
        markService.addMark(film1Id, user2Id, 5);
        markService.addMark(film1Id, user3Id, 6);

        markService.addMark(film2Id, user1Id, 5);
        markService.addMark(film2Id, user2Id, 7);
        markService.addMark(film2Id, user3Id, 8);

        markService.addMark(film3Id, user1Id, 7);
        markService.addMark(film3Id, user2Id, 9);
        markService.addMark(film3Id, user3Id, 10);

        Film film1WithMarks1 = markService.findById(film1Id);
        Film film1WithMarks2 = markService.findById(film2Id);
        Film film1WithMarks3 = markService.findById(film3Id);

        assertThat(film1WithMarks1.getRate()).isEqualTo((6.0 + 5.0 + 3.0) / 3.0);
        assertThat(film1WithMarks2.getRate()).isEqualTo((8.0 + 7.0 + 5.0) / 3.0);
        assertThat(film1WithMarks3.getRate()).isEqualTo((10.0 + 9.0 + 7.0) / 3.0);

        List<Film> collection = markService.getAllWithMarks(10);
        assertThat(collection.size()).isEqualTo(3);
        assertThat(collection).asList().containsAnyOf(
                markService.findById(3L),
                markService.findById(2L),
                markService.findById(1L));
    }

    void getRecommendation() {
        markService.addMark(film1Id, user1Id, 3);
        markService.addMark(film1Id, user2Id, 5);
        markService.addMark(film1Id, user3Id, 6);

        markService.addMark(film2Id, user1Id, 5);
        markService.addMark(film2Id, user2Id, 7);
        markService.addMark(film2Id, user3Id, 8);

        markService.addMark(film3Id, user1Id, 7);
        markService.addMark(film3Id, user2Id, 9);
        markService.addMark(film3Id, user3Id, 10);

        Film film1WithMarks1 = markService.findById(film1Id);
        Film film1WithMarks2 = markService.findById(film2Id);
        Film film1WithMarks3 = markService.findById(film3Id);

        assertThat(film1WithMarks1.getRate()).isEqualTo((6.0 + 5.0 + 3.0) / 3.0);
        assertThat(film1WithMarks2.getRate()).isEqualTo((8.0 + 7.0 + 5.0) / 3.0);
        assertThat(film1WithMarks3.getRate()).isEqualTo((10.0 + 9.0 + 7.0) / 3.0);

        List<Film> collection = markService.getAllWithMarks(10);
        assertThat(collection.size()).isEqualTo(3);
        assertThat(collection).asList().containsAnyOf(
                markService.findById(3L),
                markService.findById(2L),
                markService.findById(1L));
    }


    private void createEntities() {
        film1 = Film.builder()
                .name("Гладиатор")
                .description("Исторический художественный фильм режиссёра Ридли Скотта")
                .releaseDate(LocalDate.of(2000, 5, 1))
                .duration(155)
                .mpa(new Mpa(4L, "R"))
                .genres(Set.of(new Genre(2L, "Драма"), new Genre(6L, "Боевик")))
                .directors(new HashSet<>())
                .build();

        film2 = Film.builder()
                .name("Властелин колец: Братство Кольца")
                .description("Power can be held in the smallest of things...")
                .releaseDate(LocalDate.of(2001, 12, 10))
                .duration(178)
                .mpa(new Mpa(3L, "PG-13"))
                .genres(Set.of(new Genre(1L, "Комедия"), new Genre(2L, "Драма")))
                .directors(new HashSet<>())
                .build();

        film3 = Film.builder()
                .name("Служебный Роман")
                .description("Комедия Эльдара Рязанова, классика советского кино")
                .releaseDate(LocalDate.of(1977, 10, 26))
                .duration(159)
                .mpa(new Mpa(1L, "G"))
                .genres(Set.of(new Genre(1L, "Комедия"), new Genre(5L, "Документальный")))
                .directors(new HashSet<>())
                .build();

        film1 = Film.builder()
                .name("Гладиатор")
                .description("Исторический художественный фильм режиссёра Ридли Скотта")
                .releaseDate(LocalDate.of(2000, 5, 1))
                .duration(155)
                .mpa(new Mpa(4L, "R"))
                .genres(Set.of(new Genre(2L, "Драма"), new Genre(6L, "Боевик")))
                .directors(new HashSet<>())
                .build();

        film2 = Film.builder()
                .name("Властелин колец: Братство Кольца")
                .description("Power can be held in the smallest of things...")
                .releaseDate(LocalDate.of(2001, 12, 10))
                .duration(178)
                .mpa(new Mpa(3L, "PG-13"))
                .genres(Set.of(new Genre(1L, "Комедия"), new Genre(2L, "Драма")))
                .directors(new HashSet<>())
                .build();

        film3 = Film.builder()
                .name("Служебный Роман")
                .description("Комедия Эльдара Рязанова, классика советского кино")
                .releaseDate(LocalDate.of(1977, 10, 26))
                .duration(159)
                .mpa(new Mpa(1L, "G"))
                .genres(Set.of(new Genre(1L, "Комедия"), new Genre(5L, "Документальный")))
                .directors(new HashSet<>())
                .build();

        user1 = User.builder()
                .email("anton@yandex.ru")
                .login("Anton")
                .name("Антон")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        user2 = User.builder()
                .email("dasha@yandex.ru")
                .login("Dasha")
                .name("Дарья")
                .birthday(LocalDate.of(1995, 2, 10))
                .build();

        user3 = User.builder()
                .email("ivan@yandex.ru")
                .login("Ivan")
                .name("Иван")
                .birthday(LocalDate.of(2000, 5, 25))
                .build();

        user4 = User.builder()
                .email("sasha@yandex.ru")
                .login("Sasha")
                .name("Саша")
                .birthday(LocalDate.of(2000, 5, 25))
                .build();

        user5 = User.builder()
                .email("viktor@yandex.ru")
                .login("Viktor")
                .name("Виктор")
                .birthday(LocalDate.of(2000, 5, 25))
                .build();

        user6 = User.builder()
                .email("tolya@yandex.ru")
                .login("Tolya")
                .name("Толя")
                .birthday(LocalDate.of(2000, 5, 25))
                .build();
    }
}