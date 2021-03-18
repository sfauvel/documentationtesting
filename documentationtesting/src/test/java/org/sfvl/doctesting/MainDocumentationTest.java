package org.sfvl.doctesting;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.FindLambdaMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class MainDocumentationTest {

    private static final DocWriter doc = new DocWriter();
    @RegisterExtension
    static ApprovalsExtension extension = new ApprovalsExtension(doc);

    final List<Method> methodsToDocument = Arrays.asList(
            FindLambdaMethod.getMethod(InMainDocTest::testA),
            FindLambdaMethod.getMethod(InMainDocTest::testB),
            FindLambdaMethod.getMethod(InMainDocBisTest::testX)
    );

    static class DocumentationOnSpecificMethods extends MainDocumentation {

        private final List<Method> methods;

        public DocumentationOnSpecificMethods(List<Method> methods) {
            super("Documentation",
                    Paths.get("src", "test", "docs"),
                    new AsciidocFormatter());
            this.methods = methods;
        }

        @Override
        protected String generalInformation() {
            return "";
        }

        @Override
        protected Set<Method> getAnnotatedMethod(Class<? extends Annotation> annotation, String packageToScan) {
            return new HashSet(methods);
        }

        @Override
        protected String getDocumentationContent(String packageToScan, Path docFilePath) {
            return super.getDocumentationContent(packageToScan, docFilePath)
                    .replaceAll("\\ninclude", "\n\\\\include");
        }
    }

    @Test
    public void document_structure(TestInfo testInfo) {


        final MainDocumentation defaultDocumentation = new DocumentationOnSpecificMethods(methodsToDocument);

        final MainDocumentation customDocumentation = new DocumentationOnSpecificMethods(methodsToDocument) {
            // >>>1
            @Override
            protected String getHeader() {
                return "from getHeader method";
            }
            // <<<1
        };

        Path docFilePath = customDocumentation.getDocRootPath();
        final String customContent = customDocumentation.getDocumentationContent("", docFilePath);

        final String defaultContent = defaultDocumentation.getDocumentationContent("", docFilePath);

        Method method = Arrays.stream(MainDocumentation.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("getDocumentationContent"))
                .findFirst().get();

        doc.write(CodeExtractor.getComment(method).orElse(""), "");

        doc.write("", ".Default document generated", "----", defaultContent, "----");

        doc.write("", ".MainDocumentation methods overwritten", "[source, java, indent=0]",
                "----",
                CodeExtractor.extractPartOfMethod(testInfo.getTestMethod().get(), "1"),
                "----");

        doc.write("", ".Custom document generated", "----", customContent, "----");

        doc.write("", ".Test class used to in this examples", "[source, java, indent=0]",
                "----",
                CodeExtractor.classSource(InMainDocTest.class).replaceAll("@" + NotIncludeToDoc.class.getSimpleName() + "\\s*\\n", ""),
                "",
                CodeExtractor.classSource(InMainDocBisTest.class).replaceAll("@" + NotIncludeToDoc.class.getSimpleName() + "\\s*\\n", ""),
                "----");
    }

    @Test
    public void select_methods_to_add(TestInfo testInfo) {
        final MainDocumentation customDocumentation = new DocumentationOnSpecificMethods(methodsToDocument) {
            // >>>1

            @Override
            protected String getMethodDocumentation(String packageToScan, Path docFilePath) {
                return "== A specific documentation\n\n"
                        + "My description without included any test documentation.";
            }

            // <<<1

        };
        doc.write("", ".MainDocumentation methods overwritten", "[source, java, indent=0]",
                "----",
                CodeExtractor.extractPartOfMethod(testInfo.getTestMethod().get(), "1"),
                "----");

        Path docFilePath = customDocumentation.getDocRootPath();
        final String customContent = customDocumentation.getDocumentationContent("", docFilePath);

        doc.write("", ".Custom document generated", "----", customContent, "----");

    }
}