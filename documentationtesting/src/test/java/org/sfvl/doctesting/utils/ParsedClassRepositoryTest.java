package org.sfvl.doctesting.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.ClassToDocument;
import org.sfvl.doctesting.junitextension.FindLambdaMethod;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

@ClassToDocument(clazz = ParsedClassRepository.class)
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
//                CodeExtractor.extractPartOfFile(path, ClassWithInformationToExtract.class.getSimpleName()).trim(),
                "",
                "----",
                ""
//                formatter.sourceCode(CodeExtractor.extractPartOfFile(path, ClassWithInformationToExtract.class.getSimpleName()))
        );

    }

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
    public void retrieve_comment_of_a_method() {
        final ParsedClassRepository parser = new ParsedClassRepository(Config.TEST_PATH);
        final Method method = FindLambdaMethod.getMethod(ClassWithInformationToExtract::doSomething);
        // >>>
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
    public void retrieve_line_of_a_method() {
        final ParsedClassRepository parser = new ParsedClassRepository(Config.TEST_PATH);
        // >>>
        final Method method = FindLambdaMethod.getMethod(ClassWithInformationToExtract::doSomething);
        final int lineNumber = parser.getLineNumber(method);
        // <<<
        doc.write(
                ".How to get the first line of a method",
                formatter.sourceCode(CodeExtractor.extractPartOfCurrentMethod()),
                String.format("Line found: *%d*", lineNumber)
        );
    }

}
