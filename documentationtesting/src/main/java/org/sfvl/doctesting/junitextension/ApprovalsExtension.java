package org.sfvl.doctesting.junitextension;

import org.approvaltests.Approvals;
import org.approvaltests.core.ApprovalFailureReporter;
import org.approvaltests.namer.ApprovalNamer;
import org.approvaltests.writers.ApprovalTextWriter;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.ModifierSupport;
import org.sfvl.doctesting.utils.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JUnit5 extension that verify written document matches with approved one.
 *
 * It checks that everything written during test is identical to the approved content.
 */
public class ApprovalsExtension<T extends DocWriter> implements AfterEachCallback, AfterAllCallback {

    public static <T extends DocWriter> ApprovalsExtension<T> build(T docWriter) {
        return new ApprovalsExtension<T>(docWriter);
    }

    private static final PathProvider pathBuidler = new PathProvider();
    private T docWriter;

    public ApprovalsExtension(T docWriter) {
        this.docWriter = docWriter;
    }

    public T getDocWriter() {
        return docWriter;
    }

    public void write(String... texts) {
        this.docWriter.write(texts);
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
        String content = getDocWriter().formatOutput(extensionContext.getDisplayName(), extensionContext.getTestMethod().get());
        getDocWriter().reset();
        content += extensionContext.getExecutionException()
                .map(this::displayFailingReason)
                .orElse("");

        verifyDoc(content, new DocPath(extensionContext.getTestMethod().get()));
    }

    public void verifyDoc(String content, DocPath docPath) {
        ApprovalNamer approvalNamer = new ApprovalNamer() {
            @Override
            public String getApprovalName() {
                return "_"+docPath.name();
            }

            @Override
            public String getSourceFilePath() {
                return docPath.approved().folder().toString() + File.separator;
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

    public String displayFailingReason(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        return String.join("\n",
                "*Error generating documentation*",
                "----",
                sw.toString(),
                "----");
    }

    /**
     * Give path where docs are generated.
     *
     * @return
     */
    public Path getDocPath() {
        return pathBuidler.getProjectPath().resolve(Config.DOC_PATH);
    }


    public static class FailureReporter implements ApprovalFailureReporter {
        ApprovalFailureReporter delegate = Approvals.getReporter();
        private final PrintStream out;

        public FailureReporter() {
            this(System.out);
        }

        public FailureReporter(PrintStream out) {
            this.out = out;
        }

        @Override
        public void report(String received, String approved) {
            delegate.report(received, approved);

            try {
                final List<String> approvedLines = Files.lines(Paths.get(approved)).collect(Collectors.toList());
                final List<String> receivedLines = Files.lines(Paths.get(received)).collect(Collectors.toList());

                int lineNumber = 0;
                while (lineNumber < approvedLines.size()
                        && lineNumber < receivedLines.size()
                        && approvedLines.get(lineNumber).equals(receivedLines.get(lineNumber))) {
                    lineNumber++;
                }

                if (lineNumber >= approvedLines.size() && lineNumber >= receivedLines.size()) {
                    out.println(String.format("Files are identical:\n    Approved: %s\n    Received: %s",
                            approved,
                            received));
                } else {
                    out.println(String.format("Differences between files:\n    Approved: %s\n    Received: %s",
                            approved,
                            received));
                    out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                    out.println(formatLine(approvedLines, lineNumber));
                    out.println("=================================================================");
                    out.println(formatLine(receivedLines, lineNumber));
                    out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        private String formatLine(List<String> lines, int lineNumber) {
            if (lineNumber < lines.size()) {
                return String.format("%d: %s", (lineNumber + 1), lines.get(lineNumber));
            } else {
                return String.format("%d (no line)", (lineNumber + 1));
            }
        }
    }
}
