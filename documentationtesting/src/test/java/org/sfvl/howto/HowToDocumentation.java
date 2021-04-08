package org.sfvl.howto;

import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.utils.ClassFinder;
import org.sfvl.doctesting.utils.DocumentationNamer;
import org.sfvl.doctesting.writer.*;

import java.io.IOException;
import java.util.List;

public class HowToDocumentation implements DocumentProducer {

    protected final Formatter formatter = new AsciidocFormatter();

    protected List<Class<?>> getClasses(Package aPackage) {
        return new ClassFinder().testClasses(aPackage);
    }

    public String build() {
        final Package aPackage = this.getClass().getPackage();
        return formatter.paragraphSuite(
                new Options(formatter).withCode(),
                formatter.title(1, "How to"),
                new Classes(formatter).includeClasses(DocumentationNamer.toPath(aPackage), getClasses(aPackage))
        );
    }

    @Override
    public void produce() throws IOException {
        new Document(this.build()).saveAs(this.getClass());
    }

    public static void main(String... args) throws IOException {
        new HowToDocumentation().produce();
    }

}