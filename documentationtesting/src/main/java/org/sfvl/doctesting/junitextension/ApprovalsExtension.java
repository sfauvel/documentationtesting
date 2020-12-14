package org.sfvl.doctesting.junitextension;

import org.approvaltests.Approvals;
import org.approvaltests.core.ApprovalFailureReporter;
import org.approvaltests.namer.ApprovalNamer;
import org.approvaltests.writers.ApprovalTextWriter;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.sfvl.doctesting.DocWriter;
import org.sfvl.doctesting.DocumentationNamer;
import org.sfvl.doctesting.PathProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * JUnit5 extension that verify written document matches with approved one.
 *
 * It checks that everything written during test is identical to the approved content.
 */
public class ApprovalsExtension<T extends DocWriter> implements AfterEachCallback {

    private static final PathProvider pathBuidler = new PathProvider();
    private T docWriter;

    public ApprovalsExtension(T docWriter) {
        this.docWriter = docWriter;
    }

    public T getDocWriter() {
        return docWriter;
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        String content = getDocWriter().formatOutput(extensionContext.getDisplayName(), extensionContext.getTestMethod().get());

        final DocumentationNamer documentationNamer = new DocumentationNamer(getDocPath(), extensionContext.getTestMethod().get());
        ApprovalNamer approvalNamer = new ApprovalNamer() {

            @Override
            public String getApprovalName() {
                return documentationNamer.getApprovalName();
            }

            @Override
            public String getSourceFilePath() {
                return documentationNamer.getSourceFilePath();
            }
        };

        final ApprovalFailureReporter approvalFailureReporter = new ApprovalFailureReporter() {
            ApprovalFailureReporter delegate = Approvals.getReporter();

            @Override
            public void report(String received, String approved) {
                delegate.report(received, approved);


                System.out.println(approved);
                System.out.println("*****************************************************************");
                try {
                    Files.lines(Paths.get(approved)).forEach(System.out::println);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("*****************************************************************");
                System.out.println(received);
                try {
                    Files.lines(Paths.get(received)).forEach(System.out::println);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("*****************************************************************");

                try {
                    final List<String> approvedLines = Files.lines(Paths.get(approved)).collect(Collectors.toList());
                    final List<String> receivedLines = Files.lines(Paths.get(received)).collect(Collectors.toList());

                    for (int lineNumber = 0; lineNumber < approvedLines.size(); lineNumber++) {
                        if (!approvedLines.get(lineNumber).equals(receivedLines.get(lineNumber))) {
                            System.out.println(String.format("%d: %s", lineNumber, receivedLines.get(lineNumber)));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("*****************************************************************");

            }
        };

        Approvals.verify(
                new ApprovalTextWriter(content, "adoc"),
                approvalNamer,
                approvalFailureReporter);
    }

    /**
     * Give path where docs are generated.
     * @return
     */
    public Path getDocPath() {
        return pathBuidler.getProjectPath().resolve(Paths.get( "src", "test", "docs"));
    }




}
