package org.sfvl.doctesting.writer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.codeextraction.CodePath;
import org.sfvl.docformatter.asciidoc.AsciidocFormatter;
import org.sfvl.doctesting.NotIncludeToDoc;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.utils.ClassToDocument;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.codeextraction.CodeExtractor;
import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.doctesting.utils.PathProvider;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

@ClassToDocument(clazz = ClassDocumentation.class)
class ClassDocumentationTest {

    private final AsciidocFormatter formatter = new AsciidocFormatter();
    @RegisterExtension
    static ApprovalsExtension doc = new SimpleApprovalsExtension();

    @Test
    @DisplayName(value = "Default test class documentation")
    public void default_class_documentation(TestInfo testInfo) throws IOException {

        // >>>
        final ClassDocumentation defaultDocumentation = new ClassDocumentation(
                new AsciidocFormatter()                                          // <1>
        );
        final String defaultContent = defaultDocumentation.getClassDocumentation(
                InMainDocTest.class                                              // <2>
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
                "<1> Formatter to use",
                "<2> Class to document",
                "");

        doc.write("",
                formatter.sourceCodeBuilder()
                        .title("Document generated")
                        .escapeSpecialKeywords()
                        .source(defaultContent)
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
        final ClassDocumentation defaultDocumentation = new ClassDocumentation(new AsciidocFormatter());

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
                .escapeSpecialKeywords()
                .source(defaultContent)
                .build());
    }

    public String extractSourceFromFile(Class<?> clazz) throws IOException {
        return Files.lines(new DocPath(clazz).test().path())
                .filter(line -> !line.startsWith("@" + NotIncludeToDoc.class.getSimpleName()))
                .filter(line -> !line.startsWith("package"))
                .filter(line -> !line.startsWith("import"))
                .collect(Collectors.joining("\n"));
    }

    @Test
    public void customize_output(TestInfo testInfo) {

        final ClassDocumentation defaultDocumentation = new ClassDocumentation(new AsciidocFormatter());

        final ClassDocumentation customDocumentation = new ClassDocumentation(new AsciidocFormatter()) {

            // >>>1
            @Override
            public String getTestClassTitle(Class<?> classToDocument) {
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
                        .escapeSpecialKeywords()
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
                        .escapeSpecialKeywords()
                        .content(customContent)
                        .build());
    }

    @Test
    @DisplayName(value = "Test class documentation with nested classes")
    public void nested_class_documentation(TestInfo testInfo) throws IOException {

        // >>>
        final ClassDocumentation defaultDocumentation = new ClassDocumentation(new AsciidocFormatter());

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
                        .escapeSpecialKeywords()
                        .source(defaultContent)
                        .build(), "");

        doc.write(".Test example using `" + ApprovalsExtension.class.getSimpleName() + "`",
                includeSourceWithTag(testClass.getSimpleName()),
                "", "");

    }

    public String includeSourceWithTag(String tag) {

        final Path projectPath = new PathProvider().getProjectPath();
        final Path packagePath = CodePath.toPath(this.getClass().getPackage());
        final Path packageDocPath = doc.getDocPath().resolve(packagePath);
        final Path relativizeToProjectPath = packageDocPath.relativize(projectPath);
        final Path javaFilePath = relativizeToProjectPath
                .resolve(Config.TEST_PATH)
                .resolve(packagePath)
                .resolve(this.getClass().getSimpleName() + ".java");

        return String.join("\n",
                "[source, java, indent=0]",
                "----",
                formatter.include_with_tag(javaFilePath.toString(), tag),
                "----");
    }
}


// tag::ClassDocumentationTest_DemoNestedTest[]

// end::ClassDocumentationTest_DemoNestedTest[]