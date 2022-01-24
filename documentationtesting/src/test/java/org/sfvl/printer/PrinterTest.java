package org.sfvl.printer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.codeextraction.CodeExtractor;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PrinterTest {

    @RegisterExtension
    public static ApprovalsExtension doc = new SimpleApprovalsExtension();

    @Test
    public void show_result_with_code() {
        {
            // >>>string
            final String result = show_result(
                    "abcd".substring(2),
                    "abcd".substring(2, 4)
            );
            // <<<string
            doc.write("We can use the following code to get values and the code used to obtain it",
                    doc.getFormatter().sourceCode(CodeExtractor.extractPartOfCurrentMethod("string")),
                    "",
                    "Result is",
                    "",
                    result,
                    "");
        }
        {
            // >>>int
            final String result = show_result(
                    3 + 6,
                    5 + 4
            );
            // <<<int
            doc.write("", "We can use it with any value return by code.",
                    doc.getFormatter().sourceCode(CodeExtractor.extractPartOfCurrentMethod("int")),
                    "",
                    "Result is",
                    "",
                    result);
        }
    }

    @Test
    public void list_of_codes_and_values_that_they_provide() {
        // >>>
        final List<CodeExtractor.CodeAndResult<Integer>> result = getCodeAndResult(
                5 + 3,
                4 + 2
        );
        // <<<
        doc.write("We can use the following code to get values and the code used to obtain it",
                doc.getFormatter().sourceCode(CodeExtractor.extractPartOfCurrentMethod()),
                "",
                "Result is",
                "",
                result.stream().map(r -> "* " + r.getCode() + " => " + r.getValue()).collect(Collectors.joining("\n")));
    }

    @Test
    public void show_result_as_describe_with_code() {
        // >>>
        final String result = show_result_as_describe(
                (value, code) -> "Value: " + value + " +\n    " + code + "\n\n",
                "abcd".substring(2),
                "abcd".substring(2, 4)
        );
        // <<<
        doc.write("We can use the following code to get values and the code used to obtain it",
                doc.getFormatter().sourceCode(CodeExtractor.extractPartOfCurrentMethod()),
                "",
                "Result is",
                "",
                result);
    }

    private <T> String show_result_as_describe(BiFunction<Object, String, String> format, T... values) {
        final List<String> codes = CodeExtractor.extractParametersCodeFromStackDepth(2);
        codes.remove(0);

        return format_values_and_codes(format, values, codes);
    }

    private <T> String show_result(T... values) {
        final List<CodeExtractor.CodeAndResult<T>> result_of_code = getResultFromDepth(2, values);
        return format_values_and_codes(r -> "* " + r.getCode() + " = " + r.getValue(), result_of_code);

//        final List<String> codes = extract_parameters_code_from_stack(2);
//
//        return format_values_and_codes(
//                (value, code) -> "* " + code + " = " + value + "\n",
//                values,
//                codes
//        );
    }

    public <T> List<CodeExtractor.CodeAndResult<T>> getCodeAndResult(T... values) {
        return getResultFromDepth(2, values);
    }

    private <T> List<CodeExtractor.CodeAndResult<T>> getResultFromDepth(int stack_depth, T[] values) {
        final List<String> codes = CodeExtractor.extractParametersCodeFromStackDepth(stack_depth + 1);
        while (codes.size() > values.length) {
            codes.remove(0);
        }

        return IntStream.range(0, values.length)
                .mapToObj(i -> new CodeExtractor.CodeAndResult<T>(codes.get(i), values[i]))
                .collect(Collectors.toList());
    }


    private <T> String format_values_and_codes(Function<CodeExtractor.CodeAndResult<T>, String> format, List<CodeExtractor.CodeAndResult<T>> result_of_code) {
        return result_of_code.stream().map(format::apply).collect(Collectors.joining("\n"));
    }


    private <T> String format_values_and_codes(BiFunction<Object, String, String> format, T[] values, List<String> codes) {
        String output = "";
        for (int i = 0; i < codes.size(); i++) {
            output += format.apply(values[i], codes.get(i));
        }
        return output;
    }


}
