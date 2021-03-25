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
class BuilderDocumentationTest {

    private static final DocWriter doc = new DocWriter();
    @RegisterExtension
    static ApprovalsExtension extension = new ApprovalsExtension(doc);

    @Test
    public void simple_doc(TestInfo testInfo) {
        // >>>1
        final Class[] classesToAdd = {
                org.sfvl.doctesting.sample.basic.FirstTest.class,
                org.sfvl.doctesting.sample.basic.SecondTest.class
        };

        final BuilderDocumentation document = new BuilderDocumentation("My title")
                .withClassesToInclude(classesToAdd);

        final String content = document.getDoc();
        // <<<1

        writeDoc(testInfo, content);
    }

    /**
     * All links are relative to a path corresponding to the location where the document will be written.
     */
    @Nested
    class RelativizedToPath {
        @Test
        public void from_a_package(TestInfo testInfo) {
            // >>>1
            final Class[] classesToAdd = {
                    org.sfvl.doctesting.sample.basic.FirstTest.class,
                    org.sfvl.docformatter.AsciidocFormatterTest.class
            };

            final BuilderDocumentation document = new BuilderDocumentation("My title")
                    .withClassesToInclude(classesToAdd)
                    .withLocation(org.sfvl.docformatter.Formatter.class.getPackage());
            final String content = document.getDoc();
            // <<<1

            doc.write("Path to file is relativized to the given package.", "");
            writeDoc(testInfo, content);
        }

        @Test
        public void from_a_path(TestInfo testInfo) {
            // >>>1
            final Class[] classesToAdd = {
                    org.sfvl.doctesting.sample.basic.FirstTest.class,
                    org.sfvl.docformatter.AsciidocFormatterTest.class
            };

            final BuilderDocumentation document = new BuilderDocumentation("My title")
                    .withClassesToInclude(classesToAdd)
                    .withLocation(Paths.get("org", "sfvl", "docformatter"))
                    .withStructure(
                            builder -> "Documentation of classes:",
                            BuilderDocumentation::includeClasses,
                            builder -> "This is my footer"
                    );
            final String content = document.getDoc();
            // <<<1

            doc.write("We can change document structure to organize it as we want.",
                    "",
                    "In this example, we display only classes includes and we add text before and after them.");
            writeDoc(testInfo, content);
        }
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