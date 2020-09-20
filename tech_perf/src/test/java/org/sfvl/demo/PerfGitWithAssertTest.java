package org.sfvl.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sfvl.doctesting.GitBase;

/**
 * Demo of a simple usage to generate documentation.
 */
@DisplayName("Check file modification with git after each test.")
public class PerfGitWithAssertTest extends GitBase {

    @Test
    public void should_give_person_information() {
        BasicDocumentation.generateTestDocumentation(this);
    }
}
