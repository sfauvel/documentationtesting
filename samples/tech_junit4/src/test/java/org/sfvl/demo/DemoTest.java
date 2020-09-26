package org.sfvl.demo;

import org.junit.Test;
import org.sfvl.doctesting.ApprovalsJUnit4;

/**
 * Demo of a simple usage to generate documentation.
 */
public class DemoTest extends ApprovalsJUnit4 {

    /**
     * When adding two simple numbers, the java operator '+' should return the sum of them.
     */
    @Test
    public void adding_2_simple_numbers() {
        int a = 2;
        int b = 3;
        write(String.format("%d + %d = %d", a, b, a + b));
    }
}
