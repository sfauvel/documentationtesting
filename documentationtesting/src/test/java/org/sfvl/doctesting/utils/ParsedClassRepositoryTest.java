package org.sfvl.doctesting.utils;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.asciidoc.AsciidocFormatter;
import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.ClassToDocument;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.test_tools.SupplierWithException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ClassToDocument(clazz = ParsedClassRepository.class)
@DisplayName(value = "ParsedClassRepository")
public class ParsedClassRepositoryTest {
    Formatter formatter = new AsciidocFormatter();
    @RegisterExtension
    static ApprovalsExtension doc = new SimpleApprovalsExtension();

    @AfterEach
    public void addSyle(TestInfo testInfo) {
        final String title = "_" + doc.getDocWriter().formatTitle(testInfo.getDisplayName(), testInfo.getTestMethod().get())
                .replaceAll(" ", "_")
                .toLowerCase();
        // Id automatically added when toc is activate but not on H1 title so we add one.
        doc.write("",
                "++++",
                "<style>",
                "#" + doc.getDocWriter().titleId(testInfo.getTestMethod().get()) + " ~ .inline {",
                "   display: inline-block;",
                "   vertical-align: top;",
                "   margin-right: 2em;",
                "}",
                "</style>",
                "++++");
    }

    @Test
    public void context() {

        final Class<?> mainFileClass = new ClassFinder().getMainFileClass(ClassWithInformationToExtract.class);
        final Path path = new DocPath(mainFileClass).test().path();
        String source;
        try {
            source = Files.lines(path).collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        doc.write("Exemples of this chapter use the following class",
                ".Class from which information is extracted",
                "",
                "[source,java,numlines,indent=0]",
                "----",
                source,
                "",
                "----",
                ""
        );

    }

    @Nested
    public class RetrieveComment {
        final ParsedClassRepository parser = new ParsedClassRepository(Config.TEST_PATH);

        public void retrieve_comment(SupplierWithException<String> r, String code) {
            Optional<Exception> exception = Optional.empty();
            String comment = "";
            try {
                comment = r.run();
            } catch (Exception e) {
                exception = Optional.of(e);
            }
            doc.write(
                    "[.inline]",
                    ".How to extract comment",
                    formatter.sourceCode(code),
                    "[.inline]",
                    exception.map(e -> ".Exception").orElse(".Comment extracted"),
                    formatter.blockBuilder("----")
                            .content(exception.map(Exception::toString).orElse(comment))
                            .build()
            );
        }

        @Test
        public void retrieve_comment_of_a_class() {
            retrieve_comment(() -> {
                        // >>>
                        String comment = parser.getComment(ClassWithInformationToExtract.class);
                        // <<<
                        return comment;
                    }
                    , CodeExtractor.extractPartOfCurrentMethod());
        }

        @Test
        public void retrieve_comment_of_a_method() {
            retrieve_comment(() -> {
                        // >>>
                        Method method = ClassWithInformationToExtract.class
                                .getMethod("doSomething");
                        String comment = parser.getComment(method);
                        // <<<
                        return comment;
                    }
                    , CodeExtractor.extractPartOfCurrentMethod());
        }

        /**
         * We are not able to deal with local classes(those defined in method).
         *
         * @throws NoSuchMethodException
         */
        @Test
        public void retrieve_comment_of_local_class_defined_in_a_method() throws NoSuchMethodException {
            retrieve_comment(() -> {
                        // >>>
                        /**
                         * Comment of local class defined in method.
                         */
                        class MyLocalClass {
                            public void doSomething() {

                            }
                        }
                        String comment = parser.getComment(MyLocalClass.class);
                        // <<<
                        return comment;
                    }
                    , CodeExtractor.extractPartOfCurrentMethod());
        }

        @Test
        public void retrieve_comment_of_a_method_with_parameter() throws NoSuchMethodException {
            retrieve_comment(() -> {
                        // >>>
                        Method method = ClassWithInformationToExtract.class
                                .getMethod("doSomething", String.class);
                        String comment = parser.getComment(method);
                        // <<<
                        return comment;
                    }
                    , CodeExtractor.extractPartOfCurrentMethod());
        }

        @Test
        public void retrieve_comment_of_a_method_with_parameter_with_scope() throws NoSuchMethodException {
            retrieve_comment(() -> {
                        // >>>
                        Method method = ClassWithInformationToExtract.class
                                .getMethod("doSomething", Character.class);
                        String comment = parser.getComment(method);
                        // <<<
                        return comment;
                    }
                    , CodeExtractor.extractPartOfCurrentMethod());
        }

        @Test
        public void retrieve_comment_of_a_method_with_list_parameter() throws NoSuchMethodException {
            retrieve_comment(() -> {
                        // >>>
                        Method method = ClassWithInformationToExtract.class
                                .getMethod("doSomething", List.class);
                        String comment = parser.getComment(method);
                        // <<<
                        return comment;
                    }
                    , CodeExtractor.extractPartOfCurrentMethod());
        }

        @Test
        public void retrieve_comment_of_a_method_with_array_parameter() throws NoSuchMethodException {
            retrieve_comment(() -> {
                        // >>>
                        Method method = ClassWithInformationToExtract.class
                                .getMethod("doSomething", String[].class);
                        String comment = parser.getComment(method);
                        // <<<
                        return comment;
                    }
                    , CodeExtractor.extractPartOfCurrentMethod());
        }
    }

    @Nested
    public class RetrieveLineNumber {
        @Test
        public void retrieve_line_of_a_class() {
            final ParsedClassRepository parser = new ParsedClassRepository(Config.TEST_PATH);
            // >>>
            final int lineNumber = parser.getLineNumber(ClassWithInformationToExtract.class);
            // <<<
            doc.write(
                    ".How to get the first line of a class",
                    formatter.sourceCode(CodeExtractor.extractPartOfCurrentMethod()),
                    String.format("Line found: *%d*", lineNumber)
            );
        }

        @Test
        public void retrieve_line_of_a_method() throws NoSuchMethodException {
            final ParsedClassRepository parser = new ParsedClassRepository(Config.TEST_PATH);
            // >>>
            Method method = ClassWithInformationToExtract.class.getMethod("doSomething");
            int lineNumber = parser.getLineNumber(method);
            // <<<
            doc.write(
                    ".How to get the first line of a method",
                    formatter.sourceCode(CodeExtractor.extractPartOfCurrentMethod()),
                    String.format("Line found: *%d*", lineNumber)
            );
        }
    }

    class CodeExecutor {
        private final SupplierWithException<?> supplier;
        private final String sourceCode;

        CodeExecutor(SupplierWithException<?> supplier, String sourceCode) {
            this.supplier = supplier;
            this.sourceCode = sourceCode;
        }

        CodeResult execute() {
            try {
                supplier.run();
                return new CodeResult("Object creation", formatter.sourceCode(sourceCode));
            } catch (Exception e) {
                return new CodeResult("Exception",
                        "." + e.toString().replaceAll("src\\\\(.*)\\\\java", "src/$1/java")
                                + "\n" + formatter.sourceCode(sourceCode));

            }
        }

    }

    class CodeResult {
        private final String description;
        private final String category;

        public CodeResult(String category, String description) {
            this.category = category;
            this.description = description;
        }

        public String category() {
            return category;
        }

        public String description() {
            return description;
        }
    }

    @Test
    public void create_ParsedClassRepository() {
        doc.write(String.format("We can build a %s with several paths.", ParsedClassRepository.class.getSimpleName()),
                "When path does not exist, an Exception was thrown.",
                "Empty path will not be added.",
                "When you do not have one of the default directories,",
                "you can set it with an empty value to not throw an exception.");


        List<CodeExecutor> list = new ArrayList<CodeExecutor>();
        list.add(new CodeExecutor(() -> {
            // >>>OnePath
            ParsedClassRepository repository = new ParsedClassRepository(
                    Paths.get("src/test/java")
            );
            // <<<OnePath
            return repository;
        }, CodeExtractor.extractPartOfCurrentMethod("OnePath")));

        list.add(new CodeExecutor(() -> {
            // >>>TwoPaths
            ParsedClassRepository repository = new ParsedClassRepository(
                    Paths.get("src/main/java"),
                    Paths.get("src/test/java")
            );
            // <<<TwoPaths
            return repository;
        }, CodeExtractor.extractPartOfCurrentMethod("TwoPaths")));

        list.add(new CodeExecutor(() -> {
            // >>>EmptyPath
            ParsedClassRepository repository = new ParsedClassRepository(
                    Paths.get(""),
                    Paths.get("src/test/java")
            );
            // <<<EmptyPath
            return repository;
        }, CodeExtractor.extractPartOfCurrentMethod("EmptyPath")));

        list.add(new CodeExecutor(() -> {
            // >>>UnknownPath
            ParsedClassRepository repository = new ParsedClassRepository(
                    Paths.get("src/UNKNOWN/java")
            );
            // <<<UnknownPath
            return repository;
        }, CodeExtractor.extractPartOfCurrentMethod("UnknownPath")));


        final Map<String, List<CodeResult>> map = list.stream()
                .map(CodeExecutor::execute)
                .collect(Collectors.groupingBy(CodeResult::category));

        final String[] lines = map.entrySet().stream()
                .flatMap(e -> Stream.concat(
                        Stream.of("", "", formatter.title(2, e.getKey()).trim()),
                        e.getValue().stream().map(x -> x.description())
                ))
                .toArray(String[]::new);

        doc.write(lines);
    }

}
