package org.sfvl.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;

/**
 * Demo of a simple usage to generate documentation.
 */
@ExtendWith(ApprovalsExtension.class)
public class DemoTest {

    public SpecificWriter writer = new SpecificWriter();

    /**
     * When adding two simple numbers, the java operator '+' should return the sum of them.
     */
    @Test
    @DisplayName("Adding 2 simple numbers")
    public void should_be_5_when_adding_2_and_3() {
        int a = 2;
        int b = 3;
        writer.write(String.format("%d + %d = %d", a, b, a+b));
    }

    /**
     * When adding three simple numbers, the java operator '+' should return the sum of them.
     */
    @Test
    @DisplayName("Adding 3 simple numbers")
    public void should_be_9_when_adding_2_3_and_4() {
        int a = 2;
        int b = 3;
        int c = 4;
        writer.writeBold(String.format("%d + %d + %d = %d", a, b, c, a+b+c));
    }


}
