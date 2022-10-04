import com.intellij.psi.PsiFile;
import docAsTest.DocAsTestFilenameIndex;
import docAsTest.DocAsTestStartupActivity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import tools.DocAsTestPlatformTestCase;

@RunWith(JUnit4.class)
public class DocAsTestStartupActivityTest extends DocAsTestPlatformTestCase {

    public void setUp() throws Exception {
        DocAsTestFilenameIndex.setSlowOperationPolicy(DocAsTestPlatformTestCase.NO_SLOW_OPERATION_POLICY);
        super.setUp();
        DocAsTestStartupActivity.reset();
    }

    @Test
    public void test_src_docs_has_default_value_when_no_property_file() throws Exception {
        new DocAsTestStartupActivity().runActivity(myFixture.getProject());
        assertEquals("src/test/docs", DocAsTestStartupActivity.getSrcDocs());
    }

    @Test
    public void test_load_property_file_on_startup() throws Exception {
        final PsiFile propertyFile = myFixture.addFileToProject("docAsTest.properties", "DOC_PATH:src/doc/custom_folder");
        new DocAsTestStartupActivity().runActivity(myFixture.getProject());
        assertEquals("src/doc/custom_folder", DocAsTestStartupActivity.getSrcDocs());
    }
}
