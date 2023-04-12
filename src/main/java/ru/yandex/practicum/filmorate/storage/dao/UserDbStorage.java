package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

@Repository("userDb")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    static final RowMapper<User> userMapper =
            (rs, rowNum) -> User.builder()
                    .id(rs.getLong("USER_ID"))
                    .email(rs.getString("EMAIL"))
                    .login(rs.getString("LOGIN"))
                    .name(rs.getString("USER_NAME"))
                    .birthday(rs.getDate("BIRTHDAY").toLocalDate())
                    .friends(new HashSet<>())
                    .likeFilms(new HashSet<>())
                    .build();

    @Override
    public Collection<User> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM USERS ORDER BY USER_ID;",
                userMapper);
    }

    @Override
    public Optional<User> findById(Long id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM USERS WHERE USER_ID = :USER_ID;",
                    new MapSqlParameterSource()
                            .addValue("USER_ID", id),
                    userMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Collection<User> findFriends(Long id) {
        return jdbcTemplate.query(
                "SELECT * FROM USERS U " +
                        "JOIN FRIENDSHIPS FS ON U.USER_ID = FS.FRIEND_ID " +
                        "WHERE FS.USER_ID = :USER_ID " +
                        "ORDER BY U.USER_ID;",
                new MapSqlParameterSource()
                        .addValue("USER_ID", id),
                userMapper);
    }

    @Override
    public Collection<User> findCommonFriends(Long userId, Long friendId) {
        return jdbcTemplate.query(
                "SELECT * FROM USERS U, FRIENDSHIPS F, FRIENDSHIPS O " +
                        "WHERE U.USER_ID = F.FRIEND_ID AND U.USER_ID = O.FRIEND_ID " +
                        "AND F.USER_ID = :USER_ID AND O.USER_ID = :FRIEND_ID;",
                new MapSqlParameterSource()
                        .addValue("USER_ID", userId)
                        .addValue("FRIEND_ID", friendId),
                userMapper);
    }

    @Override
    public Optional<User> create(User user) {
        SimpleJdbcInsert insert = new SimpleJdbcInsert(dataSource)
                .withTableName("USERS")
                .usingGeneratedKeyColumns("USER_ID");
        long id = insert
                .executeAndReturnKey(getUserParams(user))
                .longValue();
        return findById(id);
    }

    @Override
    public Optional<User> update(User user) {
        jdbcTemplate.update(
                "UPDATE USERS " +
                        "SET EMAIL = :EMAIL, LOGIN = :LOGIN, " +
                        "USER_NAME = :USER_NAME, BIRTHDAY = :BIRTHDAY " +
                        "WHERE USER_ID = :USER_ID;",
                getUserParams(user));
        return findById(user.getId());
    }

    @Override
    public List<Integer> findAdviseFilmsIds(Integer id) {
        final List<Integer> maxCommonUsersId = convertMaxCommonLikes(id);
        final Map<Integer, List<Integer>> filmDiffByUser = getDiffFilms(id);
        final Map<Integer, Integer> scoreByFilms = getFilmsScore(id);
        return filmDiffByUser.entrySet().stream()
                .filter(a -> maxCommonUsersId.contains(a.getKey()))
                .flatMap(a -> a.getValue().stream())
                .distinct()
                .sorted(Comparator.comparing(scoreByFilms::get).reversed())
                .collect(Collectors.toList());
    }

    private Map<Integer, Integer> getFilmsScore(Integer id) {
        Map<Integer, Integer> filmsScore = new HashMap<>();
        jdbcTemplate.query("SELECT FILM_ID, COUNT(FILM_ID) SCORE " +
                        "FROM LIKES " +
                        "WHERE USER_ID IN (SELECT DISTINCT USER_ID " +
                        "FROM LIKES " +
                        "WHERE USER_ID <> :ID " +
                        "AND FILM_ID IN (SELECT FILM_ID " +
                        "FROM LIKES " +
                        "WHERE USER_ID = :ID)) " +
                        "GROUP BY FILM_ID " +
                        "ORDER BY SCORE DESC;",
                new MapSqlParameterSource("ID", id),
                (ResultSet rs) -> {
                    int filmId = rs.getInt("FILM_ID");
                    int score = rs.getInt("SCORE");
                    filmsScore.put(filmId, score);
                });
        return filmsScore;
    }

    private Map<Integer, List<Integer>> getDiffFilms(Integer id) {
        final Map<Integer, List<Integer>> filmLikeByUserId = new HashMap<>();
        jdbcTemplate.query(
                "SELECT USER_ID, FILM_ID " +
                        "FROM LIKES " +
                        "WHERE USER_ID IN (SELECT DISTINCT USER_ID " +
                        "FROM LIKES " +
                        "WHERE USER_ID <> :ID " +
                        "AND FILM_ID IN (SELECT FILM_ID " +
                        "FROM LIKES " +
                        "WHERE USER_ID = :ID)) " +
                        "AND FILM_ID NOT IN (SELECT FILM_ID FROM LIKES WHERE USER_ID = :ID);",
                new MapSqlParameterSource().addValue("ID", id),
                (ResultSet rs) -> {
                    int userId = rs.getInt("USER_ID");
                    int filmId = rs.getInt("FILM_ID");
                    filmLikeByUserId.computeIfAbsent(userId, l -> new ArrayList<>()).add(filmId);
                });
        return filmLikeByUserId;
    }

    private List<Integer> convertMaxCommonLikes(Integer id) {
        final List<Integer> scores = new ArrayList<>();
        final Map<Integer, Integer> scoreByUsersId = new HashMap<>();
        jdbcTemplate.query(
                "SELECT USER_ID, COUNT(FILM_ID) SCORE " +
                        "FROM LIKES " +
                        "WHERE USER_ID <> :ID " +
                        "AND FILM_ID IN (SELECT FILM_ID FROM LIKES WHERE USER_ID = :ID) " +
                        "GROUP BY USER_ID " +
                        "ORDER BY SCORE DESC " +
                        "LIMIT 1",
                new MapSqlParameterSource().addValue("ID", id),
                (ResultSet rs) -> {
                    int score = rs.getInt("SCORE");
                    scores.add(score);
                    scoreByUsersId.put(rs.getInt("USER_ID"), score);
                });
        Optional<Integer> scoreMax = scores.stream().max(Comparator.naturalOrder());
        return scoreMax.map(integer -> scoreByUsersId.entrySet()
                .stream()
                .filter(e -> e.getValue().equals(integer))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList())).orElseGet(ArrayList::new);
    }

    private MapSqlParameterSource getUserParams(User user) {
        return new MapSqlParameterSource()
                .addValue("USER_ID", user.getId())
                .addValue("EMAIL", user.getEmail())
                .addValue("LOGIN", user.getLogin())
                .addValue("USER_NAME", user.getName())
                .addValue("BIRTHDAY", user.getBirthday());
    }
}
