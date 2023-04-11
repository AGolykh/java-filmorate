package ru.yandex.practicum.filmorate.storage.dao;

import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class DirectorDbStorage implements DirectorStorage {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private static final String FIND_ALL_DIRECTORS = "SELECT * FROM directors;";
    private static final String FIND_DIRECTOR_BY_ID = "SELECT * FROM directors WHERE director_id = :DIRECTOR_ID;";
    private static final String UPDATE_DIRECTOR_BY_ID = "UPDATE directors " +
            "SET director_name = :DIRECTOR_NAME WHERE director_id = :DIRECTOR_ID;";
    private static final String DELETE_DIRECTOR = "DELETE FROM directors WHERE director_id = :DIRECTOR_ID;";

    static final RowMapper<Director> directorRowMapper = ((rs, rowNum) -> Director.builder()
            .id(rs.getLong("DIRECTOR_ID"))
            .name(rs.getString("DIRECTOR_NAME")).build());

    @Override
    public Collection<Director> findAll() {
        return jdbcTemplate.query(FIND_ALL_DIRECTORS, directorRowMapper);
    }

    @Override
    public Optional<Director> findById(Long id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    FIND_DIRECTOR_BY_ID, new MapSqlParameterSource()
                            .addValue("DIRECTOR_ID", id),
                    directorRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Director> createDirector(Director director) {
        SimpleJdbcInsert insert = new SimpleJdbcInsert(dataSource)
                .withTableName("DIRECTORS")
                .usingGeneratedKeyColumns("DIRECTOR_ID");
        long id = insert
                .executeAndReturnKey(getDirectorParams(director))
                .longValue();
        return findById(id);
    }

    @Override
    public Optional<Director> updateDirector(Director director) {
        jdbcTemplate.update(UPDATE_DIRECTOR_BY_ID, getDirectorParams(director));
        return findById(director.getId());
    }

    @Override
    public void deleteDirector(Long id) {
        jdbcTemplate.update(DELETE_DIRECTOR,
                new MapSqlParameterSource()
                        .addValue("DIRECTOR_ID", id));
    }

    private MapSqlParameterSource getDirectorParams(Director director) {
        return new MapSqlParameterSource()
                .addValue("DIRECTOR_ID", director.getId())
                .addValue("DIRECTOR_NAME", director.getName());
    }
}