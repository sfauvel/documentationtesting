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

    public <K> Map<T, List<CodeAndResult<T>>> groupBy() {
        return groupBy(CodeAndResult::getValue);
    }

    public <K> Map<K, List<CodeAndResult<T>>> groupBy(Function<CodeAndResult<T>, K> buildKey) {
        return Printer.groupByResult(buildKey, data);
    }

    public <K> Map<K, List<CodeAndResult<T>>> groupBy(BiFunction<T, String, K> buildKey) {
        return Printer.groupByResult(
                cr -> buildKey.apply(cr.getValue(), cr.getCode()),
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
        return groupBy(buildKey).entrySet().stream()
                .map(e -> formatFunction.apply(
                        e.getKey(),
                        e.getValue().stream().map(CodeAndResult::getCode).collect(Collectors.toList())
                )).collect(Collectors.joining(delimiter));
    }

}
