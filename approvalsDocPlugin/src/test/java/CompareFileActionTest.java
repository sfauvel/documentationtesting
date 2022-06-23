import com.intellij.psi.PsiFile;
import tools.DocAsTestPlatformTest;
import tools.FieldAutoNaming;
import tools.FileHelper;
import tools.MockActionOnFileEvent;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

public class CompareFileActionTest extends DocAsTestPlatformTest {

    public static class fileNames extends FieldAutoNaming {
        public String folder1_fileA_received_adoc;
        public String folder1_fileA_approved_adoc;
    }
    
    private MockActionOnFileEvent actionEvent;
    final private fileNames FILE_NAMES = new fileNames();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        actionEvent = new MockActionOnFileEvent(myFixture);
    }

    public void test_compare_file_menu_when_no_received_file() throws IOException {
        final Map<String, PsiFile> files = fileHelper.initFiles(
                FILE_NAMES.folder1_fileA_approved_adoc
        );

        actionEvent.performUpdate(new CompareFileAction(),
                files.get(FILE_NAMES.folder1_fileA_approved_adoc));

        assertFalse(actionEvent.getPresentation().isEnabledAndVisible());
    }

    public void test_compare_file_menu_when_no_approved_file() throws IOException {
        final Map<String, PsiFile> files = fileHelper.initFiles(
                FILE_NAMES.folder1_fileA_received_adoc
        );

        actionEvent.performUpdate(new CompareFileAction(),
                files.get(FILE_NAMES.folder1_fileA_received_adoc));

        assertFalse(actionEvent.getPresentation().isEnabledAndVisible());
    }

    public void test_compare_from_menu_receive_file_when_approved_and_received_files() {
        assert_menu_when_compare_file_menu_when_approved_and_received_files(FILE_NAMES.folder1_fileA_received_adoc);
    }

    public void test_compare_from_menu_approved_file_when_approved_and_received_files() {
        assert_menu_when_compare_file_menu_when_approved_and_received_files(FILE_NAMES.folder1_fileA_approved_adoc);
    }

    private void assert_menu_when_compare_file_menu_when_approved_and_received_files(String selected_file) {
        final Map<String, PsiFile> files = fileHelper.initFiles(
                FILE_NAMES.folder1_fileA_received_adoc,
                FILE_NAMES.folder1_fileA_approved_adoc
        );

        actionEvent.performUpdate(new CompareFileAction(),
                files.get(selected_file));

        assertTrue(actionEvent.getPresentation().isEnabledAndVisible());
    }

    public void test_compare_file_menu_from_java_file_when_approved_and_received_files() {
        final Map<String, PsiFile> files = fileHelper.initFiles(
                FILE_NAMES.folder1_fileA_received_adoc,
                FILE_NAMES.folder1_fileA_approved_adoc
        );

        final PsiFile classFile = fileHelper.addTestClassFile("FileA", FileHelper.CaretOn.CLASS);

        actionEvent.performUpdate(new CompareFileAction(),classFile);

        assertTrue(actionEvent.getPresentation().isEnabledAndVisible());
    }

}
