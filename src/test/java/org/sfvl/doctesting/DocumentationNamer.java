package org.sfvl.doctesting;

import org.approvaltests.namer.ApprovalNamer;
import org.junit.jupiter.api.TestInfo;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class to define documentation file name from a test method.
 */
public class DocumentationNamer implements ApprovalNamer {

    public static final Path DOC_ROOT_PATH = Paths.get("src", "test", "docs");
    private final Method testMethod;

    public DocumentationNamer(TestInfo testInfo) {
        this(testInfo.getTestMethod().get());
    }

    public DocumentationNamer(Method testMethod) {
        this.testMethod = testMethod;
    }

    @Override
    public String getApprovalName() {
        return String.join(".",
                testMethod.getDeclaringClass().getSimpleName(),
                testMethod.getName());
    }

    @Override
    public String getSourceFilePath() {
        String canonicalName = testMethod.getDeclaringClass().getPackage().getName();
        String pathName = canonicalName.toString().replace('.', '/');

        return DOC_ROOT_PATH.resolve(pathName) + "/";
    }
}
