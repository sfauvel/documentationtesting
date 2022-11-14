package org.sfvl.doctesting.junitextension;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.codeextraction.ClassFinder;
import org.sfvl.codeextraction.CodeExtractor;
import org.sfvl.codeextraction.MethodReference;
import org.sfvl.docformatter.Formatter;
import org.sfvl.docformatter.asciidoc.AsciidocFormatter;
import org.sfvl.doctesting.NotIncludeToDoc;
import org.sfvl.doctesting.utils.ClassToDocument;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.doctesting.utils.NoTitle;
import org.sfvl.doctesting.utils.OnePath;
import org.sfvl.samples.*;
import org.sfvl.samples.justone.OneTest;
import org.sfvl.test_tools.IntermediateHtmlPage;
import org.sfvl.test_tools.OnlyRunProgrammatically;
import org.sfvl.test_tools.ProjectTestExtension;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@DisplayName("Approvals extension")
@ClassToDocument(clazz = ApprovalsExtension.class)
@ExtendWith(IntermediateHtmlPage.class)
public class ApprovalsExtensionTest {

    private AsciidocFormatter formatter = new AsciidocFormatter();
    @RegisterExtension
    static ProjectTestExtension doc = new ProjectTestExtension();

    @Test
    @DisplayName(value = "Creating a test using ApprovalsExtension")
    public void using_extension() {
        final Class<?> testClass = OneTest.class;

        doc.runTestAndWriteResultAsComment(testClass);

        final String methodToWrite = MethodReference.getName((MethodReference.SerializableConsumer<String[]>) doc::write);

        doc.write("This is an example to create a simple test using `" + ApprovalsExtension.class.getSimpleName() + "`.",
                "",
                "You have to write a class and add register an `" + ApprovalsExtension.class.getSimpleName() + "` attribute using `" + RegisterExtension.class.getSimpleName() + "` annotation.",
                "This extension will check that everything wrote using `" + methodToWrite + "` method has not changed since the last execution.",
                "", "");

        doc.write(".Test example using `" + ApprovalsExtension.class.getSimpleName() + "`", extractSourceWithTag(testClass.getSimpleName(), testClass), "", "");

        final Method method = MethodReference.getMethod(OneTest::test_A);
        final Path approvedPath = new DocPath(method).approved().from(this.getClass());
        final Path receivedPath = new DocPath(method).received().from(this.getClass());
        doc.write("When executing test method `" + method.getName() + "`, the following text is generated.",
                "----",
                formatter.include(approvedPath.toString()),
                "----",
                "If this content is identical to the `" + approvedPath.getFileName() + "`, then the test is a success.",
                "Otherwise, test fails and the generated text is written to the `" + receivedPath.getFileName() + "` file.",
                "So we can compare those two files to see what has changed.",
                "",
                "File name and title come from method name.", "The chapter content contains what was written using `" + methodToWrite + "`.");

        final Path docFolder = new DocPath(method).approved().folder();

        try {
            final String filesInDocFolder = Files.list(docFolder)
                    .map(f -> "* " + f.getFileName().toString())
                    .sorted()
                    .collect(Collectors.joining("\n"));

            doc.write("", "",
                    String.format("Files are stored in `%s` directory which contains:", DocPath.toAsciiDoc(docFolder)),
                    "",
                    filesInDocFolder,
                    "",
                    "There is one file per test and one file for the class.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    @Test
    public void using_displayName() throws IOException {
        final Class<?> testClass = UsingDisplayNameTest.class;
        doc.runTestAndWriteResultAsComment(testClass);

        final OnePath approvedTestA = new DocPath(MethodReference.getMethod(UsingDisplayNameTest::test_A)).approved();

        doc.write("You can use DisplayName annotation to customize test title",
                "",
                ".Test example using DisplayName",
                extractSourceWithTag(testClass.getSimpleName(), testClass),
                "",
                ".Generated file with DisplayName content as title",
                formatter.blockBuilder(Formatter.Block.CODE)
                        .content(formatter.include(approvedTestA.filename()))
                        .build());
    }

    @Test
    @DisplayName("Hide title")
    public void hide_title(TestInfo testInfo) {

        final Class<?> testClass = MyTestWithoutTitleOnOneTest.class;
        doc.runTestAndWriteResultAsComment(testClass);

        final DocPath docPath = new DocPath(testClass);
        final String methodId = doc.getDocWriter().titleId(testInfo.getTestMethod().get());
        doc.write("You can hide a test title adding the " + NoTitle.class.getSimpleName() + " annotation to the test.",
                "It's usefull to reuse content of a test in different place without keeping the original structure.",
                "You also want to separate the content of one chapter through different test but display them under only title.",
                "",
                ".Test example using hiding title",
                formatter.sourceCode(CodeExtractor.classSource(testClass)),
                "",
                "The file generated from the method with `" + NoTitle.class.getSimpleName() + "` annotation has no header.",
                "On the final rendering, it's like it was part of the previous chapter.",
                "",
                "[.rendering]",
                "== Rendering of the result",
                formatter.include(docPath.approved().from(this.getClass()).toString(), 2)
        );
    }

    @Test
    public void using_a_custom_writer() {
        final Class<?> testClass = MyCustomWriterTest.class;

        doc.runTestAndWriteResultAsComment(testClass);

        // You have to write a class and add RegisterExtension annotation on an attribute. This extension will check that content of DocWriter has not changed since the last time. DocWriter passed to the ApprovalsExtension is used to indicated what we want to write to the output.

        doc.write("It's possible to give a specific DocWriter to `" + ApprovalsExtension.class.getSimpleName() + "`",
                "and modify what it will be written in the final document.",
                "", "");

        doc.write(".Test example using `" + ApprovalsExtension.class.getSimpleName() + "`", extractSourceWithTag(testClass.getSimpleName(), testClass), "", "");

        final Method method = MethodReference.getMethod(MyCustomWriterTest::test_A);
        final Path approvedPath = new DocPath(method).approved().from(this.getClass());
        doc.write("When executing test method `" + method.getName() + "`, a file `" + approvedPath.getFileName() + "` is generated and contains the following text",
                "----",
                formatter.include(approvedPath.toString()),
                "----");
    }

    @Test
    public void using_a_custom_formatter() {
        final Class<?> testClass = MyCustomFormatterTest.class;

        doc.runTestAndWriteResultAsComment(testClass);

        doc.write("It's possible to give a specific Formatter to `" + ApprovalsExtension.class.getSimpleName() + "`",
                "and change the rendering for some instructions or create another formatter.",
                "", "");

        doc.write(".Test example using `" + ApprovalsExtension.class.getSimpleName() + "`", extractSourceWithTag(testClass.getSimpleName(), testClass), "", "");

        final Method method = MethodReference.getMethod(MyCustomFormatterTest::test_A);
        final Path approvedPath = new DocPath(method).approved().from(this.getClass());
        doc.write("When executing test method `" + method.getName() + "`, a file `" + approvedPath.getFileName() + "` is generated and contains the following text",
                "----",
                formatter.include(approvedPath.toString()),
                "----");
    }

    @Test
    public void nested_class() {
        try {
            doc.write("Nested class can be used to organize tests.", "Each nested class create a nested title.", "");

            final Class<?> testClass = DemoNestedTest.class;
            doc.runTestAndWriteResultAsComment(testClass);

            doc.write("", "", ".Test example using nested class", extractSourceWithTag(testClass.getSimpleName(), testClass), "", "");

            final Path generatedFilePath = Paths.get("", getClass().getPackage().getName().split("\\."));
            doc.write("Generated files in `" + DocPath.toAsciiDoc(generatedFilePath) + "`:", "", Files.list(doc.getDocPath().resolve(generatedFilePath))
                    .map(file -> file.getFileName().toString())
                    .filter(filename -> filename.startsWith("_" + DemoNestedTest.class.getSimpleName() + "."))
                    .filter((filename -> filename.endsWith(".approved.adoc")))
                    .sorted()
                    .map(filename -> "* " + filename)
                    .collect(Collectors.joining("\n\n")));

            final OnePath approved = new DocPath(testClass).approved();
            doc.write("", "", ".Document generated", "----", Files.lines(approved.path())
                    .collect(Collectors.joining("\n"))
                    .replaceAll("\\ninclude::", "\n\\\\include::"), "----");

            doc.write("", "",
                    "_Final rendering_",
                    "[.rendering]",
                    "== Rendering",
                    "",
                    formatter.include("_" + testClass.getSimpleName() + ".approved.adoc", 2)
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void document_with_all_tests_in_a_testclass() throws IOException {
        doc.write("At the end of a test, a file is created including files generated on each test.", "", "`" + ApprovalsExtension.class.getSimpleName() + "` must be static to be able to run `" + AfterAll.class.getSimpleName() + "` callback.");

        final Class<?> testClass = MyTest.class;
        doc.runTestAndWriteResultAsComment(testClass);

        doc.write("", "", ".Test example used to generate class document", extractSourceWithTag(testClass.getSimpleName(), testClass), "", "");

        doc.write("Class `" + testClass.getSimpleName() + "` is in package `" + testClass.getPackage().getName() + "`.");
        final OnePath approved = new DocPath(MyTest.class).approved();

        doc.write("", "", adocFileSourceEscaped(approved));

        doc.write("", "",
                "_Final rendering_",
                "[.rendering]",
                formatter.include(approved.from(this.getClass()).toString(), 1));
    }


    @Test
    public void failing_test_output(TestInfo info) throws IOException {
        doc.write("When a test fails, the error is written in the final document.",
                "It's help to understand and investigate on the problem.",
                "",
                "When the test fails, the reason (exception) is written into the generated document.", "");

        final Class<?> testClass = FailingTest.class;
        doc.runTestAndWriteResultAsComment(testClass);

        doc.write("", "", ".Test example used to generate class document", extractSourceWithTag(testClass.getSimpleName(), testClass), "", "");

        final Method method = MethodReference.getMethod(FailingTest::failing_test);
        final DocPath docPath = new DocPath(method);
        final Path documentationPath = docPath.received().path();

        Predicate<String> isStackLine = line -> line.startsWith("	at ");

        // We include stack trace from a file that is not approved to avoid a failure if it changes from on execution from another.

        final String stackTraceFileName = String.format("_%s.ExceptionStackTrace.adoc",
                new DocPath(info.getTestMethod().get()).name());

        final Path stackTraceFile = new DocPath(this.getClass()).approved().folder().resolve(stackTraceFileName);
        try (FileWriter fileWriter = new FileWriter(stackTraceFile.toFile())) {
            final String stackTraceContent = Files.lines(documentationPath)
                    .filter(isStackLine)
                    .collect(Collectors.joining("\n"));

            fileWriter.write(stackTraceContent);
        }

        List<String> cutLines = new ArrayList<>();
        final String INCLUDE_KEYWORD_TO_SUBSTITUTE = "INCLUDE_STACKTRACE_HERE";
        boolean stackTraceFileAlreadyIncluded = false;
        for (String line : Files.lines(documentationPath).collect(Collectors.toList())) {
            if (isStackLine.test(line)) {
                if (stackTraceFileAlreadyIncluded == false) {
                    stackTraceFileAlreadyIncluded = true;
                    cutLines.add(INCLUDE_KEYWORD_TO_SUBSTITUTE);
                }
            } else {
                cutLines.add(line);
            }
        }

        final int numberOfstackTraceLinesToDisplay = 3;
        final String include_stacktrace_asciidoc = "include::" + stackTraceFileName + "[lines=1.." + (numberOfstackTraceLinesToDisplay) + "]\n\t...";
        doc.write("", "",
                ".Document generated (exception stack trace is truncated)",
                "------",
                cutLines.stream()
                        .map(this::escapedAdocSpecialKeywords)
                        .collect(Collectors.joining("\n"))
                        .replace(INCLUDE_KEYWORD_TO_SUBSTITUTE, include_stacktrace_asciidoc),
                "------");

        doc.write("",
                "",
                "_Final rendering_",
                "[.rendering]",
                "--",
                cutLines.stream()
                        .map(this::escapedAdocTitle)
                        .collect(Collectors.joining("\n"))
                        .replace(INCLUDE_KEYWORD_TO_SUBSTITUTE, include_stacktrace_asciidoc),
                "--"
        );

        final DocPath classDocPath = new DocPath(FailingTest.class);
        doc.write("", "",
                "A received file is produced for the class containing the failing test.",
                "This file includes received files if they exist.",
                "It is thus possible to visualize the received file in the file with other methods.",
                "",
                adocFileSourceEscaped(classDocPath.received())
        );
    }

    private String extractSourceWithTag(String tag, Class<?> classToIdentifySourceClass, Class<?> testClass) {
        final Class<?> mainFileClass = new ClassFinder().getMainFileClass(classToIdentifySourceClass);
        final Path path = new DocPath(mainFileClass).test().path();
        final String javaSource = CodeExtractor.extractPartOfFile(path, tag)
                .replaceAll("(^|\n)@" + NotIncludeToDoc.class.getSimpleName(), "")
                .replaceAll("(^|\n)@" + OnlyRunProgrammatically.class.getSimpleName(), "");
        return formatter.sourceCode(javaSource.trim()).trim();
    }

    private String extractSourceWithTag(String tag, Class<?> testClass) {
        return extractSourceWithTag(tag, testClass, testClass);
    }

    private String adocFileSourceEscaped(OnePath approved) throws IOException {
        final String collect = Files.lines(approved.path())
                .collect(Collectors.joining("\n"));
        return String.join("\n",
                ".Document generated",
                "----",
                escapedAdocSpecialKeywords(collect),
                "----");
    }

    private String escapedAdocSpecialKeywords(String collect) {
        return collect
                .replaceAll("(^|\\n)include::", "$1\\\\include::")
                .replaceAll("(^|\\n)ifndef::", "$1\\\\ifndef::")
                .replaceAll("(^|\\n)ifdef::", "$1\\\\ifdef::")
                .replaceAll("(^|\\n)endif::", "$1\\\\endif::");
    }

    private String escapedAdocTitle(String line) {
        return line.replaceAll("^= (.*)", "[.title1]#$1#");
    }


}



