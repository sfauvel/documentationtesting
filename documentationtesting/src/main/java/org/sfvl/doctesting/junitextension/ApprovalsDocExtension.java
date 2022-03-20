package org.sfvl.doctesting.junitextension;

import org.approvaltests.Approvals;
import org.approvaltests.namer.ApprovalNamer;
import org.approvaltests.writers.ApprovalTextWriter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.ModifierSupport;
import org.sfvl.codeextraction.CodeExtractor;
import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.doctesting.utils.PathProvider;
import org.sfvl.doctesting.writer.DocWriter;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;

/**
 * JUnit5 extension that verify written document matches with approved one.
 *
 * It checks that everything written during test is identical to the approved content.
 */
public class ApprovalsDocExtension<T extends DocWriter> implements AfterEachCallback, AfterAllCallback {

    private static final PathProvider pathBuidler = new PathProvider();
    private T docWriter;

    public ApprovalsDocExtension(T docWriter) {
        this.docWriter = docWriter;
    }

    public T getDocWriter() {
        return docWriter;
    }

    private boolean isNestedClass(Class<?> currentClass) {
        return !ModifierSupport.isStatic(currentClass) && currentClass.isMemberClass();
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        final Class<?> currentClass = extensionContext.getTestClass().get();
        if (isNestedClass(currentClass)) {
            return;
        }
        final String content = docWriter.formatOutput(currentClass);

        verifyDoc(content, new DocPath(extensionContext.getTestClass().get()));

    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {

        final Method testMethod = extensionContext.getTestMethod().get();
        final DisplayName displayName = testMethod.getAnnotation(DisplayName.class);

        String content = displayName != null
                ? getDocWriter().formatOutput(displayName.value(), testMethod)
                : getDocWriter().formatOutput(testMethod);

        content += extensionContext.getExecutionException()
                .map(e -> getDocWriter().formatException(e, "Error generating documentation"))
                .orElse("");

        getDocWriter().reset();

        verifyDoc(content, new DocPath(extensionContext.getTestMethod().get()));
    }

    public void verifyDoc(String content, DocPath docPath) {
        ApprovalNamer approvalNamer = new ApprovalNamer() {
            @Override
            public String getApprovalName() {
                return "_" + docPath.name();
            }

            @Override
            public String getSourceFilePath() {
                return docPath.approved().folder().toString() + File.separator;
            }

            @Override
            public File getApprovedFile(String extensionWithDot) {
                return new File(this.getSourceFilePath() + "/" + this.getApprovalName() + ".approved" + extensionWithDot);
            }

            @Override
            public File getReceivedFile(String extensionWithDot) {
                return new File(this.getSourceFilePath() + "/" + this.getApprovalName() + ".received" + extensionWithDot);
            }
        };

        verifyDoc(content, approvalNamer);
    }

    private void verifyDoc(String content, ApprovalNamer approvalNamer) {
        Approvals.verify(
                new ApprovalTextWriter(content, "adoc"),
                approvalNamer,
                new FailureReporter());
    }

}