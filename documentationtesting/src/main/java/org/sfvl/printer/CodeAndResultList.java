package org.sfvl.printer;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CodeAndResultList<T> {
    List<CodeAndResult<T>> data;

    public CodeAndResultList(List<CodeAndResult<T>> data) {
        this.data = data;
    }

    public CodeAndResultList(T... values) {
        this(new Printer().getResultFromDepth(2, values));
    }

    public <K> Map<T, List<CodeAndResult<T>>> groupByResult() {
        return groupByResult(CodeAndResult::getValue);
    }

    public <K> Map<K, List<CodeAndResult<T>>> groupByResult(Function<CodeAndResult<T>, K> buildKey) {
        return Printer.groupByResult(
                r -> buildKey.apply(r),
                data
        );
    }

    public <K> Map<K, List<CodeAndResult<T>>> groupByResult(BiFunction<T, String, K> buildKey) {
        return Printer.groupByResult(
                cr -> {
                    final T value = cr.getValue();
                    final String code = cr.getCode();
                    final K apply = buildKey.apply(value, code);
                    return apply;
                },
                data
        );
    }

    public String formatGroupedByValue(BiFunction<T, List<String>, String> formatFunction) {
        return formatGroupedByValue(formatFunction, " +\n");
    }

    public String formatGroupedByValue(BiFunction<T, List<String>, String> formatFunction, String delimiter) {
        return formatGroupedByValue((value, code) -> value, formatFunction, delimiter);
    }

    public <K> String formatGroupedByValue(BiFunction<T, String, K> buildKey, BiFunction<K, List<String>, String> formatFunction, String delimiter) {
        return groupByResult(buildKey).entrySet().stream()
                .map(e -> formatFunction.apply(
                        e.getKey(),
                        e.getValue().stream().map(CodeAndResult::getCode).collect(Collectors.toList())
                )).collect(Collectors.joining(delimiter));
    }

    public String mapAndJoin(List<String> codes, Function<String, String> formatCode, String separator) {
        return codes.stream().map(formatCode).collect(Collectors.joining(separator));
    }
}
