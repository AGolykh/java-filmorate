package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.Map.Entry;

@Service
@Slf4j
@RequiredArgsConstructor
public class SlopeOne {

    private static final Map<Film, Map<Film, Double>> diff = new HashMap<>();
    private static final Map<Film, Map<Film, Integer>> freq = new HashMap<>();


    public static Map<User, Map<Film, Double>> slopeOne(Map<User, Map<Film, Double>> inputData) {
        System.out.println("Slope One - Before the Prediction\n");
        buildDifferencesMatrix(inputData);
        System.out.println("\nSlope One - With Predictions\n");
        return predict(inputData);
    }

    private static void buildDifferencesMatrix(Map<User, Map<Film, Double>> data) {
        for (Map<Film, Double> user : data.values()) {
            for (Entry<Film, Double> e : user.entrySet()) {
                if (!diff.containsKey(e.getKey())) {
                    diff.put(e.getKey(), new HashMap<>());
                    freq.put(e.getKey(), new HashMap<>());
                }
                for (Entry<Film, Double> e2 : user.entrySet()) {
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
        for (Film j : diff.keySet()) {
            for (Film i : diff.get(j).keySet()) {
                double oldValue = diff.get(j).get(i);
                int count = freq.get(j).get(i);
                diff.get(j).put(i, oldValue / count);
            }
        }
        printData(data);
    }

    private static Map<User, Map<Film, Double>> predict(Map<User, Map<Film, Double>> data) {
        Map<Film, Double> uPred = new HashMap<>();
        Map<Film, Integer> uFreq = new HashMap<>();
        Map<User, Map<Film, Double>> result = new HashMap<>();
        for (Film j : diff.keySet()) {
            uFreq.put(j, 0);
            uPred.put(j, 0.0);
        }
        for (Entry<User, Map<Film, Double>> e : data.entrySet()) {
            for (Film j : e.getValue().keySet()) {
                for (Film k : diff.keySet()) {
                    try {
                        double predictedValue = diff.get(k).get(j) + e.getValue().get(j);
                        double finalValue = predictedValue * freq.get(k).get(j);
                        uPred.put(k, uPred.get(k) + finalValue);
                        uFreq.put(k, uFreq.get(k) + freq.get(k).get(j));
                    } catch (NullPointerException e1) {
                    }
                }
            }
            Map<Film, Double> clean = new HashMap<>();
            for (Film j : uPred.keySet()) {
                if (uFreq.get(j) > 0) {
                    clean.put(j, uPred.get(j) / uFreq.get(j));
                }
            }

            Set<Film> films = new HashSet<>();

            for (Map<Film, Double> filmWithMark : data.values()) {
                for (Entry<Film, Double> film: filmWithMark.entrySet()) {
                    films.add(film.getKey());
                }
            }

            for (Film j : films) {
                if (e.getValue().containsKey(j)) {
                    clean.put(j, e.getValue().get(j));
                } else if (!clean.containsKey(j)) {
                    clean.put(j, -1.0);
                }
            }
            result.put(e.getKey(), clean);
        }

        for (Entry<User, Map<Film, Double>> entry : data.entrySet()) {
            for (Film film : entry.getValue().keySet()) {
                result.get(entry.getKey()).remove(film);
            }
        }

        printData(result);
        return result;
    }

    private static void printData(Map<User, Map<Film, Double>> data) {
        for (User user : data.keySet()) {
            System.out.println(user.getName() + ":");
            print(data.get(user));
        }
    }

    private static void print(Map<Film, Double> hashMap) {
        NumberFormat formatter = new DecimalFormat("#0.000");
        for (Film j : hashMap.keySet()) {
            System.out.println(" " + j.getName() + " --> " + formatter.format(hashMap.get(j).doubleValue()));
        }
    }

}