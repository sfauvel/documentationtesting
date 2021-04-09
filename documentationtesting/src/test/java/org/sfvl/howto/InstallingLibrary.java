package org.sfvl.howto;

import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.writer.Document;
import org.sfvl.doctesting.writer.DocumentProducer;
import org.sfvl.doctesting.writer.DocumentationBuilder;
import org.sfvl.doctesting.writer.Options;

import java.io.IOException;

public class InstallingLibrary implements DocumentProducer {

    Formatter formatter = new AsciidocFormatter();

    public String build() {
        return formatter.paragraphSuite(
                new Options(formatter).withCode(),
                formatter.title(1, "Installing Documentation testing"),
                getContent()
        );
    }

    protected String getContent() {
        return "To be written";
    }

    @Override
    public void produce() throws IOException {
        new Document(build()).saveAs(this.getClass());
    }

    public static void main(String... args) throws IOException {
        new InstallingLibrary().produce();
    }
}
