package org.sfvl.test_tools;

import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.utils.DocPath;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProjectTestExtension extends SimpleApprovalsExtension {

    public void runTestAndWriteResultAsComment(Class<?> testClass) {
        final TestRunnerFromTest.Results results = new TestRunnerFromTest().runTestClass(testClass);
        String[] texts = new String[]{"", String.format("// Test result for %s: %s", testClass.getSimpleName(), results.sucess() ? "Success" : "Fails"), ""};
        write(texts);
    }

    public void removeNonApprovalFiles(DocPath docPath) {
        for(File file: docPath.page().path().getParent().toFile().listFiles(pathname ->
                pathname.isFile() && !pathname.getName().startsWith("_"))) {
            file.delete();
        }
    }
}
