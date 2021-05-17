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

@DisplayName("Using JUnit assertions to test. There is only one test but it check 100 times the same case.")
@TestCategory(category = TestCategory.Cat.Long)
public class UsingJUnitWithALongTestTest {

    @AfterAll
    public static void end() throws IOException, NoSuchMethodException {
        final Class<?> clazzWithCode = UsingJUnitWithALongTestTest.class;
        final DocPath docPath = new DocPath(clazzWithCode.getMethod("should_give_person_information"));
        final Path approvedPath = docPath.approved().path();

        try (FileWriter fileWriter = new FileWriter(approvedPath.toFile())) {
            final Path pathToInclude = new DocPath(clazzWithCode).test().from(docPath.approved());
            fileWriter.write("\n[source,java,indent=0]\n----\ninclude::" + pathToInclude + "[tags=code]\n----\n");
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
