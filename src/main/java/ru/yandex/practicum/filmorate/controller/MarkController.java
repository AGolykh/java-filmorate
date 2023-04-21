package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.service.MarkService;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class MarkController {

    private final MarkService markService;

    @PutMapping("{filmId}/mark/{userId}")
    public void addMark(@PathVariable Long filmId, @PathVariable Long userId, @RequestParam Integer value) {
        markService.addMark(filmId, userId, value);
    }

    @DeleteMapping("{filmId}/mark/{userId}")
    public void deleteMark(@PathVariable Long filmId, @PathVariable Long userId) {
        markService.deleteMark(filmId, userId);
    }
}