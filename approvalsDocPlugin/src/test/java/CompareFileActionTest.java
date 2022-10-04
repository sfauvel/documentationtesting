import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import docAsTest.DocAsTestStartupActivity;
import docAsTest.action.CompareFileAction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import tools.DocAsTestPlatformTestCase;
import tools.FieldAutoNaming;
import tools.FileHelper;
import tools.MockActionOnFileEvent;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

@RunWith(JUnit4.class)
public class CompareFileActionTest extends DocAsTestPlatformTestCase {

    class SpyCompareFileAction extends CompareFileAction {
        private VirtualFile fileApproved;
        private VirtualFile fileReceived;
        boolean isShowDiffCalled = false;

        @Override
        protected String getProjectBasePath(Project project) {
            return "/";
        }

        @Override
        protected void showDiff(AnActionEvent e, VirtualFile fileApproved, VirtualFile fileReceived) {
            assertFalse("showDiff must be called only once", isShowDiffCalled);
            isShowDiffCalled = true;
            this.fileApproved = fileApproved;
            this.fileReceived = fileReceived;
        }
    };

    final SpyCompareFileAction actionUnderTest = new SpyCompareFileAction();

    public static class fileNames extends FieldAutoNaming {
        public String docs_fileA_received_adoc;
        public String docs_fileA_approved_adoc;
        public String docs_fileA_myMethod_received_adoc;
        public String docs_fileA_myMethod_approved_adoc;
    }
    
    private MockActionOnFileEvent actionEvent;
    final private fileNames FILE_NAMES = new fileNames();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        actionEvent = new MockActionOnFileEvent(myFixture);

        final Properties properties = new Properties();
        properties.setProperty("TEST_PATH", "src");
        // We are not able to put a file outside of /src for now so we put docs folder in src.
        properties.setProperty("DOC_PATH", "src/docs");
        DocAsTestStartupActivity.setProperties(properties);
    }

    @Test
    public void test_compare_file_menu_when_no_received_file() throws IOException {
        final Map<String, PsiFile> files = fileHelper.initFiles(
                FILE_NAMES.docs_fileA_approved_adoc
        );

        actionEvent.performUpdate(new CompareFileAction(),
                files.get(FILE_NAMES.docs_fileA_approved_adoc));

        assertFalse(actionEvent.getPresentation().isEnabledAndVisible());
    }

    @Test
    public void test_compare_file_menu_when_no_approved_file() throws IOException {
        final Map<String, PsiFile> files = fileHelper.initFiles(
                FILE_NAMES.docs_fileA_received_adoc
        );

        actionEvent.performUpdate(new CompareFileAction(),
                files.get(FILE_NAMES.docs_fileA_received_adoc));

        assertFalse(actionEvent.getPresentation().isEnabledAndVisible());
    }

    @Test
    public void test_compare_from_menu_receive_file_when_approved_and_received_files() {
        assert_menu_when_compare_file_menu_when_approved_and_received_files(FILE_NAMES.docs_fileA_received_adoc);
    }

    @Test
    public void test_compare_from_menu_approved_file_when_approved_and_received_files() {
        assert_menu_when_compare_file_menu_when_approved_and_received_files(FILE_NAMES.docs_fileA_approved_adoc);
    }

    private void assert_menu_when_compare_file_menu_when_approved_and_received_files(String selected_file) {
        final Map<String, PsiFile> files = fileHelper.initFiles(
                FILE_NAMES.docs_fileA_received_adoc,
                FILE_NAMES.docs_fileA_approved_adoc
        );

        actionEvent.performUpdate(new CompareFileAction(),
                files.get(selected_file));

        assertTrue(actionEvent.getPresentation().isEnabledAndVisible());
    }

    @Test
    public void test_compare_file_menu_from_java_file_when_approved_and_received_files() {
        final Map<String, PsiFile> files = fileHelper.initFiles(
                FILE_NAMES.docs_fileA_received_adoc,
                FILE_NAMES.docs_fileA_approved_adoc
        );

        final PsiFile classFile = fileHelper.addTestClassFile("FileA", FileHelper.CaretOn.CLASS);

        actionEvent.performUpdateOnEditor(new CompareFileAction(){
            @Override
            protected String getProjectBasePath(Project project) {
                return "/";
            }
        }, myFixture, classFile);

        assertTrue(actionEvent.getPresentation().isEnabledAndVisible());
    }

    @Test
    public void test_compare_file_menu_from_java_method_when_approved_and_received_files() {
        final Map<String, PsiFile> files = fileHelper.initFiles(
                FILE_NAMES.docs_fileA_myMethod_received_adoc,
                FILE_NAMES.docs_fileA_myMethod_approved_adoc
        );

        final PsiFile classFile = fileHelper.addTestClassFile("FileA", FileHelper.CaretOn.METHOD);

        actionEvent.performUpdateOnEditor(actionUnderTest, myFixture, classFile);
        assertTrue(actionEvent.getPresentation().isEnabledAndVisible());

        actionEvent.performActionOnEditor(actionUnderTest, myFixture, classFile);

        assertTrue(actionUnderTest.isShowDiffCalled);
        assertEquals(files.get(FILE_NAMES.docs_fileA_myMethod_approved_adoc).getVirtualFile().getPath(), actionUnderTest.fileApproved.getPath());
        assertEquals(files.get(FILE_NAMES.docs_fileA_myMethod_received_adoc).getVirtualFile().getPath(), actionUnderTest.fileReceived.getPath());

    }

    @Test
    public void test_no_compare_file_menu_from_java_method_when_only_approved_file() {
        no_compare_file_menu_from_java_method_when_not_approved_and_received(
                FILE_NAMES.docs_fileA_myMethod_approved_adoc);
    }

    @Test
    public void test_no_compare_file_menu_from_java_method_when_only_received_file() {
        no_compare_file_menu_from_java_method_when_not_approved_and_received(
                FILE_NAMES.docs_fileA_myMethod_received_adoc);
    }

    private void no_compare_file_menu_from_java_method_when_not_approved_and_received(String file_to_create) {
        final Map<String, PsiFile> files = fileHelper.initFiles(
                file_to_create
        );

        final PsiFile classFile = fileHelper.addTestClassFile("FileA", FileHelper.CaretOn.METHOD);

        actionEvent.performUpdateOnEditor(new CompareFileAction(){
            @Override
            protected String getProjectBasePath(Project project) {
                return "/";
            }
        }, myFixture, classFile);

        assertFalse(actionEvent.getPresentation().isEnabledAndVisible());
    }

}
