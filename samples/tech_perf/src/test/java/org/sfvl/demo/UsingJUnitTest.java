package org.sfvl.demo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sfvl.Person;
import org.sfvl.doctesting.utils.DocPath;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Using JUnit assertions to test.")
@TestCategory(category = TestCategory.Cat.Simple)
public class UsingJUnitTest {

    @AfterAll
    public static void end() throws IOException, NoSuchMethodException {
        final Class<?> clazzWithCode = UsingJUnitTest.class;
        final DocPath docPath = new DocPath(clazzWithCode.getMethod("checkPerson"));
        final Path approvedPath = docPath.approved().path();

        try (FileWriter fileWriter = new FileWriter(approvedPath.toFile())) {
            final Path pathToInclude = new DocPath(clazzWithCode).test().from(docPath.approved());
            fileWriter.write("\n[source,java,indent=0]\n----\ninclude::" + pathToInclude + "[tags=code]\n----\n");
        }

    }

    // tag::code[]
    @Test
    public void should_give_person_information() {
        checkPerson();
    }

    public static void checkPerson() {
        final LocalDate now = LocalDate.now(Person.CLOCK);
        final int current_year = now.getYear();
        final int age = 45;
        final Person person = new Person("John", "Doe", LocalDate.of(current_year - age, 11, 23));

        assertEquals(age, person.age());
        assertEquals("John Doe", person.toString());
    }
    // end::code[]
}
