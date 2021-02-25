package org.sfvl.doctesting;

import org.junit.jupiter.api.TestInfo;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

        final List<String> classesTree = new ArrayList<String>();

        final Class<?> declaringClass = testMethod.getDeclaringClass();
        classesTree.add(0, declaringClass.getSimpleName());

        Class<?> enclosingClass = declaringClass.getEnclosingClass();
        while (enclosingClass != null) {
            classesTree.add(0, enclosingClass.getSimpleName());
            enclosingClass = enclosingClass.getEnclosingClass();
        }

        classesTree.add(testMethod.getName());
        return classesTree.stream().collect(Collectors.joining("."));
    }

    public String getSourceFilePath() {
        String canonicalName = testMethod.getDeclaringClass().getPackage().getName();
        String pathName = canonicalName.toString().replace('.', '/');

        return docRootPath.resolve(pathName) + "/";
    }

    public Path getFilePath() {
        return Paths.get(getSourceFilePath(), getApprovalName()+".approved.adoc");
    }

    public Path getApprovedPath(Path docFilePath) {
        final Path fileName = getFilePath().getFileName();
        final Path resolve = docFilePath.getParent().relativize(Paths.get(getSourceFilePath())).resolve(fileName);
        return resolve;
    }
}
