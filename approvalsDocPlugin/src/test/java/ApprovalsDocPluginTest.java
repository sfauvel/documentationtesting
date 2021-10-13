import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.UndoConfirmationPolicy;
import com.intellij.openapi.command.undo.UndoManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class ApprovalsDocPluginTest extends BasePlatformTestCase {

    public void testMenuForOneFile() throws IOException {
        final PsiFile file = myFixture.addFileToProject("tmp/_file.received.adoc", "some text");

        AnActionEvent actionEvent = new MockActionOnFileEvent(file);

        new ApproveFileAction().update(actionEvent);

        assertEquals("Approved file", actionEvent.getPresentation().getText());
    }

    public void testMenuForOneFolder() throws IOException {
        myFixture.addFileToProject("tmp/_file.received.adoc", "some text");
        final VirtualFile folder = myFixture.findFileInTempDir("tmp");

        AnActionEvent actionEvent = new MockActionOnFileEvent(myFixture.getProject(), folder);

        new ApproveFileAction().update(actionEvent);

        assertEquals("Approved All", actionEvent.getPresentation().getText());
    }

    public void testName() throws IOException {

        myFixture.addFileToProject("tmp/_file.received.adoc", "some text");
        VirtualFile virtualFile = myFixture.findFileInTempDir("tmp/_file.received.adoc");
        System.out.println(virtualFile.getUrl());
        System.out.println(virtualFile.getPath());
        PsiFile file1 = myFixture.getPsiManager().findFile(virtualFile);
        assertEquals("some text", file1.getText());

        {
            String text = new BufferedReader(new InputStreamReader(virtualFile.getInputStream()))
                    .lines().collect(Collectors.joining("\n"));

            assertEquals("some text", text);
        }

    }

    public void test_approved_one_received_file() throws IOException {
        String receivedFile = "tmp/_file.multi.name.received.adoc";
        String approvedFile = "tmp/_file.multi.name.approved.adoc";

        myFixture.addFileToProject(receivedFile, "some text");

        VirtualFile virtualFile = myFixture.findFileInTempDir(receivedFile);

        performAction(new ApproveFileAction.ApprovedRunnable(myFixture.getProject(), virtualFile));

        assertNotExists(receivedFile);
        assertExists(approvedFile);
    }

    public void test_approved_file_to_replace_a_previous_approbation() throws IOException {
        String receivedFile = "tmp/_file.received.adoc";
        String approvedFile = "tmp/_file.approved.adoc";

        myFixture.addFileToProject(receivedFile, "new text");
        myFixture.addFileToProject(approvedFile, "old text");

        VirtualFile virtualFile = myFixture.findFileInTempDir(receivedFile);

        performAction(new ApproveFileAction.ApprovedRunnable(myFixture.getProject(), virtualFile));

        VirtualFile newApprovedFile = myFixture.findFileInTempDir(approvedFile);
        assertEquals("new text", new String(newApprovedFile.contentsToByteArray()));
    }

    public void test_approved_one_directory() throws IOException {
        String receivedFileA = "tmp/_fileA.received.adoc";
        String receivedFileB = "tmp/_fileB.received.adoc";
        String approvedFileA = "tmp/_fileA.approved.adoc";
        String approvedFileB = "tmp/_fileB.approved.adoc";

        myFixture.addFileToProject(receivedFileA, "some text for A");
        myFixture.addFileToProject(receivedFileB, "some text for B");

        VirtualFile virtualFile = myFixture.findFileInTempDir("tmp");
        performAction(new ApproveFileAction.ApprovedRunnable(myFixture.getProject(), virtualFile));

        assertNotExists(receivedFileA);
        assertNotExists(receivedFileB);
        assertExists(approvedFileA);
        assertExists(approvedFileB);
    }

    public void test_cancelled_approved_files() throws IOException {
        String receivedFileA = "tmp/_fileA.received.adoc";
        String receivedFileB = "tmp/_fileB.received.adoc";

        myFixture.addFileToProject(receivedFileA, "some text for A");
        myFixture.addFileToProject(receivedFileB, "some text for B");

        VirtualFile virtualFile = myFixture.findFileInTempDir("tmp");
        performAction(new ApproveFileAction.ApprovedRunnable(myFixture.getProject(), virtualFile));

        assertNotExists(receivedFileA);
        assertNotExists(receivedFileB);

        UndoManager undoManager = UndoManager.getInstance(getProject());
        undoManager.undo(null);

        assertExists(receivedFileA);
        assertExists(receivedFileB);
    }

    private void assertExists(String filename) {
        assertExists(myFixture.findFileInTempDir(filename));
    }

    private void assertExists(VirtualFile virtualFile) {
        assertNotNull(virtualFile);
    }

    private void assertNotExists(String receivedFile) {
        assertNotExists(myFixture.findFileInTempDir(receivedFile));
    }

    private void assertNotExists(VirtualFile virtualFile) {
        assertNull(virtualFile);
    }

    private void performAction(Runnable action) {
        performWriteAction(myFixture.getProject(), action);
    }

    private void performWriteAction(final Project project, final Runnable action) {
        ApplicationManager.getApplication().runWriteAction(
                () -> CommandProcessor.getInstance().executeCommand(
                        project,
                        action,
                        "test command",
                        null,
                        UndoConfirmationPolicy.DO_NOT_REQUEST_CONFIRMATION));
    }

}
