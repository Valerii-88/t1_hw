package ru.t1.feature2.streams;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class Feature2Tasks {
    private static final String ENGINEER_POSITION = "Инженер";

    private Feature2Tasks() {
    }

    public static OptionalInt findThirdLargest(List<Integer> numbers) {
        Objects.requireNonNull(numbers, "numbers must not be null");
        return numbers.stream()
                .sorted(Comparator.reverseOrder())
                .skip(2)
                .mapToInt(Integer::intValue)
                .findFirst();
    }

    public static OptionalInt findThirdLargestUnique(List<Integer> numbers) {
        Objects.requireNonNull(numbers, "numbers must not be null");
        return numbers.stream()
                .distinct()
                .sorted(Comparator.reverseOrder())
                .skip(2)
                .mapToInt(Integer::intValue)
                .findFirst();
    }

    public static List<String> findThreeOldestEngineerNames(List<Employee> employees) {
        Objects.requireNonNull(employees, "employees must not be null");
        return employees.stream()
                .filter(employee -> ENGINEER_POSITION.equals(employee.position()))
                .sorted(Comparator.comparingInt(Employee::age)
                        .reversed()
                        .thenComparing(Employee::name))
                .limit(3)
                .map(Employee::name)
                .toList();
    }

    public static OptionalDouble findAverageEngineerAge(List<Employee> employees) {
        Objects.requireNonNull(employees, "employees must not be null");
        return employees.stream()
                .filter(employee -> ENGINEER_POSITION.equals(employee.position()))
                .mapToInt(Employee::age)
                .average();
    }

    public static Optional<String> findLongestWord(List<String> words) {
        Objects.requireNonNull(words, "words must not be null");
        return words.stream()
                .filter(Objects::nonNull)
                .max(Comparator.comparingInt(String::length)
                        .thenComparing(Comparator.reverseOrder()));
    }

    public static Map<String, Long> countWords(String source) {
        Objects.requireNonNull(source, "source must not be null");
        return Arrays.stream(source.trim().split("\\s+"))
                .filter(word -> !word.isBlank())
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));
    }

    public static List<String> sortByLengthThenAlphabetically(List<String> words) {
        Objects.requireNonNull(words, "words must not be null");
        return words.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder()))
                .toList();
    }

    public static Optional<String> findLongestWordAcrossLines(String[] lines) {
        Objects.requireNonNull(lines, "lines must not be null");
        return Arrays.stream(lines)
                .filter(Objects::nonNull)
                .flatMap(line -> Arrays.stream(line.trim().split("\\s+")))
                .filter(word -> !word.isBlank())
                .max(Comparator.comparingInt(String::length)
                        .thenComparing(Comparator.reverseOrder()));
    }
}
