package org.sfvl.demo;

import org.approvaltests.Approvals;
import org.approvaltests.core.ApprovalWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;

// tag::test[]
/**
 * Demo of a simple usage to generate documentation and validate it with https://sfauvel.github.io/documentationtesting/documentationtesting/[Documentation testing].
 */
public class DemoWithDocumentationTestingTest {

    @RegisterExtension
    static ApprovalsExtension doc = new SimpleApprovalsExtension();

    /**
     * When adding two simple numbers, the java operator '+' should return the sum of them.
     */
    @Test
    public void should_add_2_numbers() {
        int a = 2;
        int b = 3;
        int result = a + b;

        final String output = String.format("%d + %d = *%d*", a, b, result);

        doc.write(output);
    }
}
// end::test[]
