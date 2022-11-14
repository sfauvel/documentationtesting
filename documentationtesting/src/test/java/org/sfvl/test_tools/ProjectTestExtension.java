package org.sfvl.test_tools;

import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.doctesting.utils.DocPath;

import java.io.File;

public class ProjectTestExtension extends FastApprovalsExtension {

    public void runTestAndWriteResultAsComment(Class<?> testClass) {
        final TestRunnerFromTest.Results results = new TestRunnerFromTest().runTestClass(testClass);
        String[] texts = new String[]{"", String.format("// Test result for %s: %s", testClass.getSimpleName(), results.sucess() ? "Success" : "Fails"), ""};
        write(texts);
    }

    public void removeNonApprovalFiles(DocPath docPath) {
        final File[] filesToDelete = docPath.page().path().getParent().toFile().listFiles(
                pathname -> pathname.isFile() && !pathname.getName().startsWith("_"));
        if (filesToDelete == null) return;

        for(File file: filesToDelete) {
            file.delete();
        }
    }
}
