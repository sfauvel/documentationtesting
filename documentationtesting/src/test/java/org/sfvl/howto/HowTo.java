package org.sfvl.howto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.junitextension.*;
import org.sfvl.doctesting.utils.*;
import org.sfvl.doctesting.writer.ClassDocumentation;
import org.sfvl.doctesting.writer.Classes;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

public class HowTo {

    @RegisterExtension
    static ApprovalsExtension doc = new SimpleApprovalsExtension() {
        @Override
        public void afterAll(ExtensionContext extensionContext) throws Exception {
            final Class<?> currentClass = extensionContext.getTestClass().get();
//            if (isNestedClass(currentClass)) {
//                return;
//            }
            final ClassDocumentation classDocumentation = new ClassDocumentation() {
                protected Optional<String> relatedClassDescription(Class<?> fromClass) {
                    return Optional.ofNullable(fromClass.getAnnotation(ClassToDocument.class))
                            .map(ClassToDocument::clazz)
                            .map(CodeExtractor::getComment);
                }
            };
            final String content = String.join("\n",
                    ":toc: left",
                    ":nofooter:",
                    ":stem:",
                    ":source-highlighter: rouge",
                    classDocumentation.getClassDocumentation(currentClass)
            );
            final Class<?> testClass = extensionContext.getTestClass().get();

            verifyDoc(content, new DocPath(testClass));
        }
    };

    private final AsciidocFormatter formatter = new AsciidocFormatter();

    @Test
    public void getting_started() {

        doc.write("To get started quickly, you can download link:https://github.com/sfauvel/TryDocAsTest[Try doc as test] project.",
                "It's a minimal project that is ready to use and which implementing a small demo.",
                "",
                "If you want to use it on your own project, you need to:",
                "",
                "* " + linkToClass(InstallingLibrary.class, "Installing DocumentationTesting").trim() + " maven library and add dependency to your `pom.xml`",
                "",
                "* Create a test and register ApprovalsExtension extension adding the code below to the test class.",
                "[source,java,indent=0]",
                "----",
                "private static final DocWriter doc = new DocWriter();",
                "@RegisterExtension",
                "static ApprovalsExtension extension = new ApprovalsExtension(docWriter);",
                "----",
                "",
                "* Write in your test everything you want to see in your documentation using `doc.write(\"...\")`",
                "You don't have to write assertions, tests will be passed when generated documents are the same as the last time.\n");

    }

    private void generatePage(Class<?> clazz) throws IOException {
        generatePage(new DocPath(clazz));
    }

    private void generatePage(DocPath docPath) throws IOException {
        String includeContent = String.join("\n",
                ":toc: left",
                ":nofooter:",
                ":stem:",
                ":source-highlighter: rouge",
                ":toclevels: 4",
                "",
                String.format("include::%s[]", docPath.approved().from(this.getClass())));

        try (FileWriter fileWriter = new FileWriter(docPath.page().path().toFile())) {
            fileWriter.write(includeContent);
        }
    }

    private String linkToClass(Class<?> clazz) {

        final String className = clazz.getSimpleName();
        final String title = className.substring(0, 1) +
                className.substring(1)
                        .replaceAll("([A-Z])", " $1")
                        .toLowerCase();

        return linkToClass(clazz, title);
    }

    private String linkToClass(Class<?> clazz, String title) {
        final DocPath docPath = new DocPath(clazz);
        try {
            generatePage(docPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return String.format("link:%s[%s]\n",
                docPath.doc().from(new DocPath(this.getClass()).doc()),
                title);
    }

    @Test
    @NoTitle
    public void create_a_test() {
        doc.write(getInclude(ApprovalsExtensionTest::using_extension, 0));
    }

    @Test
    @NoTitle
    public void format_text() {
        // >>>
        Formatter formatter = new AsciidocFormatter();
        final String text = formatter.listItems("Item A", "Item B", "Item C");
        // <<<
        doc.write(formatter.title(1, "Format text"),
                "",
                "To format text, we can use a formatter that hide technical syntax of the language used.",
                "",
                ".Formatter usage",
                CodeExtractor.extractPartOfCurrentMethod(),
                "",
                formatter.blockBuilder("----")
                        .title("Text generated")
                        .content(text)
                        .build(),
                "",
                formatter.blockBuilder("====")
                        .title("Final rendering")
                        .content(text)
                        .build());
    }

    @Test
    @NoTitle
    public void create_a_document() {
        doc.write(getInclude(CreateADocument.class, 0));
    }

    @Test
    @NoTitle
    public void use_your_own_style() {
        doc.write(getInclude(UseYourOwnStyle.class, 0));
    }

    public String getInclude(Class aClass, int offset) {
        return new Classes(formatter).includeClasses(DocPath.toPath(aClass.getPackage()), Arrays.asList(aClass), offset).trim();
    }

    public <T> String getInclude(FindLambdaMethod.SerializableConsumer<T> methodToInclude, int offset) {
        final Method method = FindLambdaMethod.getMethod(methodToInclude);

        final OnePath approvedPath = new DocPath(method).approved();
        return formatter.include(approvedPath.from(new DocPath(this.getClass()).approved()).toString(), offset);
    }
}