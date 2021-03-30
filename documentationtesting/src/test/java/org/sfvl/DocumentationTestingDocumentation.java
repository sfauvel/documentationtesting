package org.sfvl;

import org.sfvl.docformatter.FormatterDocumentation;
import org.sfvl.doctesting.DocTestingDocumentation;
import org.sfvl.doctesting.DocumentationBuilder;
import org.sfvl.doctesting.MainDocumentation;
import org.sfvl.doctesting.junitextension.JUnitExtensionDocumentation;
import org.sfvl.doctesting.Document;
import org.sfvl.howto.HowToDocumentation;
import org.sfvl.howto.InstallingLibrary;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

public class DocumentationTestingDocumentation extends MainDocumentation {

    public DocumentationTestingDocumentation() {
        super("Documentation testing");
    }

    @Override
    protected String getMethodDocumentation(String packageToScan, Path docFilePath) {

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
                "After "+ linkToClass(InstallingLibrary.class, "installing Documentation testing") + " maven library, you are ready to write documentation that validate your code.",
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

    private String linkToClass(Class<?> clazz) {
        final String name = clazz.getPackage().getName();
        return linkToClass(clazz, name);
    }

    private String linkToClass(Class<?> clazz, String name) {
        return String.format("link:%s.html[%s]\n",
                clazz.getName().replace(".", "/"),
                name);
    }

    public static void main(String... args) throws IOException {
        new DocTestingDocumentation().generate();
        new FormatterDocumentation().generate();

        for (DocumentationBuilder builder : Arrays.asList(
                new HowToDocumentation(),
                new InstallingLibrary()
        )) {
            Document.produce(builder);
        }

        new DocumentationTestingDocumentation().generate(null, "index");
    }

}
