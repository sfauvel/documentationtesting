package org.sfvl.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sfvl.doctesting.junitinheritance.SelectableBase;

/**
 * Demo of a simple usage to generate documentation.
 */
public class DemoTest extends SelectableBase {

    /**
     * When adding two simple numbers, the java operator '+' should return the sum of them.
     */
    @Test
    @DisplayName("Adding 2 simple numbers")
    public void should_be_5_when_adding_2_and_3() {
        int a = 2;
        int b = 3;
        write(String.format("%d + %d = %d", a, b, a + b));
    }
}
