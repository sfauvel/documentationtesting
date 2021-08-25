package org.sfvl.doctesting.junitextension;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.NotIncludeToDoc;
import org.sfvl.test_tools.OnlyRunProgrammatically;

@NotIncludeToDoc
@OnlyRunProgrammatically
// tag::DemoNestedTest[]
/**
 * Demo of a simple usage to generate documentation.
 */
public class DemoNestedTest {
    @RegisterExtension
    static final ApprovalsExtension writer = new SimpleApprovalsExtension();

    /**
     * Document of Addition operations.
     */
    @Nested
    class Adding {

        @Test
        @DisplayName("Adding 2 simple numbers")
        public void should_be_5_when_adding_2_and_3() {
            writer.write(String.format("%d + %d = %d", 2, 3, 2 + 3));
        }

        /**
         * A nested test.
         */
        @Test
        @DisplayName("Adding 3 simple numbers")
        public void should_be_9_when_adding_2_3_and_4() {
            writer.write(String.format("%d + %d + %d = %d", 2, 3, 4, 2 + 3 + 4));
        }
    }

    @Nested
    class Multiply {
        @Test
        @DisplayName("Multiply 2 simple numbers")
        public void should_be_12_when_multiply_4_and_3() {
            writer.write(String.format("%d * %d = %d", 4, 3, 4 * 3));
        }
    }
}
// end::DemoNestedTest[]