package org.sfvl.demo;

import org.junit.jupiter.api.Test;
import org.sfvl.codeextraction.ClassFinder;
import org.sfvl.doctesting.demo.DemoDocumentation;
import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.writer.Classes;
import org.sfvl.doctesting.writer.Document;
import org.sfvl.doctesting.writer.Options;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class TennisDocumentation extends DemoDocumentation {

    public TennisDocumentation() {
        super("Tennis");
    }

    @Override
    public void produce() throws IOException {
        new Document(this.build()).saveAs(Config.DOC_PATH.resolve("index.adoc"));
    }

    public String build() {
        final List<Class<?>> classesToInclude = new ClassFinder().classesWithAnnotatedMethod(
                this.getClass().getPackage(),
                Test.class,
                m -> !Arrays.asList(TennisStandardTest.class).contains(m.getDeclaringClass())
        );

        return formatter.paragraphSuite(
                new Options(formatter).withCode(),
                getHeader(),
                getContent(),
                new Classes(formatter).includeClasses(
                        docRootPath,
                        classesToInclude
                )
        );
    }

    public static void main(String... args) throws IOException {
        new TennisDocumentation().produce();
    }
}
