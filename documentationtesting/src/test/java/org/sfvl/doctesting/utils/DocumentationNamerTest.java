package org.sfvl.doctesting.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;

import java.nio.file.Paths;
import java.util.function.Function;

@DisplayName("Documentation Namer")
class DocumentationNamerTest {
    private static final DocWriter doc = new DocWriter();
    @RegisterExtension
    static ApprovalsExtension extension = new ApprovalsExtension(doc);

    /**
     * DocumentNamer build names and paths from a method or a class.
     */
    @Test
    public void document_namer(TestInfo testInfo) {

        // >>>1_method
        final DocumentationNamer namerMethod =
                new DocumentationNamer(Config.DOC_PATH, testInfo);
        // <<<1_method

        // >>>1_class
        final DocumentationNamer namerClass =
                new DocumentationNamer(Config.DOC_PATH, this.getClass());
        // <<<1_class

        doc.write(".DocumentNamer usage with method",
                CodeExtractor.extractPartOfMethod(testInfo.getTestMethod().get(), "1_method"),
                "", "");

        doc.write(".DocumentNamer usage with class",
                CodeExtractor.extractPartOfMethod(testInfo.getTestMethod().get(), "1_class"),
                "", "");

        doc.write( "[%header]",
                "|====",
                "| Code | Result with method | Result with class",
                getS(testInfo, namerMethod, namerClass, namer ->
                        // >>>2
                        namer.getApprovalName()
                        // <<<2
                        , "2"
                ),
                getS(testInfo, namerMethod, namerClass, namer ->
                        // >>>3
                        namer.getApprovalFileName()
                        // <<<3
                        , "3"
                ),
                getS(testInfo, namerMethod, namerClass, namer ->
                        // >>>4
                        namer.getSourceFilePath()
                        // <<<4
                        , "4"
                ),
                getS(testInfo, namerMethod, namerClass, namer ->
                        // >>>5
                        namer.getFilePath()
                        // <<<5
                        , "5"
                ),
                getS(testInfo, namerMethod, namerClass, namer ->
                        // >>>6
                        namer.getApprovedPath(Config.DOC_PATH.resolve("org"))
                        // <<<6
                        , "6"
                ),
                "|====");

    }

    private String getS(TestInfo testInfo, DocumentationNamer namer, DocumentationNamer namerClass, Function<DocumentationNamer, Object> t, String suffix) {
        return "3.+a| `" + extract(testInfo, suffix) + "`\n| | " +
                t.apply(namer)
                + " | " + t.apply(namerClass);
    }


    /**
     * DocumentNamer build names and paths from a method.
     */
    @Test
    public void document_namer_static_methods(TestInfo testInfo) {

        doc.write( "[%header]",
                "|====",
                "| Code | Result ",
                "a| `" + extract(testInfo, "7") + "` | " +
                        // >>>7
                        DocumentationNamer.toPath(DocumentationNamerTest.class)
                // <<<7
                ,
                "a| `" + extract(testInfo, "8") + "` | " +
                        // >>>8
                        DocumentationNamer.toPath(DocumentationNamerTest.class.getPackage())
                // <<<8
                ,
                "a| `" + extract(testInfo, "9") + "` | " +
                        // >>>9
                        DocumentationNamer.toPath(DocumentationNamerTest.class, "prefix_", ".suffix")
                // <<<9
                ,
                "a| `" + extract(testInfo, "10") + "` | " +
                        // >>>10
                        DocumentationNamer.toAsciiDocFilePath(DocumentationNamerTest.class)
                // <<<10
                ,
                "|====");
    }

    public String extract(TestInfo testInfo, String suffix) {
        return CodeExtractor.extractPartOfMethod(testInfo.getTestMethod().get(), suffix).trim();
    }

}


