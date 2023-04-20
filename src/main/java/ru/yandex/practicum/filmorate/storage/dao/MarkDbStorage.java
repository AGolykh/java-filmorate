package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.MarkStorage;

import javax.sql.DataSource;
import java.util.*;

@Repository("MarkDb")
@RequiredArgsConstructor
public class MarkDbStorage implements MarkStorage {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    static final RowMapper<Mark> markMapper =
            (rs, rowNum) -> new Mark(
                    rs.getLong("FILM_ID"),
                    rs.getLong("USER_ID"),
                    rs.getInt("MARK"));

    static final RowMapper<Film> filmMapper =
            ((rs, rowNum) -> Film.builder()
                    .id(rs.getLong("FILM_ID"))
                    .name(rs.getString("FILM_NAME"))
                    .description(rs.getString("DESCRIPTION"))
                    .releaseDate(rs.getDate("RELEASE_DATE").toLocalDate())
                    .duration(rs.getInt("DURATION"))
                    .rate(rs.getDouble("RATE"))
                    .mpa(new Mpa(rs.getLong("RATING_ID"),
                            rs.getString("RATING_NAME")))
                    .genres(new HashSet<>())
                    .likes(new HashSet<>())
                    .directors(new HashSet<>())
                    .build());

    @Override
    public Optional<Mark> find(Long filmId, Long userId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM FILM_MARKS " +
                            "WHERE FILM_ID = :FILM_ID " +
                            "AND USER_ID = :USER_ID;",
                    new MapSqlParameterSource()
                            .addValue("FILM_ID", filmId)
                            .addValue("USER_ID", userId),
                    markMapper));
        } catch (EmptyResultDataAccessException exc) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Mark> create(Long filmId, Long userId, Integer value) {
        new SimpleJdbcInsert(dataSource)
                .withTableName("FILM_MARKS")
                .execute(getMarkParams(new Mark(filmId, userId, value)));
        return find(filmId, userId);
    }

    @Override
    public Optional<Mark> update(Long filmId, Long userId, Integer value) {
        jdbcTemplate.update(
                "UPDATE FILM_MARKS " +
                        "SET MARK = :MARK " +
                        "WHERE FILM_ID = :FILM_ID " +
                        "AND USER_ID = :USER_ID",
                getMarkParams(new Mark(filmId, userId, value)));
        return find(filmId, userId);
    }

    @Override
    public Optional<Mark> remove(Long filmId, Long userId) {
        jdbcTemplate.update(
                "DELETE FROM FILM_MARKS " +
                        "WHERE FILM_ID = :FILM_ID " +
                        "AND USER_ID = :USER_ID;",
                new MapSqlParameterSource()
                        .addValue("FILM_ID", filmId)
                        .addValue("USER_ID", userId));
        return find(filmId, userId);
    }

    @Override
    public List<Film> findAllFilms(int size) {
        return jdbcTemplate.query(
                "SELECT F.FILM_ID, " +
                        "FILM_NAME, " +
                        "DESCRIPTION, " +
                        "RELEASE_DATE, " +
                        "DURATION, " +
                        "AVG(MARK) RATE, " +
                        "F.RATING_ID, " +
                        "RATING_NAME " +
                        "FROM FILMS F " +
                        "JOIN RATING MPA ON F.RATING_ID = MPA.RATING_ID " +
                        "LEFT OUTER JOIN FILM_MARKS FM ON FM.FILM_ID = F.FILM_ID " +
                        "GROUP BY F.FILM_ID " +
                        "ORDER BY RATE DESC " +
                        "LIMIT :SIZE;",
                new MapSqlParameterSource()
                        .addValue("SIZE", size),
                filmMapper);
    }

    @Override
    public Optional<Film> findById(Long id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT F.FILM_ID, " +
                            "FILM_NAME, " +
                            "DESCRIPTION, " +
                            "RELEASE_DATE, " +
                            "DURATION, " +
                            "AVG(MARK) RATE, " +
                            "F.RATING_ID, " +
                            "RATING_NAME " +
                            "FROM FILMS F " +
                            "JOIN RATING MPA ON F.RATING_ID = MPA.RATING_ID " +
                            "LEFT OUTER JOIN FILM_MARKS FM ON FM.FILM_ID = F.FILM_ID " +
                            "WHERE F.FILM_ID = :FILM_ID " +
                            "GROUP BY F.FILM_ID;",
                    new MapSqlParameterSource()
                            .addValue("FILM_ID", id),
                    filmMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }


    @Override
    public Boolean isExistMark(Long filmId, Long userId) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT * FROM FILM_MARKS " +
                        "WHERE FILM_ID = :FILM_ID " +
                        "AND USER_ID = :USER_ID);",
                new MapSqlParameterSource()
                        .addValue("USER_ID", userId)
                        .addValue("FILM_ID", filmId),
                Boolean.class));
    }

    @Override
    public Map<Long, Map<Long, Double>> findDataForRecommendations() {
        return jdbcTemplate.query(
                "SELECT FM.USER_ID, " +
                        "F.FILM_ID, " +
                        "FILM_NAME, " +
                        "DESCRIPTION, " +
                        "RELEASE_DATE, " +
                        "DURATION, " +
                        "AVG(MARK) RATE, " +
                        "F.RATING_ID, " +
                        "RATING_NAME " +
                        "FROM FILMS F " +
                        "JOIN RATING MPA ON F.RATING_ID = MPA.RATING_ID " +
                        "LEFT OUTER JOIN FILM_MARKS FM ON FM.FILM_ID = F.FILM_ID " +
                        "GROUP BY FM.USER_ID, F.FILM_ID " +
                        "ORDER BY RATE DESC;",
                userMarkExtractor);
    }

    private MapSqlParameterSource getMarkParams(Mark mark) {
        return new MapSqlParameterSource()
                .addValue("FILM_ID", mark.getFilmId())
                .addValue("USER_ID", mark.getUserId())
                .addValue("MARK", mark.getValue());
    }

    static final ResultSetExtractor<Map<Long, Set<Long>>> userLikesExtractor = rs -> {
        Map<Long, Set<Long>> userLikes = new HashMap<>();
        while (rs.next()) {
            userLikes.putIfAbsent(rs.getLong("USER_ID"), new HashSet<>());
            userLikes.get(rs.getLong("USER_ID"))
                    .add(rs.getLong("FILM_ID"));
        }
        return userLikes;
    };

    static final ResultSetExtractor<Map<Long, Map<Long, Double>>> userMarkExtractor = rs -> {
        Map<Long, Map<Long, Double>> userMarks = new HashMap<>();
        while (rs.next()) {
            userMarks.putIfAbsent(rs.getLong("USER_ID"), new HashMap<>());
            userMarks.get(rs.getLong("USER_ID"))
                    .putIfAbsent(rs.getLong("FILM_ID"), rs.getDouble("RATE"));
        }
        return userMarks;
    };
}
