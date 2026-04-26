package ru.t1.feature2.streams;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;

class Feature2TasksTest {
    @Test
    void findsThirdLargestNumber() {
        OptionalInt result = Feature2Tasks.findThirdLargest(List.of(5, 2, 10, 9, 4, 3, 10, 1, 13));

        Assertions.assertEquals(10, result.orElseThrow());
    }

    @Test
    void findsThirdLargestUniqueNumber() {
        OptionalInt result = Feature2Tasks.findThirdLargestUnique(List.of(5, 2, 10, 9, 4, 3, 10, 1, 13));

        Assertions.assertEquals(9, result.orElseThrow());
    }

    @Test
    void findsThreeOldestEngineers() {
        List<Employee> employees = List.of(
                new Employee("Анна", 28, "Инженер"),
                new Employee("Борис", 41, "Инженер"),
                new Employee("Виктор", 34, "Менеджер"),
                new Employee("Галина", 37, "Инженер"),
                new Employee("Дмитрий", 45, "Инженер")
        );

        List<String> result = Feature2Tasks.findThreeOldestEngineerNames(employees);

        Assertions.assertEquals(List.of("Дмитрий", "Борис", "Галина"), result);
    }

    @Test
    void calculatesAverageEngineerAge() {
        List<Employee> employees = List.of(
                new Employee("Анна", 28, "Инженер"),
                new Employee("Борис", 40, "Инженер"),
                new Employee("Виктор", 34, "Менеджер")
        );

        OptionalDouble result = Feature2Tasks.findAverageEngineerAge(employees);

        Assertions.assertEquals(34.0, result.orElseThrow());
    }

    @Test
    void findsLongestWordInList() {
        String result = Feature2Tasks.findLongestWord(List.of("java", "stream", "collections", "api"))
                .orElseThrow();

        Assertions.assertEquals("collections", result);
    }

    @Test
    void countsWordsInSourceString() {
        Map<String, Long> result = Feature2Tasks.countWords("java stream api stream java java");

        Assertions.assertEquals(Map.of("java", 3L, "stream", 2L, "api", 1L), result);
    }

    @Test
    void sortsWordsByLengthThenAlphabetically() {
        List<String> result = Feature2Tasks.sortByLengthThenAlphabetically(List.of("bbb", "a", "cc", "ab", "aa"));

        Assertions.assertEquals(List.of("a", "aa", "ab", "cc", "bbb"), result);
    }

    @Test
    void findsLongestWordAcrossLines() {
        String[] lines = {
                "alpha beta gamma delta epsilon",
                "stream collector comparator optional pipeline",
                "one two three four five"
        };

        String result = Feature2Tasks.findLongestWordAcrossLines(lines).orElseThrow();

        Assertions.assertEquals("comparator", result);
    }
}
