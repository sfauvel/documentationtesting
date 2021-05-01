package org.sfvl.doctesting.junitextension;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.utils.CodeExtractor;
import org.sfvl.doctesting.utils.DocWriter;

import java.lang.reflect.Method;

/**
 * FindLambdaMethod class provides tools to find reference method name.
 */
@DisplayName(value = "Extract information from method reference")
public class FindLambdaMethodTest {
    private static final DocWriter doc = new DocWriter();
    @RegisterExtension
    static ApprovalsExtension extension = new ApprovalsExtension(doc);

    public void myMethod() {

    }

    public void methodWithOneParameter(String parameter) {

    }

    /**
     * It's possible to extract method name from a method reference.
     *
     * @param testInfo
     */
    @Test
    @DisplayName("Find method name")
    public void find_method_name(TestInfo testInfo) {
        {
            // >>>1
            String methodName = FindLambdaMethod.getName(FindLambdaMethodTest::myMethod);
            // <<<1

            doc.write(extractMarkedCode(testInfo, "1"), "");

            doc.write(String.format("Method name: *%s*", methodName), "");
        }
        {
            // >>>2
            String methodName = FindLambdaMethod.getName(FindLambdaMethodTest::methodWithOneParameter);
            // <<<2

            doc.write(extractMarkedCode(testInfo, "2"), "");

            doc.write(String.format("Method name: *%s*", methodName));
        }
    }

    /**
     * It's possible to extract method from a method reference.
     *
     * @param testInfo
     */
    @Test
    @DisplayName("Find method")
    public void find_method(TestInfo testInfo) {
        {
            // >>>1
            Method method = FindLambdaMethod.getMethod(FindLambdaMethodTest::myMethod);
            String methodName = method.getName();
            // <<<1

            doc.write(extractMarkedCode(testInfo, "1"), "");

            doc.write(String.format("Method name: *%s*", methodName), "");
        }
        {
            // >>>2
            Method method = FindLambdaMethod.getMethod(FindLambdaMethodTest::methodWithOneParameter);
            String methodName = method.getName();
            // <<<2

            doc.write(extractMarkedCode(testInfo, "2"), "");

            doc.write(String.format("Method name: *%s*", methodName));
        }
    }

    private String extractMarkedCode(TestInfo testInfo) {
        return formatSourceCode(CodeExtractor.extractPartOfMethod(testInfo.getTestMethod().get()));
    }

    private String extractMarkedCode(TestInfo testInfo, String suffix) {
        return formatSourceCode(CodeExtractor.extractPartOfMethod(testInfo.getTestMethod().get(), suffix));
    }

    private <T> String formatSourceCode(String source) {
        return String.join("\n",
                "[source, java, indent=0]",
                "----",
                source,
                "----");
    }
}