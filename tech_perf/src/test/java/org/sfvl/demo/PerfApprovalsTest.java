package org.sfvl.demo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sfvl.Person;
import org.sfvl.doctesting.ApprovalsBase;
import org.sfvl.doctesting.MainDocumentation;
import org.sfvl.doctesting.PathProvider;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Demo of a simple usage to generate documentation.
 */
@DisplayName("Verify documentation with _Approvals_ library.")
public class PerfApprovalsTest extends ApprovalsBase {
    private static LocalDateTime begin;

    @BeforeAll
    public static void begin() {
        begin = LocalDateTime.now();
        System.out.println(System.getProperty("no-assert")==null?"assert":"no-assert");
    }

    @AfterAll
    public static void end() throws IOException {
        LocalDateTime end = LocalDateTime.now();

        BasicDocumentation.writeDuration(MethodHandles.lookup().lookupClass(), begin, end);
    }

    @Test
    public void should_give_person_information() {
        BasicDocumentation.generateTestDocumentation(this);
    }

}
