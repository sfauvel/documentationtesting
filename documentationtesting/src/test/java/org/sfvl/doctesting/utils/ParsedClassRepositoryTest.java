package org.sfvl.doctesting.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.ClassToDocument;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@ClassToDocument(clazz = ParsedClassRepository.class)
@DisplayName(value="ParsedClassRepository")
public class ParsedClassRepositoryTest {
    Formatter formatter = new AsciidocFormatter();
    @RegisterExtension
    static ApprovalsExtension doc = new SimpleApprovalsExtension();

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
        @Test
        public void retrieve_comment_of_a_class() {
            final ParsedClassRepository parser = new ParsedClassRepository(Config.TEST_PATH);
            // >>>
            final String comment = parser.getComment(ClassWithInformationToExtract.class);
            // <<<
            doc.write(
                    ".How to extract comment",
                    formatter.sourceCode(CodeExtractor.extractPartOfCurrentMethod()),
                    ".Comment extracted",
                    formatter.blockBuilder("====")
                            .content(comment)
                            .build()
            );

        }

        @Test
        public void retrieve_comment_of_a_method() throws NoSuchMethodException {
            final ParsedClassRepository parser = new ParsedClassRepository(Config.TEST_PATH);
            // >>>
            final Method method = ClassWithInformationToExtract.class.getMethod("doSomething");
            final String comment = parser.getComment(method);
            // <<<
            doc.write(
                    ".How to extract comment",
                    formatter.sourceCode(CodeExtractor.extractPartOfCurrentMethod()),
                    ".Comment extracted",
                    formatter.blockBuilder("====")
                            .content(comment)
                            .build()
            );
        }

        @Test
        public void retrieve_comment_of_a_method_with_parameter() throws NoSuchMethodException {
            final ParsedClassRepository parser = new ParsedClassRepository(Config.TEST_PATH);
            // >>>
            final Method method = ClassWithInformationToExtract.class.getMethod("doSomething", String.class);
            final String comment = parser.getComment(method);
            // <<<
            doc.write(
                    ".How to extract comment",
                    formatter.sourceCode(CodeExtractor.extractPartOfCurrentMethod()),
                    ".Comment extracted",
                    formatter.blockBuilder("====")
                            .content(comment)
                            .build()
            );
        }

        @Test
        public void retrieve_comment_of_a_method_with_parameter_with_scope() throws NoSuchMethodException {
            final ParsedClassRepository parser = new ParsedClassRepository(Config.TEST_PATH);
            // >>>
            final Method method = ClassWithInformationToExtract.class.getMethod("doSomething", Character.class);
            final String comment = parser.getComment(method);
            // <<<
            doc.write(
                    ".How to extract comment",
                    formatter.sourceCode(CodeExtractor.extractPartOfCurrentMethod()),
                    ".Comment extracted",
                    formatter.blockBuilder("====")
                            .content(comment)
                            .build()
            );
        }

        @Test
        public void retrieve_comment_of_a_method_with_list_parameter() throws NoSuchMethodException {
            final ParsedClassRepository parser = new ParsedClassRepository(Config.TEST_PATH);
            // >>>
            final Method method = ClassWithInformationToExtract.class.getMethod("doSomething", List.class);
            final String comment = parser.getComment(method);
            // <<<
            doc.write(
                    ".How to extract comment",
                    formatter.sourceCode(CodeExtractor.extractPartOfCurrentMethod()),
                    ".Comment extracted",
                    formatter.blockBuilder("====")
                            .content(comment)
                            .build()
            );
        }

        @Test
        public void retrieve_comment_of_a_method_with_array_parameter() throws NoSuchMethodException {
            final ParsedClassRepository parser = new ParsedClassRepository(Config.TEST_PATH);
            // >>>
            final Method method = ClassWithInformationToExtract.class.getMethod("doSomething", String[].class);
            final String comment = parser.getComment(method);
            // <<<
            doc.write(
                    ".How to extract comment",
                    formatter.sourceCode(CodeExtractor.extractPartOfCurrentMethod()),
                    ".Comment extracted",
                    formatter.blockBuilder("====")
                            .content(comment)
                            .build()
            );
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
            final Method method = ClassWithInformationToExtract.class.getMethod("doSomething");
            final int lineNumber = parser.getLineNumber(method);
            // <<<
            doc.write(
                    ".How to get the first line of a method",
                    formatter.sourceCode(CodeExtractor.extractPartOfCurrentMethod()),
                    String.format("Line found: *%d*", lineNumber)
            );
        }
    }

}
