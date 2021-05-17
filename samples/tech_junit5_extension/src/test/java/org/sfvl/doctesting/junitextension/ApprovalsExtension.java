package org.sfvl.doctesting.junitextension;

import org.approvaltests.Approvals;
import org.approvaltests.namer.ApprovalNamer;
import org.approvaltests.writers.ApprovalTextWriter;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.doctesting.utils.DocWriter;
import org.sfvl.doctesting.utils.PathProvider;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Base class for test.
 *
 * It checks that everything written during test is identical to the approved content.
 */
public class ApprovalsExtension implements AfterEachCallback {

    private static final PathProvider pathBuidler = new PathProvider();

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        final DocWriter writer = getWriter(extensionContext);
        String content = writer.formatOutput(extensionContext.getDisplayName(), extensionContext.getTestMethod().get());

        final DocPath docPath = new DocPath(extensionContext.getTestMethod().get());
        ApprovalNamer approvalNamer = new ApprovalNamer() {
            @Override
            public String getApprovalName() {
                return docPath.name();
            }

            @Override
            public String getSourceFilePath() {
                return docPath.approved().folder().toString() + File.separator;
            }
        };

        Approvals.verify(
                new ApprovalTextWriter(content, "adoc"),
                approvalNamer,
                Approvals.getReporter());
    }

    protected DocWriter getWriter(ExtensionContext extensionContext) throws Exception {
        final Object obj = extensionContext.getTestInstance().get();

        Field writerField = Arrays.stream(obj.getClass().getFields())
                .filter(f -> DocWriter.class.isAssignableFrom(f.getType()))
                .findFirst().get();

        return (DocWriter) writerField.get(obj);
    }

    /**
     * Give path where docs are generated.
     *
     * @return
     */
    protected Path getDocPath() {
        return pathBuidler.getProjectPath().resolve(Paths.get("src", "test", "docs"));
    }


}
