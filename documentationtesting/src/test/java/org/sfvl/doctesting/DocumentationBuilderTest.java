package org.sfvl.doctesting;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.ClassToDocument;

import java.nio.file.Paths;

@DisplayName("Create a general documentation")
@ClassToDocument(clazz = MainDocumentation.class)
class DocumentationBuilderTest {

    private static final DocWriter doc = new DocWriter();
    @RegisterExtension
    static ApprovalsExtension extension = new ApprovalsExtension(doc);

    @Test
    public void simple_doc(TestInfo testInfo) {
        // >>>1
        Class[] classesToAdd = {
                org.sfvl.doctesting.sample.basic.FirstTest.class,
                org.sfvl.doctesting.sample.basic.SecondTest.class
        };

        DocumentationBuilder builder = new DocumentationBuilder("My title")
                .withClassesToInclude(classesToAdd);

        String document = builder.build();
        // <<<1

        writeDoc(testInfo, document);
    }

    /**
     * All links are relative to a path corresponding to the location where the document will be written.
     */
    @Nested
    class RelativizedToPath {
        @Test
        public void from_a_package(TestInfo testInfo) {
            // >>>1
            Class[] classesToAdd = {
                    org.sfvl.doctesting.sample.basic.FirstTest.class,
                    org.sfvl.docformatter.AsciidocFormatterTest.class
            };

            DocumentationBuilder builder = new DocumentationBuilder("My title")
                    .withClassesToInclude(classesToAdd)
                    .withLocation(org.sfvl.docformatter.Formatter.class.getPackage());
            String document = builder.build();
            // <<<1

            doc.write("Path to file is relativized to the given package.", "");
            writeDoc(testInfo, document);
        }

        @Test
        public void from_a_path(TestInfo testInfo) {
            // >>>1
            Class[] classesToAdd = {
                    org.sfvl.doctesting.sample.basic.FirstTest.class,
                    org.sfvl.docformatter.AsciidocFormatterTest.class
            };

            DocumentationBuilder builder = new DocumentationBuilder("My title")
                    .withClassesToInclude(classesToAdd)
                    .withLocation(Paths.get("org", "sfvl", "docformatter"));
            String document = builder.build();
            // <<<1

            doc.write("Path to file is relativized to the given path.", "");
            writeDoc(testInfo, document);
        }
    }

    @Test
    public void define_document_structure(TestInfo testInfo) {
        // >>>1
        Class[] classesToAdd = {
                org.sfvl.doctesting.sample.basic.FirstTest.class,
                org.sfvl.docformatter.AsciidocFormatterTest.class
        };

        DocumentationBuilder builder = new DocumentationBuilder("My title")
                .withClassesToInclude(classesToAdd)
                .withLocation(Paths.get("org", "sfvl", "docformatter"))
                .withStructure(
                        b -> "Documentation of classes:",
                        DocumentationBuilder::includeClasses,
                        b -> "This is my footer"
                );
        String document = builder.build();
        // <<<1

        doc.write("We can change document structure to organize it as we want.",
                "",
                "In this example, we display only classes includes and we add text before and after them.");
        writeDoc(testInfo, document);
    }

    public void writeDoc(TestInfo testInfo, String content) {
        doc.write("", ".Usage", "[source, java, indent=0]",
                "----",
                CodeExtractor.extractPartOfMethod(testInfo.getTestMethod().get(), "1"),
                "----",
                "");


        doc.write("", ".Document generated",
                "----",
                content.replaceAll("\\ninclude", "\n\\\\include"),
                "----");
    }
}