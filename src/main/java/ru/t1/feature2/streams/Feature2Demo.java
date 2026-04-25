package ru.t1.feature2.streams;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
        String source = "java stream api stream java java";
        List<String> wordsToSort = List.of("bbb", "a", "cc", "ab", "aa");

        printResult(
                1,
                "Найдите в списке целых чисел 3-е наибольшее число (пример: 5 2 10 9 4 3 10 1 13 => 10)",
                numbers.toString(),
                String.valueOf(Feature2Tasks.findThirdLargest(numbers).orElseThrow())
        );
        printResult(
                2,
                "Найдите в списке целых чисел 3-е наибольшее «уникальное» число (пример: 5 2 10 9 4 3 10 1 13 => 9, в отличие от прошлой задачи здесь разные 10 считает за одно число)",
                numbers.toString(),
                String.valueOf(Feature2Tasks.findThirdLargestUnique(numbers).orElseThrow())
        );
        printResult(
                3,
                "Имеется список объектов типа Сотрудник (имя, возраст, должность), необходимо получить список имен 3 самых старших сотрудников с должностью «Инженер», в порядке убывания возраста",
                employees.toString(),
                Feature2Tasks.findThreeOldestEngineerNames(employees).toString()
        );
        printResult(
                4,
                "Имеется список объектов типа Сотрудник (имя, возраст, должность), посчитайте средний возраст сотрудников с должностью «Инженер»",
                employees.toString(),
                String.valueOf(Feature2Tasks.findAverageEngineerAge(employees).orElseThrow())
        );
        printResult(
                5,
                "Найдите в списке слов самое длинное",
                words.toString(),
                Feature2Tasks.findLongestWord(words).orElseThrow()
        );

        Map<String, Long> counts = Feature2Tasks.countWords(source);
        printResult(
                6,
                "Имеется строка с набором слов в нижнем регистре, разделенных пробелом. Постройте хеш-мапы, в которой будут хранится пары: слово - сколько раз оно встречается во входной строке",
                source,
                counts.toString()
        );

        System.out.println("7) Отпечатайте в консоль строки из списка в порядке увеличения длины слова, если слова имеют одинаковую длины, то должен быть сохранен алфавитный порядок");
        System.out.println("Дано: " + wordsToSort);
        System.out.println("Ответ:");
        Feature2Tasks.sortByLengthThenAlphabetically(wordsToSort)
                .forEach(System.out::println);
        System.out.println();

        printResult(
                8,
                "Имеется массив строк, в каждой из которых лежит набор из 5 слов, разделенных пробелом, найдите среди всех слов самое длинное, если таких слов несколько, получите любое из них",
                Arrays.toString(lines),
                Feature2Tasks.findLongestWordAcrossLines(lines).orElseThrow()
        );
    }

    private static void printResult(int number, String taskText, String input, String result) {
        System.out.println(number + ") " + taskText);
        System.out.println("Дано: " + input);
        System.out.println("Ответ: " + result);
        System.out.println();
    }
}
