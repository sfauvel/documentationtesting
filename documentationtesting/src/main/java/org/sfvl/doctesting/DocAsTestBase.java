package org.sfvl.doctesting;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.TestInfo;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class DocAsTestBase {
    protected static final PathProvider pathBuidler = new PathProvider();
    protected StringBuffer sb = new StringBuffer();
    private static JavaProjectBuilder builder;

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
     * Init JavaProjectBuilder it's a bit long (more than 100ms).
     * We init it only once to avoid this low performances.
     */
    static {
        if (builder == null) {
            builder = new JavaProjectBuilder();
            builder.addSourceTree(new File("src/main/java"));
            builder.addSourceTree(new File("src/test/java"));
        }
    }

    protected String getComment(Class<?> clazz, String methodName) {
        return getComment(clazz, methodName, Collections.emptyList());
    }

    protected String getComment(Class<?> clazz, String methodName, List<JavaType> argumentList) {
        JavaClass javaClass = builder.getClassByName(clazz.getCanonicalName());

        JavaMethod method = javaClass.getMethod(methodName, argumentList, false);
        while (method == null && javaClass.getSuperJavaClass() != null) {
            javaClass = javaClass.getSuperJavaClass();
            method = javaClass.getMethod(methodName, argumentList, false);
        }
        return Optional.ofNullable(method).map(c -> c.getComment()).orElse("");
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
        final JavaClass testInfoJavaClass = builder.getClassByName(TestInfo.class.getCanonicalName());
        final String comment1 = getComment(testInfo.getTestClass().get(), testInfo.getTestMethod().get().getName());
        final String comment2 = getComment(testInfo.getTestClass().get(), testInfo.getTestMethod().get().getName(), Arrays.asList(testInfoJavaClass));
        return String.join("\n\n",
                "= " + formatTitle(testInfo),
                getComment(testInfo.getTestClass().get(), testInfo.getTestMethod().get().getName()),
                sb.toString());
    }
}
