package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mark;
import ru.yandex.practicum.filmorate.storage.MarkStorage;

import javax.sql.DataSource;
import java.util.Optional;

@Repository("MarkDb")
@RequiredArgsConstructor
public class MarkDbStorage implements MarkStorage {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    static final RowMapper<Mark> markMapper =
            (rs, rowNum) -> new Mark(
                    rs.getLong("FILM_ID"),
                    rs.getLong("USER_ID"),
                    rs.getInt("VALUE"));

    @Override
    public Optional<Mark> find(Long filmId, Long userId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM MARKS " +
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
                .withTableName("MARKS")
                .execute(getMarkParams(new Mark(filmId, userId, value)));
        return find(filmId, userId);
    }

    @Override
    public Optional<Mark> update(Long filmId, Long userId, Integer value) {
        jdbcTemplate.update(
                "UPDATE MARKS " +
                        "SET VALUE = :VALUE " +
                        "WHERE FILM_ID = :FILM_ID " +
                        "AND USER_ID = :USER_ID",
                getMarkParams(new Mark(filmId, userId, value)));
        return find(filmId, userId);
    }

    @Override
    public Optional<Mark> remove(Long filmId, Long userId) {
        jdbcTemplate.update(
                "DELETE FROM MARKS " +
                        "WHERE FILM_ID = :REVIEW_ID " +
                        "AND USER_ID = :USER_ID;",
                new MapSqlParameterSource()
                        .addValue("FILM_ID", filmId)
                        .addValue("USER_ID", userId));
        return find(filmId, userId);
    }


    @Override
    public Boolean isExistMark(Long filmId, Long userId) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT * FROM MARKS " +
                        "WHERE FILM_ID = :FILM_ID " +
                        "AND USER_ID = :USER_ID);",
                new MapSqlParameterSource()
                        .addValue("USER_ID", userId)
                        .addValue("FILM_ID", filmId),
                Boolean.class));
    }

    private MapSqlParameterSource getMarkParams(Mark mark) {
        return new MapSqlParameterSource()
                .addValue("FILM_ID", mark.getFilmId())
                .addValue("USER_ID", mark.getUserId())
                .addValue("VALUE", mark.getValue());
    }

}
