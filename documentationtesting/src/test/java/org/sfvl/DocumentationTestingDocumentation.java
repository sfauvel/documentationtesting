package org.sfvl;

import org.sfvl.docformatter.FormatterDocumentation;
import org.sfvl.doctesting.DocTestingDocumentation;
import org.sfvl.doctesting.MainDocumentation;
import org.sfvl.howto.HowToDocumentation;

import java.io.IOException;
import java.nio.file.Path;

public class DocumentationTestingDocumentation extends MainDocumentation {

    public DocumentationTestingDocumentation() {
        super("Documentation testing");
    }

    @Override
    protected String getMethodDocumentation(String packageToScan, Path docFilePath) {
        return "This project is composed of two main packages.\n\n" +
                "* " + linkToClass(DocTestingDocumentation.class) + ": Tools to make test validating generated files.\n" +
                "* " + linkToClass(FormatterDocumentation.class) + ": Utilities to format documentation." +
                "\n\n" +
                "Section " + linkToClass(HowToDocumentation.class) + " shows how to do some common needs.";
    }

    private String linkToClass(Class<?> clazz) {
        return String.format("link:%s.html[%s]\n",
                clazz.getName().replace(".", "/"),
                clazz.getPackage().getName());
    }

    public static void main(String... args) throws IOException {
        new DocTestingDocumentation().generate();
        new FormatterDocumentation().generate();
        new HowToDocumentation().generate();

        new DocumentationTestingDocumentation().generate(null, "index");
    }

}
