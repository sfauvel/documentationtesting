package org.sfvl.doctesting.writer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.doctesting.NotIncludeToDoc;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.ClassToDocument;
import org.sfvl.doctesting.junitextension.FindLambdaMethod;
import org.sfvl.doctesting.utils.CodeExtractor;
import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.utils.DocWriter;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@DisplayName("Create a general documentation")
@ClassToDocument(clazz = MainDocumentation.class)
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
                    Config.DOC_PATH,
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
    public void by_default(TestInfo testInfo) {
        // >>>1
        final MainDocumentation doc = new MainDocumentation("Documentation",
                Config.DOC_PATH,
                new AsciidocFormatter());

        Path docFilePath = doc.getDocRootPath();
        final String packageToScan = "org.sfvl.doctesting.sample.basic";
        final String content = doc.getDocumentationContent(packageToScan, docFilePath);
        // <<<1

        final Path packagePath = Config.TEST_PATH.resolve(packageToScan.replaceAll("\\.", "/"));
        final Stream<String> fileStream = Arrays.stream(packagePath.toFile().listFiles())
                .filter(File::isFile)
                .map(File::getName);

        MainDocumentationTest.doc.write("", ".Usage", "[source, java, indent=0]",
                "----",
                CodeExtractor.extractPartOfMethod(testInfo.getTestMethod().get(), "1"),
                "----",
                "");

        MainDocumentationTest.doc.write(String.format(".Files contained in the folder `%s`", packagePath),
                fileStream.map(f -> "* " + f)
                        .collect(Collectors.joining("\n", "", "\n")));

        MainDocumentationTest.doc.write("", ".Default document generated", "----", content.replaceAll("\\ninclude", "\n\\\\include"), "----");
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