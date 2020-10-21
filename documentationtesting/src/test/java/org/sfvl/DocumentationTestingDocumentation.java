package org.sfvl;

import org.sfvl.docformatter.FormatterDocumentation;
import org.sfvl.doctesting.DocTestingDocumentation;
import org.sfvl.doctesting.MainDocumentation;

import java.io.IOException;

public class DocumentationTestingDocumentation extends MainDocumentation {

    public DocumentationTestingDocumentation() {
        super("Documentation testing");
    }

    @Override
    protected String getMethodDocumentation(String packageToScan) {
        return linkToClass(DocTestingDocumentation.class) +
                linkToClass(FormatterDocumentation.class);
    }

    private String linkToClass(Class<?> clazz) {
        return String.format("link:%s.html[%s]\n\n",
                clazz.getName().replace(".", "/"),
                clazz.getSimpleName());
    }

    public static void main(String... args) throws IOException {
        new DocTestingDocumentation().generate();
        new FormatterDocumentation().generate();

        new DocumentationTestingDocumentation().generate(null, "index");
    }

}
