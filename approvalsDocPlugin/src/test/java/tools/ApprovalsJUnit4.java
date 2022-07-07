package tools;

import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.approvaltests.namer.ApprovalNamer;
import org.approvaltests.writers.ApprovalTextWriter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.sfvl.codeextraction.CodeExtractor;
import org.sfvl.docformatter.asciidoc.AsciidocFormatter;
import org.sfvl.doctesting.junitextension.DocAsTestApprovalNamer;
import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.doctesting.writer.ClassDocumentation;
import org.sfvl.doctesting.writer.DocWriter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Paths;

/**
 * Base class for test.
 */
public class ApprovalsJUnit4 {

    static {
        CodeExtractor.init(Config.TEST_PATH, Config.SOURCE_PATH);
    }

    @Rule
    public TestName testName = new TestName();

    private static Class<?> testClass;

    final static DocWriter<AsciidocFormatter> docWriter = new DocWriter<AsciidocFormatter>(new AsciidocFormatter()) {
        private final ClassDocumentation classDocumentation = new ClassDocumentation(
                getFormatter(),
                o -> Paths.get(o.filename()),
                m -> m.isAnnotationPresent(org.junit.Test.class),
                m -> true
        );

        public String formatOutput(Class<?> clazz) {
            return String.join("\n",
                    defineDocPath(clazz),
                    "",
                    classDocumentation.getClassDocumentation(clazz)
            );
        }

    };

    protected void write(String... lines) {
        docWriter.write(lines);
    }

    protected static void approved(DocPath docPath, String content) {
        ApprovalNamer approvalNamer = new DocAsTestApprovalNamer(docPath);

        final Options options = new Options()
                .forFile().withExtension(".adoc");

        Approvals.verify(
                new ApprovalTextWriter(content, options),
                approvalNamer);
    }

    @After
    public void approvedAfterTest() throws NoSuchMethodException {

        final String methodName = testName.getMethodName();
        final Method testMethod = this.getClass().getDeclaredMethod(methodName);

        testClass = testMethod.getDeclaringClass();
        final String content = docWriter.formatOutput(testMethod);
        approved(new DocPath(testMethod), content);
    }

    @AfterClass
    public static void writeTestDoc() throws IOException {
        final String content = docWriter.formatOutput(testClass);
        final DocPath path = new DocPath(testClass);

        approved(path, content);
    }

}
