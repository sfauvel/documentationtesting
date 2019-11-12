package org.sfvl.doctesting;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import org.approvaltests.Approvals;
import org.approvaltests.core.ApprovalWriter;
import org.approvaltests.namer.ApprovalNamer;
import org.approvaltests.writers.ApprovalTextWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.TestInfo;

import java.io.File;
import java.util.Collections;
import java.util.Optional;

/**
 * Base class for test.
 */
public class ApprovalsBase {

    private StringBuffer sb = new StringBuffer();

    protected void write(String s) {
        sb.append(s);
    }

    protected void approved() {
        Approvals.verify(sb.toString(), "adoc");
    }

    @AfterEach
    public void approvedAfterTest(TestInfo testInfo) {

        String content = String.join("\n\n",
                "= " + formatTitle(testInfo),
                getComment(testInfo.getTestClass().get(), testInfo.getTestMethod().get().getName()),
                sb.toString());

        Approvals.verify(
                new ApprovalTextWriter(content, "adoc"),
                new DocumentationNamer(testInfo),
                Approvals.getReporter());
    }

    private String formatTitle(TestInfo testInfo) {
        String title = testInfo.getTestMethod().get().getName()
                .replace("_", " ");

        return title.substring(0, 1).toUpperCase() + title.substring(1);
    }

    public String getComment(Class<?> clazz, String methodName) {
        JavaProjectBuilder builder = new JavaProjectBuilder();
        builder.addSourceTree(new File("src/test/java"));

        JavaClass javaClass = builder.getClassByName(clazz.getCanonicalName());

        JavaMethod method = javaClass.getMethod(methodName, Collections.emptyList(), false);
        return Optional.ofNullable(method.getComment()).orElse("");
    }

}
