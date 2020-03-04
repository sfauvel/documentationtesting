package org.sfvl.doctesting;

import org.junit.jupiter.api.TestInfo;

import java.lang.reflect.Method;
import java.nio.file.Path;

/**
 * Class to define documentation file name from a test method.
 */
public class DocumentationNamer {

    private final Method testMethod;
    private final Path docRootPath;

    public DocumentationNamer(Path docRootPath, TestInfo testInfo) {
        this(docRootPath, testInfo.getTestMethod().get());
    }

    public DocumentationNamer(Path docRootPath, Method testMethod) {
        this.docRootPath = docRootPath;
        this.testMethod = testMethod;
    }

    public String getApprovalName() {
        return String.join(".",
                testMethod.getDeclaringClass().getSimpleName(),
                testMethod.getName());
    }

    public String getSourceFilePath() {
        String canonicalName = testMethod.getDeclaringClass().getPackage().getName();
        String pathName = canonicalName.toString().replace('.', '/');

        return docRootPath.resolve(pathName) + "/";
    }

    public Path getFilePath() {
        return Paths.get(getSourceFilePath(), getApprovalName()+".approved.adoc");
    }
}
