package org.sfvl.doctesting.writer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.NotIncludeToDoc;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.ClassToDocument;
import org.sfvl.doctesting.utils.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

@ClassToDocument(clazz = ClassDocumentation.class)
class ClassDocumentationTest {

    private final Formatter formatter = new AsciidocFormatter();
    private static final DocWriter doc = new DocWriter();
    @RegisterExtension
    static ApprovalsExtension extension = new ApprovalsExtension(doc);

    @Test
    @DisplayName(value = "Default test class documentation")
    public void default_class_documentation(TestInfo testInfo) throws IOException {

        // >>>
        final ClassDocumentation defaultDocumentation = new ClassDocumentation();
        final String defaultContent = defaultDocumentation.getClassDocumentation(
                InMainDocTest.class                                              // <1>
        );
        // <<<

        Method method = Arrays.stream(ClassDocumentation.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("getClassDocumentation"))
                .findFirst().get();

        doc.write(CodeExtractor.getComment(method).orElse(""), "");

        doc.write("",
                formatter.sourceCodeBuilder("java")
                        .title("Code used")
                        .source(CodeExtractor.extractPartOfMethod(testInfo.getTestMethod().get()))
                        .build(),
                "",
                "<1> Class to document",
                "");

        doc.write("",
                formatter.sourceCodeBuilder()
                        .title("Document generated")
                        .source(escapeIncludeInstruction(defaultContent))
                        .build(), "");

        doc.write("",
                "Document generated from a class has class name(or DisplayName) as title.",
                "The class comment is used as description text under the title.",
                "An include instruction is added for each method specified.", "");

        doc.write("",
                formatter.sourceCodeBuilder("java")
                        .title("Test class used to in this examples")
                        .source(extractSourceFromFile(InMainDocTest.class))
                        .build(), "");
    }

    /**
     * We can define the title level.
     */
    @Test
    public void title_level(TestInfo testInfo) throws IOException {

        // >>>
        final ClassDocumentation defaultDocumentation = new ClassDocumentation();

        final String defaultContent = defaultDocumentation.getClassDocumentation(
                InMainDocTest.class,
                3);   // <1>
        // <<<

        doc.write(formatter.sourceCodeBuilder("java")
                        .title("Code used")
                        .source(CodeExtractor.extractPartOfMethod(testInfo.getTestMethod().get()))
                        .build(),
                "",
                "<1> Level used for title",
                "", "");

        doc.write(formatter.sourceCodeBuilder()
                .title("Title generated with a specific level")
                .source(escapeIncludeInstruction(defaultContent))
                .build());
    }

    public String extractSourceFromFile(Class<?> clazz) throws IOException {
        final Path sourcePath = Config.TEST_PATH;

        return Files.lines(sourcePath.resolve(Paths.get(
                clazz.getPackage().getName().replace(".", "/"),
                clazz.getSimpleName() + ".java")))
                .filter(line -> !line.startsWith("@" + NotIncludeToDoc.class.getSimpleName()))
                .filter(line -> !line.startsWith("package"))
                .filter(line -> !line.startsWith("import"))
                .collect(Collectors.joining("\n"));
    }

    public static String escapeIncludeInstruction(String defaultContent) {
        return defaultContent.replaceAll("(^|\\n)include", "\n\\\\include");
    }

    @Test
    public void customize_output(TestInfo testInfo) {

        final ClassDocumentation defaultDocumentation = new ClassDocumentation();

        final ClassDocumentation customDocumentation = new ClassDocumentation() {

            // >>>1
            @Override
            protected String getTestClassTitle(Class<?> classToDocument) {
                return "Title from getTestClassTitle method " + classToDocument.getSimpleName();
            }

            @Override
            protected String getDescription(Class<?> classToDocument) {
                return "Description from getDescription method " + classToDocument.getSimpleName();
            }
            // <<<1
        };
        final String customContent = customDocumentation.getClassDocumentation(InMainDocTest.class);

        final String defaultContent = defaultDocumentation.getClassDocumentation(InMainDocTest.class);

        doc.write("Title and description could be changed by overriding the appropriate methods.", "");

        doc.write("",
                formatter.sourceCodeBuilder()
                        .title("Default document generated")
                        .content(escapeIncludeInstruction(defaultContent))
                        .build());

        doc.write("",
                formatter.sourceCodeBuilder("java")
                        .title("ClassDocumentation methods overwritten")
                        .source(CodeExtractor.extractPartOfMethod(testInfo.getTestMethod().get(), "1"))
                        .build());

        doc.write("",
                formatter.sourceCodeBuilder()
                        .title("Custom document generated")
                        .content(escapeIncludeInstruction(customContent))
                        .build());
    }

