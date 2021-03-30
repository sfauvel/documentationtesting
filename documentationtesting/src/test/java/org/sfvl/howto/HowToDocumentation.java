package org.sfvl.howto;

import org.sfvl.doctesting.ClassFinder;
import org.sfvl.doctesting.Document;
import org.sfvl.doctesting.DocumentationBuilder;

import java.io.IOException;
import java.util.List;

public class HowToDocumentation extends DocumentationBuilder {

    public HowToDocumentation() {
        super("How to");

        final Package aPackage = this.getClass().getPackage();
        withClassesToInclude(getClasses(aPackage));
        withLocation(aPackage);
        withOptionAdded("source-highlighter", "rouge");
    }

    protected List<Class<?>> getClasses(Package aPackage) {
        return new ClassFinder().testClasses(aPackage);
    }

    public static void main(String... args) throws IOException {
        Document.produce(new HowToDocumentation());
    }

}