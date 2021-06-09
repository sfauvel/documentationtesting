package org.sfvl.doctesting;

import org.approvaltests.namer.ApprovalNamer;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class to define documentation file name from a test method.
 */
public class DocumentationNamer {

    private final Method testMethod;
    private final Path docRootPath;

    public DocumentationNamer(Path docRootPath, Method testMethod) {
        this.docRootPath = docRootPath;
        this.testMethod = testMethod;
    }

    public String getApprovalName() {
        return "_" + String.join(".",
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
