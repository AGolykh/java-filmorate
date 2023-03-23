package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendStorage;
import ru.yandex.practicum.filmorate.storage.LikesStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    @Qualifier("userDb") private final UserStorage userStorage;
    @Qualifier("friendDb") private final FriendStorage friendStorage;
    @Qualifier("likesDb") private final LikesStorage likesStorage;

    public Collection<User> findAll() { // вынести оснастку в другой класс
        Collection<User> result = userStorage.findAll();
        result.forEach(this::makeData);
        log.info("Found {} user(s).", result.size());
        return result;
    }

    public Optional<User> findById(Long userId) { // вынести оснастку в другой класс
        Optional<User> result = userStorage.findById(userId);
        if (result.isEmpty()) {
            log.warn("User {} is not found.", userId);
            return result;
        }
        makeData(result.get());
        log.info("User {} is found.", result.get().getId());
        return result;
    }

    public User create(User user) {
        User result = userStorage.create(user);
        makeData(result);
        log.info("User {} {} added.", result.getId(), result.getLogin());
        return result;
    }

    public User update(User user) {
        User result = userStorage.update(user);
        makeData(result);
        log.info("User {} updated.", result.getId());
        return result;
    }

    public Map<String, Long> addFriend(Long userId, Long friendId) {
        Map<String, Long> result = validateUserDataRequest(userId, friendId);
        if (!result.isEmpty()) {
            log.warn("Data {} is not found.", result);
            return result;
        }
        friendStorage.addFriend(userId, friendId);
        log.info("User {} added user {} to friends.", userId, friendId);
        return null;
    }

    public Map<String, Long> delFriend(Long userId, Long friendId) {
        Map<String, Long> result = validateUserDataRequest(userId, friendId);
        if (!result.isEmpty()) {
            log.warn("Data {} is not found.", result);
            return result;
        }
        friendStorage.delFriend(userId, friendId);
        log.info("User {} deleted user {} from friends.", userId, friendId);
        return null;

    }

    public Collection<User> getFriends(Long userId) {
        Collection<User> result = userStorage.findAll()
                .stream()
                .filter(user -> friendStorage
                        .getFriends(userId)
                        .contains(user.getId()))
                .collect(Collectors.toList());
        result.forEach(this::makeData);
        log.info("Found {} user(s).", result.size());
        return result;
    }

    public Collection<User> getCommonFriends(Long userId, Long friendId) {
        Collection<User> result = userStorage.findAll()
                .stream()
                .filter(user -> friendStorage
                        .getCommonFriends(userId, friendId)
                        .contains(user.getId()))
                .collect(Collectors.toList());
        result.forEach(this::makeData);
        log.info("Found {} user(s).", result.size());
        return result;
    }

    private Map<String, Long> validateUserDataRequest(Long userId, Long friendId) {
        Map<String, Long> result = new HashMap<>();
        if (userStorage.findById(userId).isEmpty()) {
            result.put("userId", userId);
        }
        if (userStorage.findById(friendId).isEmpty()) {
            result.put("friendId", friendId);
        }
        return result;
    }

    private void makeData(User user) {
        user.setFriends(new HashSet<>(friendStorage.getFriends(user.getId())));
        user.setLikeFilms(new HashSet<>(likesStorage.getUserLikes(user.getId())));
    }
 }
