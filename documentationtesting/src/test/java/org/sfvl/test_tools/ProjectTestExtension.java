package org.sfvl.test_tools;

import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;

public class ProjectTestExtension extends SimpleApprovalsExtension {

    public void runTestAndWriteResultAsComment(Class<?> testClass) {
        final TestRunnerFromTest.Results results = new TestRunnerFromTest().runTestClass(testClass);
        String[] texts = new String[]{"", String.format("// Test result for %s: %s", testClass.getSimpleName(), results.sucess() ? "Success" : "Fails"), ""};
        write(texts);
    }
}
