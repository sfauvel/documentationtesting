package org.sfvl.doctesting.junitinheritance;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInfo;
import org.junit.platform.commons.support.ModifierSupport;
import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.doctesting.utils.DocWriter;
import org.sfvl.doctesting.utils.PathProvider;
import org.sfvl.doctesting.writer.ClassDocumentation;

import java.io.FileWriter;
import java.lang.reflect.Method;
import java.nio.file.Path;

public abstract class DocAsTestBase {
    protected static final PathProvider pathBuidler = new PathProvider();

    DocWriter writer = new DocWriter();

    /**
     * Give path where docs are generated.
     *
     * @return
     */
    protected Path getDocPath() {
        return ApprovalsBase.pathBuidler.getProjectPath().resolve(Config.DOC_PATH);
    }

    private static boolean isNestedClass(Class<?> currentClass) {
        return !ModifierSupport.isStatic(currentClass) && currentClass.isMemberClass();
    }

    @AfterAll
    public static void afterAll(TestInfo testInfo) throws Exception {
        final Class<?> clazz = testInfo.getTestClass().get();
        if (isNestedClass(clazz)) {
            return;
        }
        final ClassDocumentation classDocumentation = new ClassDocumentation();
        final String content = classDocumentation.getClassDocumentation(clazz);

        final Path docFilePath = new DocPath(clazz).approved().path();
        try (FileWriter fileWriter = new FileWriter(docFilePath.toFile())) {
            fileWriter.write(content);
        }
    }

    @AfterEach
    public void approvedAfterTest(TestInfo testInfo) throws Exception {

        final Path docRootPath = getDocPath();
        String content = buildContent(testInfo);

        approvalAfterTestSpecific(content, new DocPath(testInfo.getTestMethod().get()));
    }

    protected abstract void approvalAfterTestSpecific(final String content, final DocPath docPath) throws Exception;

    /**
     * Write a text to the output.
     *
     * @param texts
     */
    public void write(String... texts) {
        writer.write(texts);
    }

    protected String buildContent(TestInfo testInfo) {
        final Method testMethod = testInfo.getTestMethod().get();
        final DisplayName displayName = testMethod.getAnnotation(DisplayName.class);

        return displayName != null
                ? writer.formatOutput(displayName.value(), testMethod)
                : writer.formatOutput(testMethod);
    }
}
