package org.sfvl.demo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sfvl.Person;
import org.sfvl.doctesting.DocumentationNamer;
import org.sfvl.doctesting.MainDocumentation;
import org.sfvl.doctesting.PathProvider;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Using JUnit assertions to test.")
public class PerfJUnitTest {
    private static LocalDateTime begin;

    @BeforeAll
    public static void begin() {
        begin = LocalDateTime.now();
        System.out.println(System.getProperty("no-assert")==null?"assert":"no-assert");
    }

    @AfterAll
    public static void end() throws IOException, NoSuchMethodException {
        LocalDateTime end = LocalDateTime.now();

        PathProvider pathProvider = new PathProvider();
        final Path docPath = new MainDocumentation().getDocRootPath();
        final DocumentationNamer documentationNamer = new DocumentationNamer(docPath, PerfJUnitTest.class.getMethod("should_give_person_information"));

        final Class<?> aClass = MethodHandles.lookup().lookupClass();
        String packageName = aClass.getPackage().getName();
        String packagePathName = packageName.replace('.', '/');

        try (FileWriter fileWriter = new FileWriter(documentationNamer.getFilePath().toFile())) {
            final Path filePath = documentationNamer.getFilePath().getParent();
            final Path testPath = filePath.relativize(docPath.getParent());
            final Path classPath = testPath.resolve("java")
                    .resolve(aClass.getPackage().getName().replace('.', '/'))
                    .resolve(aClass.getSimpleName()+".java");

            fileWriter.write("\n[source,java,indent=0]\n----\ninclude::"+classPath+"[tags=code]\n----\n");
        }

        BasicDocumentation.writeDuration(MethodHandles.lookup().lookupClass(), begin, end);
    }

    // tag::code[]
    @Test
    public void should_give_person_information() {
        final LocalDate now = LocalDate.now();
        final int current_year = now.getYear();
        final int age = 45;
        final Person person = new Person("John", "Doe", LocalDate.of(current_year - age, 11, 23));

        assertEquals(age, person.age());
        assertEquals("John Doe", person.toString());
    }
    // end::code[]
}
