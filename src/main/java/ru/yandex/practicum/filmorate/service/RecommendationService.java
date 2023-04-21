package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendationService {

    private final UserService userService;
    private final FilmService filmService;
    private final MarkService markService;

    public Map<Long, Double> getRecommendedFilms(Map<Long, Map<Long, Double>> marks, Long userId) {

        Map<Long, Map <Long, Double>> diff = new HashMap<>();
        Map<Long, Map <Long, Integer>> freq = new HashMap<>();

        for (Map<Long, Double> userMarks : marks.values()) {
            for(Map.Entry<Long, Double> e: userMarks.entrySet()) {
                if (!diff.containsKey(e.getKey())) {
                    diff.put(e.getKey(), new HashMap<>());
                    freq.put(e.getKey(), new HashMap<>());
                }
                for(Map.Entry<Long, Double> e2: userMarks.entrySet()) {
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

        for (Long i : diff.keySet()) {
            for (Long j : diff.get(i).keySet()) {
                double oldValue = diff.get(i).get(j);
                int count = freq.get(i).get(j);
                diff.get(i).put(j, oldValue / count);
            }
        }

        Map<Long, Double> userMarks = marks.get(userId);
        Map<Long, Double> uPred = new HashMap<>();
        Map<Long, Integer> uFreq = new HashMap<>();
        for (Long fid : diff.keySet()) {
            uPred.put(fid, 0.0);
            uFreq.put(fid, 0);
        }
        for (Long j : userMarks.keySet()) {
            for (Long k : diff.keySet()) {
                double predictedValue = diff.get(k).get(j) + userMarks.get(j);
                double finalValue = predictedValue * freq.get(k).get(j);
                uPred.put(k, uPred.get(k) + finalValue);
                uFreq.put(k, uFreq.get(k) + freq.get(k).get(j));
            }
        }

        HashMap<Long, Double> clean = new HashMap<>();
        for (Long j : uPred.keySet()) {
            if (uFreq.get(j) > 0) {
                clean.put(j, uPred.get(j) / uFreq.get(j));
            }
        }
        for (Long j : diff.keySet()) {
            if (userMarks.containsKey(j)) {
                clean.put(j, userMarks.get(j));
            } else if (!clean.containsKey(j)) {
                clean.put(j, -1.0);
            }
        }

        return clean;
    }
}