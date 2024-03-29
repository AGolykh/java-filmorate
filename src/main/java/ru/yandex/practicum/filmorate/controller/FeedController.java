package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.service.EventService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class FeedController {
    private final EventService eventService;

    @GetMapping("/{userId}/feed")
    public List<Event> getFeed(@Valid @PathVariable Long userId) {
        return eventService.getFeed(userId);
    }
}
