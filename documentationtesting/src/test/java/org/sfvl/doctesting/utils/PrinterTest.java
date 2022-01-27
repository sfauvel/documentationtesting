package org.sfvl.doctesting.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.codeextraction.CodeExtractor;
import org.sfvl.codeextraction.MethodReference;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.printer.CodeAndResult;
import org.sfvl.printer.CodeAndResultList;
import org.sfvl.printer.Printer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@DisplayName(value = "Printer")
public class PrinterTest {

    @RegisterExtension
    public static ApprovalsExtension doc = new SimpleApprovalsExtension();

    class Toto {
        @Override
        public String toString() {
            return "it's me";
        }
    }

    @Test
    public void show_result_with_code() {
        {
            // >>>string
            final String result = new Printer().show_result(
                    "abcd".substring(2),
                    "abcd".substring(2, 4)
            );
            // <<<string
            doc.write("We can use the following code to get values and the code used to obtain it.",
                    doc.getFormatter().sourceCode(CodeExtractor.extractPartOfCurrentMethod("string")),
                    "",
                    "Result is",
                    "",
                    result,
                    "");
        }
        {
            // >>>int
            final String result = new Printer().show_result(
                    3 + 6,
                    5 + 4
            );
            // <<<int
            doc.write("", "We can use it with any type returned by code.",
                    doc.getFormatter().sourceCode(CodeExtractor.extractPartOfCurrentMethod("int")),
                    "",
                    "Result is",
                    "",
                    result,
                    "");
        }
        {
            // >>>null
            String value = "abcdef";
            String nullValue = null;
            final String result = new Printer().show_result(
                    value,
                    nullValue
            );
            // <<<null
            doc.write("", "Null values are displayed with `null`.",
                    doc.getFormatter().sourceCode(CodeExtractor.extractPartOfCurrentMethod("null")),
                    "",
                    "Result is",
                    "",
                    result);
        }
    }

    @Test
    public void list_of_codes_and_values_that_they_provide() {
        // >>>
        final List<CodeAndResult<Integer>> result = new Printer().getCodeAndResult(
                5 + 3,
                4 + 2
        );
        // <<<

        final List<MethodReference.SerializableFunction<CodeAndResult, ?>> getters = Arrays.asList(
                CodeAndResult::getCode,
                CodeAndResult::getValue
        );

        doc.write("We can use the following code to get values and the code used to obtain it",
                doc.getFormatter().sourceCode(CodeExtractor.extractPartOfCurrentMethod()),
                "",
                "Result is",
                "",
                "[%autowidth]" + doc.getFormatter().tableWithHeader(Stream.concat(
                            Stream.of(getters.stream().map(f -> MethodReference.getMethod(f).getName() + "()").collect(Collectors.toList())),
                            result.stream().map(r -> getters.stream().map(f -> f.apply(r)).collect(Collectors.toList()))
                        ).collect(Collectors.toList())),
                "");
    }

    @Test
    public void show_result_as_describe_with_code() {
        // >>>
        final String result = new Printer().show_result_as_describe(
                (value, code) -> "Extracted value: `" + value + "` +\nCode to extract:\n----\n" + code + "\n----\n\n",
                "abcdef".substring(2),
                "abcdef".substring(2, 4)
        );
        // <<<
        doc.write("We can use the following code to get values and the code used to obtain it",
                doc.getFormatter().sourceCode(CodeExtractor.extractPartOfCurrentMethod()),
                "",
                "Result is",
                "",
                result);
    }

    @Nested
    class GroupByResult {
        @Test
        public void description() {
            doc.write("It's sometime interesting to group all values that provides the same result.",
                    "It can be used to show several ways of doing the same thing.",
                    "We can simply display result once with all cases that can be used to build it.",
                    "You also can just check that we obtain the same result with different values.",
                    "In that case, there is only one key in the result",
                    "and you can just say that all values are the same.");
        }

        @Test
        public void applying_a_function() {

            // >>>
            final Map<String, List<String>> stringListMap = Printer.groupByResult(
                    String::toLowerCase,
                    Arrays.asList("abc", "ABC", "XyZ", "Abc")
            );
            // <<<

            doc.write(doc.getFormatter().sourceCode(CodeExtractor.extractPartOfCurrentMethod()));

            doc.write("",
                    "You obtain a map with value returned by the function as key and a list of values that provides the key value.",
                    "",
                    "");

            for (Map.Entry<String, List<String>> stringListEntry : stringListMap.entrySet()) {
                doc.write("*" + stringListEntry.getKey() + "*: "
                                + stringListEntry.getValue().stream()
                                .map(code -> "`" + code.trim() + "`")
                                .collect(Collectors.joining(", ")),
                        " +", "");
            }

        }

        @Test
        public void using_code_and_result_class() {

            // >>>
            final CodeAndResultList<Integer> cr = new CodeAndResultList(
                    2 + 4,
                    3 + 5,
                    3 + 3
            );

            // Extract codes grouped by value.
            Map<Integer, List<CodeAndResult<Integer>>> result = cr.groupByResult();

            // Format an output grouping codes by value and providing a function to generate lines.
            final String output = cr.formatGroupedByValue((value, codes) ->
                    "*" + value + "*: " + cr.mapAndJoin(codes, code -> "`" + code.trim() + "`", ", ")
            );
            // <<<

            doc.write("You can create an object that associate the code and the value it return.",
                    "This class provides method to help rendering data.",
                    "",
                    doc.getFormatter().sourceCode(CodeExtractor.extractPartOfCurrentMethod()),
                    "",
                    output,
                    "",
                    doc.getFormatter().blockBuilder("----").title("Generated asciidoc in output").content(output).build(),
                    "");

            doc.write("You can change delimiter between values." ,
                    "Here, we used \" / \".",
                    "",
                    doc.getFormatter().sourceCode(CodeExtractor.extractPartOfCurrentMethod("delimiter")),
                    "",
                    // >>>delimiter
                    cr.formatGroupedByValue((value, codes) ->
                            "*" + value + "*: " + cr.mapAndJoin(codes, code -> "`" + code.trim() + "`", ", ")
                    , " / ")
                    // <<<delimiter
            );
        }

        @Test
        public void group_by_a_modified_value() {

            // >>>
            final CodeAndResultList<String> cr = new CodeAndResultList(
                    "abc", "ijkl", "xyz"
            );

            // Format an output grouping codes by value applying a function.
            final String output = cr.formatGroupedByValue(
                    (value, code) -> value.length(),
                    (value, codes) -> "*" + value + "*: " + cr.mapAndJoin(codes, Function.identity(), ", "),
                    " +\n"
            );
            // <<<

            doc.write("You can group using the value after applying a function.",
                    "The key used to group can be of another type than that of the initial value.",
                    "",
                    doc.getFormatter().sourceCode(CodeExtractor.extractPartOfCurrentMethod()),
                    "",
                    output,
                    "",
                    doc.getFormatter().blockBuilder("----").title("Generated asciidoc in output").content(output).build(),
                    "");
        }

    }

}
