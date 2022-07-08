package org.sfvl.demo;

import com.github.javaparser.utils.SourceRoot;
import org.junit.Test;
import org.sfvl.codeextraction.CodeExtractor;
import org.sfvl.doctesting.ApprovalsJUnit4;

import static org.sfvl.doctesting.utils.Config.TEST_PATH;

/**
 * Demo of a simple usage to generate documentation.
 */
public class DemoTest extends ApprovalsJUnit4 {

    /**
     * When adding two simple numbers, the java operator '+' should return the sum of them.
     */
    @Test
    public void adding_2_simple_numbers() {
        final SourceRoot parserCode = new SourceRoot(TEST_PATH);
        int a = 2;
        int b = 3;
        write(String.format("%d + %d = %d", a, b, a + b));
        write(CodeExtractor.extractPartOfCurrentMethod("A"));
    }


    /**
     * When multiplying two simple numbers, the java operator '*' should return the multiplication of them.
     */
    @Test
    public void multiplying_2_simple_numbers() {
        final SourceRoot parserCode = new SourceRoot(TEST_PATH);
        int a = 2;
        int b = 3;
        write(String.format("%d * %d = %d", a, b, a * b));
        write(CodeExtractor.extractPartOfCurrentMethod("A"));
    }
}
