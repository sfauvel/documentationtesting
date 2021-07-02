package org.sfvl.demo;

import org.approvaltests.Approvals;
import org.junit.jupiter.api.Test;

/**
 * Demo of a simple usage to generate documentation and validate it with Approvals.
 */
public class DemoWithApprovalsTest {

    // tag::test[]
    /**
     * When adding two simple numbers, the java operator '+' should return the sum of them.
     */
    @Test
    public void should_be_5_when_adding_2_and_3() {
        int a = 2;
        int b = 3;
        int result = a + b;

        final String output = String.join("\n",
                "= Should add 2 numbers",
                "",
                String.format("%d + %d = *%d*", a, b, a + b));

        Approvals.verify(output);
    }
    // end::test[]
}
