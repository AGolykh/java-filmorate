package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.IncorrectObjectIdException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorStorage directorStorage;
    private final ExistService existService;

    public List<Director> getAll() {
        List<Director> result = directorStorage.findAll();
        log.info("Found {} director(s)", result.size());
        return result;
    }

    public Director getById(Long directorId) {
        Optional<Director> result = directorStorage.findById(directorId);
        if (result.isEmpty()) {
            log.warn("Director {} is not found", directorId);
            throw new IncorrectObjectIdException(String.format("Director %d is not found.", directorId));
        }
        log.info("Director {} is found", result.get().getId());
        return result.get();
    }

    public Director createDirector(Director director) {
        Optional<Director> result = directorStorage.addDirector(director);
        if (result.isEmpty()) {
            log.warn("Director {} is not created", director.getName());
            throw new IncorrectObjectIdException(String.format("Director %s is not created.", director.getName()));
        }
        log.info("Director {} {} created", result.get().getId(), result.get().getName());
        return result.get();
    }

    public Director updateDirector(Director director) {
        existService.assertDirectorExists(director.getId());
        log.info("Director {} {} updated", director.getId(), director.getName());
        return directorStorage.updateDirector(director).orElseThrow();
    }

    public void deleteDirector(Long directorId) {
        existService.assertDirectorExists(directorId);
        log.info("Director {} deleted", directorId);
        directorStorage.removeDirector(directorId);
    }
}
