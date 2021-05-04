package org.sfvl.howto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.ApprovalsExtensionTest;
import org.sfvl.doctesting.junitextension.ClassToDocument;
import org.sfvl.doctesting.junitextension.FindLambdaMethod;
import org.sfvl.doctesting.utils.CodeExtractor;
import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.utils.DocWriter;
import org.sfvl.doctesting.utils.DocumentationNamer;
import org.sfvl.doctesting.writer.ClassDocumentation;
import org.sfvl.doctesting.writer.Classes;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

public class HowTo {

    private static final DocWriter doc = new DocWriter() {
        public String formatOutput(String displayName, Method testMethod) {
            return String.join("",
                    CodeExtractor.getComment(testMethod).map(comment -> comment + "\n\n").orElse(""),
                    read());
        }
    };

    @RegisterExtension
    static ApprovalsExtension extension = new ApprovalsExtension(doc) {
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
            final DocumentationNamer documentationNamer = new DocumentationNamer(getDocPath(), testClass);

            verifyDoc(content, documentationNamer);
        }
    };

    private final AsciidocFormatter formatter = new AsciidocFormatter();

    @Test
    public void create_a_test() {
        doc.write(getInclude(ApprovalsExtensionTest::using_extension, 0));
    }

    @Test
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
    public void create_a_document() {
        doc.write(getInclude(CreateADocument.class, 0));
    }

    @Test
    public void use_your_own_style() {
        doc.write(getInclude(UseYourOwnStyle.class, 0));
    }

    public String getInclude(Class aClass, int offset) {
        return new Classes(formatter).includeClasses(DocumentationNamer.toPath(aClass.getPackage()), Arrays.asList(aClass), offset).trim();
    }

    public <T> String getInclude(FindLambdaMethod.SerializableConsumer<T> methodToInclude, int offset) {
        final Method method = FindLambdaMethod.getMethod(methodToInclude);
        final DocumentationNamer documentationNamer = new DocumentationNamer(Config.DOC_PATH, method);
        return formatter.include(
                documentationNamer.getApprovedPath(Config.DOC_PATH.resolve(DocumentationNamer.toPath(this.getClass().getPackage()))).toString(), offset);

    }
}