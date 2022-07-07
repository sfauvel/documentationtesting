import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.command.undo.UndoManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import docAsTest.action.ApproveFileAction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import tools.DocAsTestPlatformTestCase;
import tools.FieldAutoNaming;
import tools.MockActionOnFileEvent;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * Testing a plugin:
 * - test update to check there is no call to slow metode
 * - test action
 * - test undo
 *
 * Contexts:
 * - test one or several files selected in menu
 * - test one or several directories selected in menu
 * - test several files and directories selected in menu
 * - test editor selected
 */
@RunWith(JUnit4.class)
public class ApproveFileActionTest extends DocAsTestPlatformTestCase {

    public static class fileNames extends FieldAutoNaming {
        public String folder1_fileA_received_adoc;
        public String folder1_fileA_approved_adoc;
        public String folder1_fileB_received_adoc;
        public String folder1_fileB_approved_adoc;
        public String folder1_fileC_received_adoc;
        public String folder1_fileC_approved_adoc;
        public String folder1_fileA_multi_dot_in_name_received_adoc;
        public String folder1_fileA_multi_dot_in_name_approved_adoc;

        public String folder2_fileA_txt;
    }

    public static class folderNames extends FieldAutoNaming {
        public String folder1;
        public String folder2;
    }

    private MockActionOnFileEvent actionEvent;
    final private fileNames FILE_NAMES = new fileNames();
    final private folderNames FOLDER_NAMES = new folderNames();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        actionEvent = new MockActionOnFileEvent(myFixture);
    }

    @Test
    public void testMenuWithoutFile() throws IOException {
        actionEvent.performUpdate(new ApproveFileAction(),
                Arrays.asList());

        final Presentation presentation = actionEvent.getPresentation();
        assertFalse(presentation.isEnabledAndVisible());
    }
    
    @Test
    public void testOneReceivedCanBeApprovedFile() throws IOException {
        final Map<String, PsiFile> files = fileHelper.initFiles(FILE_NAMES.folder1_fileA_received_adoc);

        actionEvent.performUpdate(new ApproveFileAction(), Arrays.asList(
                files.get(FILE_NAMES.folder1_fileA_received_adoc)));

        final Presentation presentation = actionEvent.getPresentation();
        assertTrue(presentation.isEnabledAndVisible());
        assertEquals("Approved file", presentation.getText());
    }

    @Test
    public void testOneApprovedAloneHasNoMenuFile() throws IOException {
        final Map<String, PsiFile> files = fileHelper.initFiles(FILE_NAMES.folder1_fileA_approved_adoc);

        actionEvent.performUpdate(new ApproveFileAction(), Arrays.asList(
                files.get(FILE_NAMES.folder1_fileA_approved_adoc)));

        assertFalse(actionEvent.getPresentation().isEnabledAndVisible());
    }

    @Test
    public void testOneFolderWithReceivedFileCanApprovedAll() throws IOException {
        final Map<String, PsiFile> files = fileHelper.initFiles(FILE_NAMES.folder1_fileA_received_adoc);
        final Map<String, PsiDirectory> folders = fileHelper.initFolders(FOLDER_NAMES.folder1);

        actionEvent.performUpdate(new ApproveFileAction(), Arrays.asList(
                folders.get(FOLDER_NAMES.folder1)));

        assertTrue(actionEvent.getPresentation().isEnabledAndVisible());
        assertEquals("Approved All", actionEvent.getPresentation().getText());
    }

    @Test
    public void testMultipleReceivedFilesCanBeApproved() throws IOException {
        final Map<String, PsiFile> files = fileHelper.initFiles(FILE_NAMES.folder1_fileA_received_adoc, FILE_NAMES.folder1_fileB_received_adoc);

        actionEvent.performUpdate(new ApproveFileAction(), Arrays.asList(
                files.get(FILE_NAMES.folder1_fileA_received_adoc),
                files.get(FILE_NAMES.folder1_fileB_received_adoc)));

        assertTrue(actionEvent.getPresentation().isEnabledAndVisible());
        assertEquals("Approved selected files", actionEvent.getPresentation().getText());
    }

    @Test
    public void testMenuForMultipleDirectories() throws IOException {
        final Map<String, PsiDirectory> folders = fileHelper.initFolders(FOLDER_NAMES.folder1, FOLDER_NAMES.folder2);

        actionEvent.performUpdate(new ApproveFileAction(), Arrays.asList(
                folders.get(FOLDER_NAMES.folder1),
                folders.get(FOLDER_NAMES.folder2)
        ));

        assertTrue(actionEvent.getPresentation().isEnabledAndVisible());
        assertEquals("Approved selected files", actionEvent.getPresentation().getText());
    }

    @Test
    public void testMenuForFileAndDirectoryProposedToApprovedAll() throws IOException {
        final Map<String, PsiFile> files = fileHelper.initFiles(FILE_NAMES.folder1_fileA_received_adoc, FILE_NAMES.folder1_fileB_received_adoc);

        final Map<String, PsiDirectory> folders = fileHelper.initFolders(FOLDER_NAMES.folder1, FOLDER_NAMES.folder2);

        actionEvent.performUpdate(new ApproveFileAction(), Arrays.asList(
                files.get(FILE_NAMES.folder1_fileA_received_adoc),
                folders.get(FOLDER_NAMES.folder2)));

        assertTrue(actionEvent.getPresentation().isEnabledAndVisible());
        assertEquals("Approved selected files", actionEvent.getPresentation().getText());
    }

    @Test
    public void testHideWhenNoReceivedFileSelected() throws IOException {
        final Map<String, PsiFile> files = fileHelper.initFiles(FILE_NAMES.folder1_fileA_approved_adoc, FILE_NAMES.folder1_fileB_received_adoc, FILE_NAMES.folder1_fileC_approved_adoc);

        actionEvent.performUpdate(new ApproveFileAction(),
                Arrays.asList(
                        files.get(FILE_NAMES.folder1_fileA_approved_adoc),
                        files.get(FILE_NAMES.folder1_fileC_approved_adoc)
                ));

        assertFalse(actionEvent.getPresentation().isEnabledAndVisible());
    }

    @Test
    public void testShowIfDirectorySelectedEvenNoReceivedFiles() throws IOException {
        Map<String, PsiFile> files = fileHelper.initFiles(FILE_NAMES.folder2_fileA_txt, FILE_NAMES.folder1_fileA_approved_adoc, FILE_NAMES.folder1_fileB_approved_adoc, FILE_NAMES.folder1_fileC_approved_adoc);

        Map<String, PsiDirectory> folders = fileHelper.initFolders(FOLDER_NAMES.folder1, FOLDER_NAMES.folder2);

        actionEvent.performUpdate(new ApproveFileAction(), Arrays.asList(
                folders.get(FOLDER_NAMES.folder1),
                folders.get(FOLDER_NAMES.folder2)
        ));

        assertTrue(actionEvent.getPresentation().isEnabledAndVisible());
        assertEquals("Approved selected files", actionEvent.getPresentation().getText());
    }

    ///////////////////////////////////////::
    // performAction

    @Test
    public void test_approved_one_received_file_with_multi_dot_in_name() throws IOException {
        final Map<String, PsiFile> files = fileHelper.initFiles(FILE_NAMES.folder1_fileA_multi_dot_in_name_received_adoc, FILE_NAMES.folder1_fileA_multi_dot_in_name_approved_adoc);

        actionEvent.performAction(new ApproveFileAction(),
                files.get(FILE_NAMES.folder1_fileA_multi_dot_in_name_received_adoc)
        );

        assertNotExists(FILE_NAMES.folder1_fileA_multi_dot_in_name_received_adoc);
        assertExists(FILE_NAMES.folder1_fileA_multi_dot_in_name_approved_adoc);
    }

    @Test
    public void test_approved_file_to_replace_a_previous_approbation() throws IOException {
        final Map<String, PsiFile> files = fileHelper.initFiles(FILE_NAMES.folder1_fileA_approved_adoc, FILE_NAMES.folder1_fileA_received_adoc);

        actionEvent.performAction(new ApproveFileAction(),
                files.get(FILE_NAMES.folder1_fileA_received_adoc));

        VirtualFile newApprovedFile = myFixture.findFileInTempDir(FILE_NAMES.folder1_fileA_approved_adoc);
        final String received_content = fileHelper.getFileContent(FILE_NAMES.folder1_fileA_received_adoc);
        assertEquals(received_content, new String(newApprovedFile.contentsToByteArray()));
    }

    @Test
    public void test_approved_one_directory() throws IOException {
        final Map<String, PsiFile> files = fileHelper.initFiles(FILE_NAMES.folder1_fileA_received_adoc, FILE_NAMES.folder1_fileB_received_adoc);

        final Map<String, PsiDirectory> folders = fileHelper.initFolders(FOLDER_NAMES.folder1);

        actionEvent.performAction(
                new ApproveFileAction(),
                folders.get(FOLDER_NAMES.folder1)
        );

        assertNotExists(FILE_NAMES.folder1_fileA_received_adoc);
        assertNotExists(FILE_NAMES.folder1_fileB_received_adoc);
        assertExists(FILE_NAMES.folder1_fileA_approved_adoc);
        assertExists(FILE_NAMES.folder1_fileB_approved_adoc);
    }

    @Test
    public void test_approved_multiple_files() throws IOException {
        final Map<String, PsiFile> files = fileHelper.initFiles(FILE_NAMES.folder1_fileA_received_adoc, FILE_NAMES.folder1_fileB_received_adoc, FILE_NAMES.folder1_fileC_received_adoc);

        actionEvent.performAction(new ApproveFileAction(), Arrays.asList(
                files.get(FILE_NAMES.folder1_fileA_received_adoc),
                files.get(FILE_NAMES.folder1_fileC_received_adoc)
        ));

        assertNotExists(FILE_NAMES.folder1_fileA_received_adoc);
        assertExists(FILE_NAMES.folder1_fileB_received_adoc);
        assertNotExists(FILE_NAMES.folder1_fileC_received_adoc);
        assertExists(FILE_NAMES.folder1_fileA_approved_adoc);
        assertNotExists(FILE_NAMES.folder1_fileB_approved_adoc);
        assertExists(FILE_NAMES.folder1_fileC_approved_adoc);
    }

    ///////////////////////////////////////::
    // undo

    @Test
    public void test_cancelled_approved_files() throws IOException {
        final Map<String, PsiFile> files = fileHelper.initFiles(
                FILE_NAMES.folder1_fileA_received_adoc,
                FILE_NAMES.folder1_fileB_received_adoc);

        final Map<String, PsiDirectory> folders = fileHelper.initFolders(FOLDER_NAMES.folder1);

        actionEvent.performAction(new ApproveFileAction(),
                folders.get(FOLDER_NAMES.folder1)
        );

        assertNotExists(FILE_NAMES.folder1_fileA_received_adoc);
        assertNotExists(FILE_NAMES.folder1_fileB_received_adoc);

        UndoManager undoManager = UndoManager.getInstance(getProject());
        undoManager.undo(null);

        assertExists(FILE_NAMES.folder1_fileA_received_adoc);
        assertExists(FILE_NAMES.folder1_fileB_received_adoc);
    }

    ///////////////////////////////////////::
    // testAction


}
