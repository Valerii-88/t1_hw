package ru.t1.feature2.streams;

import java.util.List;

public final class Feature2Demo {
    private Feature2Demo() {
    }

    public static void main(String[] args) {
        List<Integer> numbers = List.of(5, 2, 10, 9, 4, 3, 10, 1, 13);
        List<Employee> employees = List.of(
                new Employee("Анна", 28, "Инженер"),
                new Employee("Борис", 41, "Инженер"),
                new Employee("Виктор", 34, "Менеджер"),
                new Employee("Галина", 37, "Инженер"),
                new Employee("Дмитрий", 45, "Инженер")
        );
        List<String> words = List.of("java", "stream", "api", "collections", "lambda");
        String[] lines = {
                "alpha beta gamma delta epsilon",
                "stream collector comparator optional pipeline",
                "one two three four five"
        };

        System.out.println("Third largest: " + Feature2Tasks.findThirdLargest(numbers).orElseThrow());
        System.out.println("Third largest unique: " + Feature2Tasks.findThirdLargestUnique(numbers).orElseThrow());
        System.out.println("Top 3 oldest engineers: " + Feature2Tasks.findThreeOldestEngineerNames(employees));
        System.out.println("Average engineer age: " + Feature2Tasks.findAverageEngineerAge(employees).orElseThrow());
        System.out.println("Longest word in list: " + Feature2Tasks.findLongestWord(words).orElseThrow());
        System.out.println("Word counts: " + Feature2Tasks.countWords("java stream api stream java java"));
        System.out.println("Sorted by length: " + Feature2Tasks.sortByLengthThenAlphabetically(words));
        System.out.println("Longest word across lines: " + Feature2Tasks.findLongestWordAcrossLines(lines).orElseThrow());
    }
}
