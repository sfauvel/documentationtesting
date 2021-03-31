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
@ClassToDocument(clazz = DocumentationBuilder.class)
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

    @Test
    public void simple_doc_with_a_subclass_of_DocumentationBuilder(TestInfo testInfo) {

        // >>>1
        class ExtendedBuilder extends DocumentationBuilder {
            public ExtendedBuilder() {
                super("My extending builder");
            }

            @Override
            protected String getDocumentOptions() {
                return ":no_options:";
            }
        }
        Class[] classesToAdd = {
                org.sfvl.doctesting.sample.basic.FirstTest.class,
                org.sfvl.doctesting.sample.basic.SecondTest.class
        };

        DocumentationBuilder builder = new ExtendedBuilder()
                .withClassesToInclude(classesToAdd);

        String document = builder.build();
        // <<<1

        doc.write("We can subclass a " + DocumentationBuilder.class.getSimpleName() + " to redefine some methods or create new ones.", "");
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
    public void select_options(TestInfo testInfo) {
        // >>>1
        Class[] classesToAdd = {
                org.sfvl.doctesting.sample.basic.FirstTest.class,
                org.sfvl.docformatter.AsciidocFormatterTest.class
        };

        DocumentationBuilder builder = new DocumentationBuilder("My title")
                .withClassesToInclude(classesToAdd)
                .withOptionRemoved("toc")
                .withOptionAdded("noheader")
                .withOptionAdded("source-highlighter", "rouge");
        String document = builder.build();
        // <<<1

        doc.write("Options can be added or removed.", "");
        writeDoc(testInfo, document);
    }

    @Test
    public void define_document_structure(TestInfo testInfo) {
        // >>>1
        Class[] classesToAdd = {
                org.sfvl.doctesting.sample.basic.FirstTest.class,
                org.sfvl.docformatter.AsciidocFormatterTest.class
        };

        DocumentationBuilder builder = new DocumentationBuilder()
                .withClassesToInclude(classesToAdd)
                .withLocation(Paths.get("org", "sfvl", "docformatter"))
                .withStructureBuilder(DocumentationBuilder.class,
                        b -> "Documentation of classes",
                        b -> b.includeClasses(),
                        b -> "This is my footer"
                );

        String document = builder.build();
        // <<<1

        doc.write("We can change document structure to organize it as we want.",
                "",
                "In this example, we display only classes includes and we add text before and after them.");
        writeDoc(testInfo, document);
    }

    /**
     * We need to specify a class when defining document structure.
     * It allows to call methods on that object in lambda.
     * Lambda will be called with the builder instance so this class must be a super class of the builder.
     * There is no verification at compile time, but an exception is thrown when build is called if a wrong type was given.
     */
    @Nested
    class check_class_for_structure {

        class SubClassOfObject {
            public String formatSomething() {
                return "";
            }
        }

        class SubClassOfDocumentationBuilder extends DocumentationBuilder {
            public String formatSomething() {
                return "";
            }
        }
        @Test
        public void with_class_inherits_from_DocumentationBuilder(TestInfo testInfo) {
            try {
                // >>>1
                final Class<SubClassOfDocumentationBuilder> clazz = SubClassOfDocumentationBuilder.class;
                // <<<1
                doc.write("When the given class inherits from a " + DocumentationBuilder.class.getSimpleName() + ".", "", "");
                doc.write("");
                doc.write(String.format("*%s* inherits from *%s*.", clazz.getSimpleName(), clazz.getSuperclass().getSimpleName()), "");
                doc.write("", ".Usage", "[source, java, indent=0]",
                        "----",
                        CodeExtractor.extractPartOfMethod(testInfo.getTestMethod().get(), "1"),
                        "----",
                        "");

                // >>>1
                DocumentationBuilder builder = new SubClassOfDocumentationBuilder()
                        .withStructureBuilder(clazz,
                                b -> b.getDocumentOptions(),
                                b -> b.formatSomething()
                        );
                // <<<1
                doc.write("No error and builder is ready to use.", "");
            } catch (Exception e) {
                doc.write(String.format("Exception was thrown: %s: \n\n.Exception message:\n----\n%s\n----", e.getClass().getSimpleName(), e.getMessage()), "");
            }
        }

        @Test
        public void with_class_not_inherits_from_DocumentationBuilder(TestInfo testInfo) {
            try {
                // >>>1
                final Class<SubClassOfObject> clazz = SubClassOfObject.class;
                // <<<1
                doc.write("When the given class not inherits from a " + DocumentationBuilder.class.getSimpleName() + ".", "", "");
                doc.write(String.format("*%s* inherits from *%s*.", clazz.getSimpleName(), clazz.getSuperclass().getSimpleName()), "");
                doc.write("", ".Usage", "[source, java, indent=0]",
                        "----",
                        CodeExtractor.extractPartOfMethod(testInfo.getTestMethod().get(), "1"),
                        "----",
                        "");

                // >>>1
                DocumentationBuilder builder = new DocumentationBuilder()
                        .withStructureBuilder(clazz, b -> b.formatSomething());
                // <<<1
                doc.write("No error and builder is ready to use.", "");
            } catch (Exception e) {
                doc.write(String.format("Exception was thrown: %s: \n\n.Exception message:\n----\n%s\n----", e.getClass().getSimpleName(), e.getMessage()), "");
            }
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