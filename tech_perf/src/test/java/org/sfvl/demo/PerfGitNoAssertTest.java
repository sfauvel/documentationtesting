package org.sfvl.demo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sfvl.Person;
import org.sfvl.doctesting.GitBase;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Demo of a simple usage to generate documentation.
 */
@DisplayName("Use Git to check file modification but after all tests. The time shown not include this verification.")
public class PerfGitNoAssertTest extends GitBase {

    private static LocalDateTime begin;

    @BeforeAll
    public static void begin() {
        System.setProperty("no-assert", "");
        begin = LocalDateTime.now();
        System.out.println(System.getProperty("no-assert")==null?"assert":"no-assert");
    }

    @AfterAll
    public static void end() throws IOException {
        LocalDateTime end = LocalDateTime.now();

        BasicDocumentation.writeDuration(MethodHandles.lookup().lookupClass(), begin, end);
        System.clearProperty("no-assert");
    }
    @Test
    public void should_give_person_information() {
        BasicDocumentation.generateTestDocumentation(this);
    }
}
