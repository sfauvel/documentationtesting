package org.sfvl.codeextraction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.doctesting.utils.DocPath;

import java.nio.file.Path;

class CodePathTest {

    @RegisterExtension
    static ApprovalsExtension doc = new SimpleApprovalsExtension();

    static Formatter formatter = doc.getFormatter();

    @Test
    public void path_from_a_package() {
        // >>>
        final Class<?> clazz = org.sfvl.samples.MyTest.class;
        final Path path = CodePath.toPath(clazz.getPackage());
        final String pathText = DocPath.toAsciiDoc(path);
        // <<<
        doc.write(
                ".Code",
                formatter.sourceCode(CodeExtractor.extractPartOfCurrentMethod()),
                "Result",
                formatter.blockBuilder("====")
                        .content(pathText)
                        .build());
    }

    @Test
    public void path_from_a_class() {
        // >>>
        final Class<?> clazz = org.sfvl.samples.MyTest.class;
        final Path path = CodePath.toPath(clazz);
        final String pathText = DocPath.toAsciiDoc(path);
        // <<<
        doc.write(
                ".Code",
                formatter.sourceCode(CodeExtractor.extractPartOfCurrentMethod()),
                "Result",
                formatter.blockBuilder("====")
                        .content(pathText)
                        .build());
    }

    @Test
    public void path_from_a_nested_class() {
        // >>>
        final Class<?> clazz = org.sfvl.samples.MyTestWithNestedClass.MyNestedClass.class;
        final Path path = CodePath.toPath(clazz);
        final String pathText = DocPath.toAsciiDoc(path);
        // <<<
        doc.write(
                ".Code",
                formatter.sourceCode(CodeExtractor.extractPartOfCurrentMethod()),
                "Result",
                formatter.blockBuilder("====")
                        .content(pathText)
                        .build());
    }

    @Test
    public void file_of_a_class() {
        // >>>
        final Class<?> clazz = org.sfvl.samples.MyTest.class;
        final Path path = CodePath.toFile(clazz);
        final String pathText = DocPath.toAsciiDoc(path);
        // <<<
        doc.write(
                ".Code",
                formatter.sourceCode(CodeExtractor.extractPartOfCurrentMethod()),
                "Result",
                formatter.blockBuilder("====")
                        .content(pathText)
                        .build());
    }

    /**
     * With a nested class, the file is that of the main class of the file.
     */
    @Test
    public void file_of_a_nested_class() {
        // >>>
        final Class<?> clazz = org.sfvl.samples.MyTestWithNestedClass.MyNestedClass.class;
        final Path path = CodePath.toFile(clazz);
        final String pathText = DocPath.toAsciiDoc(path);
        // <<<
        doc.write(
                ".Code",
                formatter.sourceCode(CodeExtractor.extractPartOfCurrentMethod()),
                "Result",
                formatter.blockBuilder("====")
                        .content(pathText)
                        .build());
    }
}