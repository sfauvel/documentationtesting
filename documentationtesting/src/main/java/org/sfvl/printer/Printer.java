package org.sfvl.printer;

import org.sfvl.codeextraction.CodeExtractor;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Printer {

    public static String join(List<String> strings, Function<String, String> mapper) {
        return join(strings, mapper, ",");
    }

    public static String join(List<String> strings, String separator) {
        return String.join(separator, strings);
    }

    public static String join(List<String> strings, Function<String, String> mapper, String separator) {
        return strings.stream().map(mapper).collect(Collectors.joining(separator));
    }

    public static <T> CodeAndResult<T> extractCode(T value) {
        final List<String> codes = CodeExtractor.extractParametersCodeFromStackDepth(2);
        return new CodeAndResult<T>(codes.get(0), value);
    }

    public static <T> CodeAndResult<T> extractCodeAsItWrite(T value) {
        final List<String> codes = CodeExtractor.extractParametersCodeFromStackDepth(2, true);
        return new CodeAndResult<T>(codes.get(0), value);
    }

    public static <T> CodeAndResultList<T> extractCodes(T... values) {
        return new CodeAndResultList<T>(new Printer().getResultFromDepth(2, values));
    }

    public <T> String showResultWithFormat(BiFunction<Object, String, String> format, T... values) {
        final List<String> codes = CodeExtractor.extractParametersCodeFromStackDepth(2);
        codes.remove(0);

        return format_values_and_codes(format, values, codes);
    }

    public <T> String showResult(T... values) {
        final List<CodeAndResult<T>> result_of_code = getResultFromDepth(2, values);
        return format_values_and_codes(r -> "* " + r.getCode() + " = " + r.getValue(), result_of_code);

//        final List<String> codes = extract_parameters_code_from_stack(2);
//
//        return format_values_and_codes(
//                (value, code) -> "* " + code + " = " + value + "\n",
//                values,
//                codes
//        );
    }

    public <T> List<CodeAndResult<T>> getCodeAndResult(T... values) {
        return getResultFromDepth(2, values);
    }

    public <T> List<CodeAndResult<T>> getResultFromDepth(int stack_depth, T[] values) {
        final List<String> codes = CodeExtractor.extractParametersCodeFromStackDepth(stack_depth + 1);
        while (codes.size() > values.length) {
            codes.remove(0);
        }

        return IntStream.range(0, values.length)
                .mapToObj(i -> new CodeAndResult<T>(codes.get(i), values[i]))
                .collect(Collectors.toList());
    }


    private <T> String format_values_and_codes(Function<CodeAndResult<T>, String> format, List<CodeAndResult<T>> result_of_code) {
        return result_of_code.stream().map(format::apply).collect(Collectors.joining("\n"));
    }


    private <T> String format_values_and_codes(BiFunction<Object, String, String> format, T[] values, List<String> codes) {
        String output = "";
        for (int i = 0; i < codes.size(); i++) {
            output += format.apply(values[i], codes.get(i));
        }
        return output;
    }

    public static <K, R> Map<K, List<R>> groupByResult(Function<R, K> buildKey, List<R> results) {
        final Function<Integer, R> buildValue = index -> results.get(index);
        return groupByResult(buildKey, buildValue, results);
    }

    private static <K, R, S> Map<K, List<S>> groupByResult(Function<R, K> buildKey, Function<Integer, S> buildValue, List<R> results) {
        return IntStream.range(0, results.size())
                .mapToObj(i -> Integer.valueOf(i))
                .collect(Collectors.groupingBy(
                        index -> buildKey.apply(results.get(index)),
                        Collectors.mapping(buildValue, Collectors.toList())));
    }

}