    @Test
    @DisplayName(value = "Test class documentation with nested classes")
    public void nested_class_documentation(TestInfo testInfo) throws IOException {

        // >>>
        final ClassDocumentation defaultDocumentation = new ClassDocumentation();

        final Class<?> testClass = ClassDocumentationTest_DemoNestedTest.class;
        final String defaultContent = defaultDocumentation.getClassDocumentation(testClass);
        // <<<

        doc.write("",
                formatter.sourceCodeBuilder("java")
                        .title("Code used")
                        .source(CodeExtractor.extractPartOfMethod(testInfo.getTestMethod().get()))
                        .build(),
                "",
                "");

        doc.write("",
                formatter.sourceCodeBuilder()
                        .title("Document generated")
                        .source(escapeIncludeInstruction(defaultContent))
                        .build(), "");

        doc.write(".Test example using `" + ApprovalsExtension.class.getSimpleName() + "`",
                includeSourceWithTag(testClass.getSimpleName()),
                "", "");

    }

    public String includeSourceWithTag(String tag) {

        final Path projectPath = new PathProvider().getProjectPath();
        final Path packagePath = DocumentationNamer.toPath(this.getClass().getPackage());
        final Path packageDocPath = extension.getDocPath().resolve(packagePath);
        final Path relativizeToProjectPath = packageDocPath.relativize(projectPath);
        final Path javaFilePath = relativizeToProjectPath
                .resolve(Config.TEST_PATH)
                .resolve(packagePath)
                .resolve(this.getClass().getSimpleName() + ".java");

        return String.join("\n",
                "[source, java, indent=0]",
                "----",
                String.format("include::%s[tag=%s]",
                        javaFilePath.toString(),
                        tag),
                "----");
    }
}


// tag::ClassDocumentationTest_DemoNestedTest[]

/**
 * Demo of a simple usage to generate documentation.
 */
@NotIncludeToDoc
class ClassDocumentationTest_DemoNestedTest {
    private static final DocWriter writer = new DocWriter();

    @RegisterExtension
    static final ApprovalsExtension extension = new ApprovalsExtension(writer);

    /**
     * Document of Addition operations.
     */
    @Nested
    class Adding {

        @Test
        @DisplayName("Adding 2 simple numbers")
        public void should_be_5_when_adding_2_and_3() {
            writer.write(String.format("%d + %d = %d", 2, 3, 2 + 3));
        }

        /**
         * A nested test.
         */
        @Test
        @DisplayName("Adding 3 simple numbers")
        public void should_be_9_when_adding_2_3_and_4() {
            writer.write(String.format("%d + %d + %d = %d", 2, 3, 4, 2 + 3 + 4));
        }

        /**
         * Addition negative numbers.
         */
        @Nested
        class AddingNegativeNumber {
            @Test
            @DisplayName("Adding 2 negative numbers")
            public void should_be_minus_8_when_adding_minus_3_and_minus_5() {
                writer.write(String.format("%d + %d = %d", -3, -5, (-3) + (-5)));
            }
        }
    }

    /**
     * A method between two nested classes is between those classes in final documentatiton.
     */
    @Test
    public void method_between_two_nested_classes() {
        writer.write("This is the documentation generated in test");
    }

    @Nested
    class Multiply {
        @Test
        @DisplayName("Multiply 2 simple numbers")
        public void should_be_12_when_multiply_4_and_3() {
            writer.write(String.format("%d * %d = %d", 4, 3, 4 * 3));
        }
    }
}
// end::ClassDocumentationTest_DemoNestedTest[]