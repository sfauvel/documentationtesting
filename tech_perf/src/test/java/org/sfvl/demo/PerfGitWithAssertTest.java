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
@DisplayName("Check file modification with git after each test.")
public class PerfGitWithAssertTest extends GitBase {

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
