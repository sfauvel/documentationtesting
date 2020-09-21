package org.sfvl.demo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sfvl.doctesting.GitBase;

import java.io.IOException;

/**
 * Demo of a simple usage to generate documentation.
 */
@DisplayName("Use Git to check file modification but after all tests. The time shown not include this verification. There is only one test but it check 100 times the same case.")
@TestCategory(category = TestCategory.Cat.Long)
public class UsingGitWithNoAssertOptionAndALongTestTest extends GitBase {

    @BeforeAll
    public static void begin() {
        System.setProperty("no-assert", "");
    }

    @AfterAll
    public static void end() throws IOException {
        System.clearProperty("no-assert");
    }

    @Test
    public void should_give_person_information() {
        // Check a lot but generate only one file.
        for (int i = 0; i < 1000; i++) {
            BasicDocumentation.generateTestDocumentation(this);
        }
    }
}
