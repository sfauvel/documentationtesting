package org.sfvl;

import org.sfvl.docformatter.FormatterDocumentation;
import org.sfvl.doctesting.DocTestingDocumentation;
import org.sfvl.doctesting.writer.Document;
import org.sfvl.doctesting.writer.DocumentationBuilder;
import org.sfvl.doctesting.utils.PathProvider;
import org.sfvl.doctesting.junitextension.JUnitExtensionDocumentation;
import org.sfvl.howto.HowToDocumentation;
import org.sfvl.howto.InstallingLibrary;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class DocumentationTestingDocumentation extends DocumentationBuilder {

    /// Record all builder references as a link in documentation.
    private Set<Class<? extends DocumentationBuilder>> buildersToGenerate = new HashSet<>();

    public DocumentationTestingDocumentation() {
        super("Documentation testing");
        withLocation(Paths.get("."));
        withStructureBuilder(DocumentationTestingDocumentation.class,
                b -> b.getDocumentOptions(),
                b -> b.getHeader(),
                b -> b.getContent()
        );
    }

    protected String getHeader() {
        final Path readmePath = new PathProvider().getProjectPath().resolve(Paths.get("readme.adoc"));
        return "\n" + (readmePath.toFile().exists()
                ? "include::../../../readme.adoc[leveloffset=+1]"
                : "= " + getDocumentTitle()) + "\n";
    }

    private String getContent() {
        return String.join("\n",
                "This project is composed of two main packages.",
                "",
                "* " + linkToClass(DocTestingDocumentation.class) + ": Tools to make test validating generated files.",
                "* " + linkToClass(FormatterDocumentation.class) + ": Utilities to format documentation.",
                "",
                "Section " + linkToClass(HowToDocumentation.class) + " shows how to do some common needs.",
                "",
                "== Getting started",
                "",
                "To get started quickly, download link:https://github.com/sfauvel/TryDocAsTest[Try doc as test] project.",
                "After " + linkToClass(InstallingLibrary.class, "installing Documentation testing") + " maven library, you are ready to write documentation that validate your code.",
                "",
                "== Main features",
                "",
                "* " + linkToClass(JUnitExtensionDocumentation.class, "JUnit extension embedded Approvals"),
                "** Name file associate to each test",
                "** Execute verification after test",
                "* Generation of a general documentation that aggregate all test files",
                "* Tools to extract parts of code",
                "* " + linkToClass(FormatterDocumentation.class, "API to transform text") + " to output format");
    }

    private String linkToClass(Class<? extends DocumentationBuilder> clazz) {
        final String name = clazz.getPackage().getName();
        return linkToClass(clazz, name);
    }

    private String linkToClass(Class<? extends DocumentationBuilder> clazz, String name) {
        buildersToGenerate.add(clazz);
        return String.format("link:%s.html[%s]\n",
                clazz.getName().replace(".", "/"),
                name);
    }

    public static void main(String... args) throws IOException {
        final DocumentationTestingDocumentation doc = new DocumentationTestingDocumentation();
        new Document(doc.build()).saveAs(Paths.get("index.adoc"));
        doc.buildLinkedFile();
    }

    private void buildLinkedFile() {
        for (Class<? extends DocumentationBuilder> aClass : this.buildersToGenerate) {
            try {
                Document.produce(aClass.getDeclaredConstructor().newInstance());
            } catch (IOException
                    | InstantiationException
                    | IllegalAccessException
                    | InvocationTargetException
                    | NoSuchMethodException e) {
                throw new RuntimeException("Not able to generate " + aClass.getSimpleName(), e);
            }
        }
    }

}
