package org.sfvl.demo;

import org.junit.jupiter.api.DisplayName;

/**
 * Demo of a simple usage to generate documentation.
 */
@DisplayName("Use Git to check file modification but after all tests. The time shown not include this verification. " +
        "100O tests are made." +
        "This test is run twice to have at least one execution with JavaProjectBuilder already initialized.")
@TestCategory(category = TestCategory.Cat.Lot)
public class UsingGitWithNoAssertOptionAndLotOfTests_B_Test extends UsingGitWithNoAssertOptionAndLotOfTestsTest {
}
