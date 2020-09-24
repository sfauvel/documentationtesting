package org.sfvl.demo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sfvl.Person;
import org.sfvl.doctesting.DocumentationNamer;
import org.sfvl.doctesting.MainDocumentation;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Using JUnit assertions to test.")
@TestCategory(category = TestCategory.Cat.Simple)
public class UsingJUnitTest {

    @AfterAll
    public static void end() throws IOException, NoSuchMethodException {

        final Path docPath = new MainDocumentation().getDocRootPath();
        final DocumentationNamer documentationNamer = new DocumentationNamer(docPath, UsingJUnitTest.class.getMethod("checkPerson"));

        final Class<?> aClass = MethodHandles.lookup().lookupClass();

        try (FileWriter fileWriter = new FileWriter(documentationNamer.getFilePath().toFile())) {
            final Path filePath = documentationNamer.getFilePath().getParent();
            final Path testPath = filePath.relativize(docPath.getParent());
            final Path classPath = testPath.resolve("java")
                    .resolve(aClass.getPackage().getName().replace('.', '/'))
                    .resolve(aClass.getSimpleName() + ".java");

            fileWriter.write("\n[source,java,indent=0]\n----\ninclude::" + classPath + "[tags=code]\n----\n");
        }

    }

    // tag::code[]
    @Test
    public void should_give_person_information() {
        checkPerson();
    }

    public static void checkPerson() {
        final LocalDate now = LocalDate.now();
        final int current_year = now.getYear();
        final int age = 45;
        final Person person = new Person("John", "Doe", LocalDate.of(current_year - age, 11, 23));

        assertEquals(age, person.age());
        assertEquals("John Doe", person.toString());
    }
    // end::code[]
}
