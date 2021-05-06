package org.sfvl.doctesting.junitinheritance;

import com.thoughtworks.qdox.model.JavaClass;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.platform.commons.support.ModifierSupport;
import org.sfvl.doctesting.utils.*;
import org.sfvl.doctesting.writer.ClassDocumentation;

import java.io.FileWriter;
import java.nio.file.Path;

public abstract class DocAsTestBase {
    protected static final PathProvider pathBuidler = new PathProvider();

    DocWriter writer = new DocWriter();

    /**
     * Return the name to use as title from a test method .
     * It returns the value specified with _DisplayName_ annotation.
     * If annotation is not present, this is the method name that will be returned
     * after some test formatting (remove '_', uppercase first letter).
     *
     * It's based on value return by _displayName_ method.
     * It returns either DisplayName annotation value or method name.
     *
     * @param testInfo
     * @return
     */
    protected String formatTitle(TestInfo testInfo) {
        return writer.formatTitle(testInfo.getDisplayName(), testInfo.getTestMethod().get());
    }

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
        final Path docFilePath = Config.DOC_PATH.resolve(DocumentationNamer.toPath(clazz, "", ".approved.adoc"));
        try (FileWriter fileWriter = new FileWriter(docFilePath.toFile())) {
            fileWriter.write(content);
        }
    }

    @AfterEach
    public void approvedAfterTest(TestInfo testInfo) throws Exception {

        final Path docRootPath = getDocPath();
        String content = buildContent(testInfo);

        approvalAfterTestSpecific(content, new DocumentationNamer(docRootPath, testInfo));
    }

    abstract protected void approvalAfterTestSpecific(final String content, final DocumentationNamer documentationNamer) throws Exception;

    /**
     * Write a text to the output.
     *
     * @param texts
     */
    public void write(String... texts) {
        writer.write(texts);
    }

    protected String buildContent(TestInfo testInfo) {
        final JavaClass testInfoJavaClass = CodeExtractor.getBuilder().getClassByName(TestInfo.class.getCanonicalName());
        return writer.formatOutput(testInfo.getDisplayName(), testInfo.getTestMethod().get());
    }
}
