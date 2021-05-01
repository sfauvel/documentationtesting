package org.sfvl.doctesting.junitextension;

import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.utils.DocumentationNamer;
import org.sfvl.doctesting.writer.*;

import java.io.IOException;
import java.util.Arrays;

public class JUnitExtensionDocumentation implements DocumentProducer {

    private final Formatter formatter = new AsciidocFormatter();

    public String getOptions() {
        return formatter.paragraph(
                new Options(formatter).withCode().trim(),
                new Option("toclevels", "4").format()
        );
    }

    public String build() {
        return formatter.paragraphSuite(
                getOptions(),
                new Classes(formatter).includeClasses(
                        DocumentationNamer.toPath(getClass().getPackage()),
                                Arrays.asList(ApprovalsExtensionTest.class))
        );
    }

    public void produce() throws IOException {
        new Document(build()).saveAs(getClass());
    }

    public static void main(String... args) throws IOException {
        new JUnitExtensionDocumentation().produce();
    }

}
