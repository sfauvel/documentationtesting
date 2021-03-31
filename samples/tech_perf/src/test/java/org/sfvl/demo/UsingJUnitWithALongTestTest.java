package org.sfvl.demo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sfvl.Person;
import org.sfvl.doctesting.DocumentationNamer;
import org.sfvl.doctesting.PathProvider;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Using JUnit assertions to test. There is only one test but it check 100 times the same case.")
@TestCategory(category = TestCategory.Cat.Long)
public class UsingJUnitWithALongTestTest {

    @AfterAll
    public static void end() throws IOException, NoSuchMethodException {
        final Path docPath = new PathProvider().getProjectPath().resolve(Paths.get("src", "test", "docs"));
        final DocumentationNamer documentationNamer = new DocumentationNamer(docPath, UsingJUnitWithALongTestTest.class.getMethod("should_give_person_information"));

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
        for (int i = 0; i < 1000; i++) {
            checkPerson();
        }
    }

    public void checkPerson() {
        final LocalDate now = LocalDate.now();
        final int current_year = now.getYear();
        final int age = 45;
        final Person person = new Person("John", "Doe", LocalDate.of(current_year - age, 11, 23));

        assertEquals(age, person.age());
        assertEquals("John Doe", person.toString());
    }
    // end::code[]
}
