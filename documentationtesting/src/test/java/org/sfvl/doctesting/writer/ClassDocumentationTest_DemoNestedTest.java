package org.sfvl.doctesting.writer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.NotIncludeToDoc;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;

/**
 * Demo of a simple usage to generate documentation.
 */
@NotIncludeToDoc
public class ClassDocumentationTest_DemoNestedTest {

    @RegisterExtension
    static final ApprovalsExtension doc = new SimpleApprovalsExtension();

    /**
     * Document of Addition operations.
     */
    @Nested
    class Adding {

        @Test
        @DisplayName("Adding 2 simple numbers")
        public void should_be_5_when_adding_2_and_3() {
            doc.write(String.format("%d + %d = %d", 2, 3, 2 + 3));
        }

        /**
         * A nested test.
         */
        @Test
        @DisplayName("Adding 3 simple numbers")
        public void should_be_9_when_adding_2_3_and_4() {
            doc.write(String.format("%d + %d + %d = %d", 2, 3, 4, 2 + 3 + 4));
        }

        /**
         * Addition negative numbers.
         */
        @Nested
        class AddingNegativeNumber {
            @Test
            @DisplayName("Adding 2 negative numbers")
            public void should_be_minus_8_when_adding_minus_3_and_minus_5() {
                doc.write(String.format("%d + %d = %d", -3, -5, (-3) + (-5)));
            }
        }
    }

    /**
     * A method between two nested classes is between those classes in final documentatiton.
     */
    @Test
    public void method_between_two_nested_classes() {
        doc.write("This is the documentation generated in test");
    }

    @Nested
    class Multiply {
        @Test
        @DisplayName("Multiply 2 simple numbers")
        public void should_be_12_when_multiply_4_and_3() {
            doc.write(String.format("%d * %d = %d", 4, 3, 4 * 3));
        }
    }
}
