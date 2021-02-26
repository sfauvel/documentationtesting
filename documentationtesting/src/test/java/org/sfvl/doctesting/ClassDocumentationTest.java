package org.sfvl.doctesting;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.ClassToDocument;
import org.sfvl.doctesting.junitextension.FindLambdaMethod;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@ClassToDocument(clazz = ClassDocumentation.class)
class ClassDocumentationTest {

    private final Formatter formatter = new AsciidocFormatter();
    private final DocWriter doc = new DocWriter();
    @RegisterExtension
    ApprovalsExtension extension = new ApprovalsExtension(doc);

    final List<Method> methodsToDocument = Arrays.asList(
            FindLambdaMethod.getMethod(InMainDocTest::testA),
            FindLambdaMethod.getMethod(InMainDocTest::testB),
            FindLambdaMethod.getMethod(InMainDocBisTest::testX)
    );

    static class DocumentationOnSpecificMethods extends ClassDocumentation {

        private final List<Method> methods;

        public DocumentationOnSpecificMethods(List<Method> methods) {
            super((m, p) -> {
                        System.out.println("Transform: " + m.getName() + " to " + Paths.get(m.getName() + ".approved.adoc").toString());
                        return Paths.get(m.getName() + ".approved.adoc");
                    },
                    new AsciidocFormatter());
            this.methods = methods;
        }

        @Override
        protected String includeMethods(List<Method> testMethods, Function<Method, Path> targetPathName, int levelOffset) {
            return escapeIncludeInstruction(super.includeMethods(testMethods, targetPathName, levelOffset));
        }
    }

    @Test
    @DisplayName(value = "Default test class documentation")
    public void default_class_documentation(TestInfo testInfo) throws IOException {

        // >>>1
        final ClassDocumentation defaultDocumentation = new ClassDocumentation(
                (m, p) -> p,
                new AsciidocFormatter());

        final String defaultContent = defaultDocumentation.getClassDocumentation(
                InMainDocTest.class,                                     // <1>
                Arrays.asList(InMainDocTest.class.getDeclaredMethods()), // <2>
                m -> Paths.get(m.getName() + ".approved.adoc"),     // <3>
                1);                                                // <4>
        // <<<1

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
                "<2> Methods to include to the documentation",
                "<3> Mapping from the method name to the file name to include",
                "<4> Title depth",
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

        // >>>1
        final ClassDocumentation defaultDocumentation = new ClassDocumentation(
                (m, p) -> p,
                new AsciidocFormatter());

        final String defaultContent = defaultDocumentation.getClassDocumentation(
                InMainDocTest.class,
                Collections.emptyList(),
                m -> Paths.get(m.getName() + ".approved.adoc"),
                3);                                                // <1>
        // <<<1

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
        final Path sourcePath = Paths.get("src", "test", "java");

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

        final ClassDocumentation defaultDocumentation = new DocumentationOnSpecificMethods(methodsToDocument);

        final ClassDocumentation customDocumentation = new DocumentationOnSpecificMethods(methodsToDocument) {

            // >>>1
            @Override
            protected String getTestClassTitle(Class<?> clazz) {
                return "from getTestClassTitle method " + clazz.getSimpleName();
            }

            @Override
            protected String getDescription(Class<?> classToDocument) {
                return "from getDescription method " + classToDocument.getSimpleName();
            }
            // <<<1
        };

        final String customContent = customDocumentation.getClassDocumentation(
                InMainDocTest.class,
                Arrays.asList(InMainDocTest.class.getDeclaredMethods().clone()),
                m -> Paths.get(m.getName() + ".approved.adoc"),
                1);

        final String defaultContent = defaultDocumentation.getClassDocumentation(
                InMainDocTest.class,
                Arrays.asList(InMainDocTest.class.getDeclaredMethods().clone()),
                m -> Paths.get(m.getName() + ".approved.adoc"),
                1);

        Method method = Arrays.stream(ClassDocumentation.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("getClassDocumentation"))
                .findFirst().get();

        doc.write(CodeExtractor.getComment(method).orElse(""), "");

        doc.write("",
                formatter.sourceCodeBuilder()
                        .title("Default document generated")
                        .content(defaultContent)
                        .build());

        doc.write("",
                formatter.sourceCodeBuilder("java")
                        .title("ClassDocumentation methods overwritten")
                        .source(CodeExtractor.extractPartOfMethod(testInfo.getTestMethod().get(), "1"))
                        .build());

        doc.write("",
                formatter.sourceCodeBuilder()
                        .title("Custom document generated")
                        .content(customContent)
                        .build());
    }

}