package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class Rec {

    private final UserService userService;
    private final FilmService filmService;

    public Map<Film, Double> getRecommendedFilms(Map<User, Map<Film, Double>> marks, Long userId) {
        Map<Film, Map <Film, Double>> diff = new HashMap<>();
        Map<Film, Map <Film, Integer>> freq = new HashMap<>();

        for (Map<Film, Double> userMarks : marks.values()) {
            for(Map.Entry<Film, Double> e: userMarks.entrySet()) {
                if (!diff.containsKey(e.getKey())) {
                    diff.put(e.getKey(), new HashMap<>());
                    freq.put(e.getKey(), new HashMap<>());
                }
                for(Map.Entry<Film, Double> e2: userMarks.entrySet()) {
                    int oldCount = 0;
                    if (freq.get(e.getKey()).containsKey(e2.getKey())) {
                        oldCount = freq.get(e.getKey()).get(e2.getKey());
                    }
                    double oldDiff = 0.0;
                    if (diff.get(e.getKey()).containsKey(e2.getKey())) {
                        oldDiff = diff.get(e.getKey()).get(e2.getKey());
                    }
                    double observedDiff = e.getValue() - e2.getValue();
                    freq.get(e.getKey()).put(e2.getKey(), oldCount + 1);
                    diff.get(e.getKey()).put(e2.getKey(), oldDiff + observedDiff);
                }
            }
        }

        for (Film film1 : diff.keySet()) {
            for (Film film2 : diff.get(film1).keySet()) {
                double oldValue = diff.get(film1).get(film2);
                int count = freq.get(film1).get(film2);
                diff.get(film1).put(film2, oldValue / count);
            }
        }

        Map<Film, Double> userMarks = marks.get(userService.findById(userId));
        Map<Film, Double> uPred = new HashMap<>();
        Map<Film, Integer> uFreq = new HashMap<>();
        for (Film fid : diff.keySet()) {
            uPred.put(fid, 0.0);
            uFreq.put(fid, 0);
        }
        for (Film j : userMarks.keySet()) {
            for (Film k : diff.keySet()) {
                double predictedValue = diff.get(k).get(j) + userMarks.get(j);
                double finalValue = predictedValue * freq.get(k).get(j);
                uPred.put(k, uPred.get(k) + finalValue);
                uFreq.put(k, uFreq.get(k) + freq.get(k).get(j));
            }
        }




        HashMap<Film, Double> clean = new HashMap<>();
        for (Film j : uPred.keySet()) {
            if (uFreq.get(j) > 0) {
                clean.put(j, uPred.get(j) / uFreq.get(j));
            }
        }
        for (Film j : diff.keySet()) {
            if (userMarks.containsKey(j)) {
                clean.put(j, userMarks.get(j));
            } else if (!clean.containsKey(j)) {
                clean.put(j, -1.0);
            }
        }

        return clean;
    }
}