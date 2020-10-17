package org.sfvl.doctesting.junitinheritance;

import com.thoughtworks.qdox.model.JavaClass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.TestInfo;
import org.sfvl.doctesting.CodeExtractor;
import org.sfvl.doctesting.DocumentationNamer;
import org.sfvl.doctesting.PathProvider;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class DocAsTestBase {
    protected static final PathProvider pathBuidler = new PathProvider();
    protected StringBuffer sb = new StringBuffer();

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
        String displayName = testInfo.getDisplayName();
        String methodName = testInfo.getTestMethod().get().getName();
        if (displayName.equals(methodName + "()")) {
            String title = methodName.replace("_", " ");
            return title.substring(0, 1).toUpperCase() + title.substring(1);
        } else {
            return displayName;
        }
    }

    /**
     * Give path where docs are generated.
     *
     * @return
     */
    protected Path getDocPath() {
        return ApprovalsBase.pathBuidler.getProjectPath().resolve(Paths.get("src", "test", "docs"));
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
        sb.append(Arrays.stream(texts).collect(Collectors.joining("\n")));
    }

    protected String buildContent(TestInfo testInfo) {
        final JavaClass testInfoJavaClass = CodeExtractor.getBuilder().getClassByName(TestInfo.class.getCanonicalName());
        final String comment1 = CodeExtractor.getComment(testInfo.getTestClass().get(), testInfo.getTestMethod().get().getName());
        final String comment2 = CodeExtractor.getComment(testInfo.getTestClass().get(), testInfo.getTestMethod().get().getName(), Arrays.asList(testInfoJavaClass));
        return String.join("\n\n",
                "= " + formatTitle(testInfo),
                CodeExtractor.getComment(testInfo.getTestClass().get(), testInfo.getTestMethod().get().getName()),
                sb.toString());
    }
}
