package org.sfvl.docformatter;

import org.sfvl.doctesting.utils.DocumentationNamer;
import org.sfvl.doctesting.writer.Classes;
import org.sfvl.doctesting.writer.Document;
import org.sfvl.doctesting.writer.DocumentProducer;
import org.sfvl.doctesting.writer.Options;

import java.io.IOException;
import java.util.Arrays;

public class FormatterDocumentation implements DocumentProducer {

    private final Formatter formatter = new AsciidocFormatter();

    public String build() {
        return formatter.paragraphSuite(
                new Options(formatter).withCode(),
                formatter.title(1, "Documentation"),
                includeClasses()
        );
    }

    public String includeClasses() {
        return new Classes(formatter).includeClasses(
                DocumentationNamer.toPath(getClass().getPackage()),
                Arrays.asList(AsciidocFormatterTest.class));
    }

    @Override
    public void produce() throws IOException {
        new Document(this.build()).saveAs(this.getClass());
    }

    public static void main(String... args) throws IOException {
        new FormatterDocumentation().produce();
    }

}
