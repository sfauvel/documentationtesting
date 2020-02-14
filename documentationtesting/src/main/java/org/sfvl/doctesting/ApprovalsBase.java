package org.sfvl.doctesting;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import org.approvaltests.Approvals;
import org.approvaltests.writers.ApprovalTextWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.TestInfo;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;

/**
 * Base class for test.
 *
 * It checks that everything write during test is identically to the content approved.
 */
public class ApprovalsBase {

    private StringBuffer sb = new StringBuffer();

    /**
     * Write a text to the output.
     * @param text
     */
    protected void write(String text) {
        sb.append(text);
    }

    @AfterEach
    public void approvedAfterTest(TestInfo testInfo) {

        String content = String.join("\n\n",
                "= " + formatTitle(testInfo),
                getComment(testInfo.getTestClass().get(), testInfo.getTestMethod().get().getName()),
                sb.toString());

        final Path docRootPath = Paths.get(this.getClass().getClassLoader().getResource("").getPath())
                .resolve(Paths.get("..", "..", "src", "test", "docs"));

        Approvals.verify(
                new ApprovalTextWriter(content, "adoc"),
                new DocumentationNamer(docRootPath, testInfo),
                Approvals.getReporter());
    }

    /**
     * Return name specified in DisplayName annotation.
     * If annotation is not present, this is the method name taht will be returned
     * after some test formmatting (remove '_', uppercase first letter).
     * @param testInfo
     * @return
     */
    private String formatTitle(TestInfo testInfo) {
        String displayName = testInfo.getDisplayName();
        String methodName = testInfo.getTestMethod().get().getName();
        if (displayName.equals(methodName+"()")) {
            String title = methodName.replace("_", " ");
            return title.substring(0, 1).toUpperCase() + title.substring(1);
        } else {
            return displayName;
        }
    }

    private String getComment(Class<?> clazz, String methodName) {
        JavaProjectBuilder builder = new JavaProjectBuilder();
        builder.addSourceTree(new File("src/test/java"));

        JavaClass javaClass = builder.getClassByName(clazz.getCanonicalName());

        JavaMethod method = javaClass.getMethod(methodName, Collections.emptyList(), false);
        return Optional.ofNullable(method.getComment()).orElse("");
    }

}
