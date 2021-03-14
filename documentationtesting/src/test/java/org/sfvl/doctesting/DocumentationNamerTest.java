package org.sfvl.doctesting;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;

import java.nio.file.Paths;

@DisplayName("Documentation Namer")
class DocumentationNamerTest {
    private static final DocWriter doc = new DocWriter();
    @RegisterExtension
    static ApprovalsExtension extension = new ApprovalsExtension(doc);

    /**
     * DocumentNamer build names and paths from a method.
     */
    @Test
    public void document_namer(TestInfo testInfo) {

        // >>>1
        final DocumentationNamer namer = new DocumentationNamer(Paths.get("src", "test", "docs"), testInfo);
        // <<<1

        doc.write(".DocumentNamer usage",
                CodeExtractor.extractPartOfMethod(testInfo.getTestMethod().get(), "1"),
                "", "");

        doc.write( "[%header]",
                "|====",
                "| Code | Result ",
                "a| `" + extract(testInfo, "2") + "` | " +
                        // >>>2
                        namer.getApprovalName()
                        // <<<2
                        ,
                "a| `" + extract(testInfo, "3") + "` | " +
                        // >>>3
                        namer.getApprovalFileName()
                        // <<<3
                ,
                "a| `" + extract(testInfo, "4") + "` | " +
                        // >>>4
                        namer.getSourceFilePath()
                        // <<<4
                ,
                "a| `" + extract(testInfo, "5") + "` | " +
                        // >>>5
                        namer.getFilePath()
                        // <<<5
                ,
                "a| `" + extract(testInfo, "6") + "` | " +
                        // >>>6
                        namer.getApprovedPath(Paths.get("src", "test", "docs", "org"))
                        // <<<6
                ,
                "a| `" + extract(testInfo, "7") + "` | " +
                        // >>>7
                        DocumentationNamer.toPath(org.sfvl.doctesting.DocumentationNamerTest.class)
                        // <<<7
                ,
                "a| `" + extract(testInfo, "8") + "` | " +
                        // >>>8
                        DocumentationNamer.toPath(org.sfvl.doctesting.DocumentationNamerTest.class.getPackage())
                        // <<<8
                ,
                "a| `" + extract(testInfo, "9") + "` | " +
                        // >>>9
                        DocumentationNamer.toPath(org.sfvl.doctesting.DocumentationNamerTest.class, "prefix_", ".suffix")
                // <<<9
                ,
                "|====");
    }

    public String extract(TestInfo testInfo, String suffix) {
        return CodeExtractor.extractPartOfMethod(testInfo.getTestMethod().get(), suffix).trim();
    }

}


