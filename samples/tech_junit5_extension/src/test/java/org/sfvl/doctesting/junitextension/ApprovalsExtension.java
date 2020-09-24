package org.sfvl.doctesting.junitextension;

import org.approvaltests.Approvals;
import org.approvaltests.namer.ApprovalNamer;
import org.approvaltests.writers.ApprovalTextWriter;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.sfvl.doctesting.DocumentationNamer;
import org.sfvl.doctesting.PathProvider;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Base class for test.
 *
 * It checks that everything written during test is identical to the approved content.
 */
public class ApprovalsExtension implements BeforeEachCallback, AfterEachCallback {

    private static final PathProvider pathBuidler = new PathProvider();

    private Writer writer = null;

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        final Object obj = extensionContext.getTestInstance().get();

        Field writerField = Arrays.stream(obj.getClass().getFields())
                .filter(f -> Writer.class.isAssignableFrom(f.getType()))
                .findFirst().get();

        writer = (Writer) writerField.get(obj);
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {

        String content = writer.formatOutput(extensionContext.getDisplayName(), extensionContext.getTestMethod().get());

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

        Approvals.verify(
                new ApprovalTextWriter(content, "adoc"),
                approvalNamer,
                Approvals.getReporter());
    }

    /**
     * Give path where docs are generated.
     * @return
     */
    protected Path getDocPath() {
        return pathBuidler.getProjectPath().resolve(Paths.get( "src", "test", "docs"));
    }




}
