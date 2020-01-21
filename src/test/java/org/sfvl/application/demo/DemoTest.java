package org.sfvl.application.demo;

import org.junit.jupiter.api.Test;
import org.sfvl.doctesting.ApprovalsBase;

public class DemoTest  extends ApprovalsBase {

    /**
     * When adding two simple number, the java operator + should return the sum of them.
     */
    @Test
    public void should_be_5_when_adding_2_and_3() {
        int a = 2;
        int b = 3;
        write(String.format("%d + %d = %d", a, b, a+b));
    }
}
