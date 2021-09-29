import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.UndoConfirmationPolicy;
import com.intellij.openapi.command.undo.UndoManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class ApprovalsDocPluginTest extends BasePlatformTestCase {

    private DataContext dataContext;
    private Presentation presentation;
    private ActionManager actionManager;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dataContext = new DataContext() {
            @Override
            public @Nullable Object getData(@NotNull String dataId) {
                return null;
            }
        };
        String place = "Here";
        presentation = new Presentation();

        actionManager = new MockActionManager();

    }

    private class MockActionEvent extends AnActionEvent {
        public MockActionEvent() {
            super(null, ApprovalsDocPluginTest.this.dataContext, "Here", ApprovalsDocPluginTest.this.presentation, ApprovalsDocPluginTest.this.actionManager, 0);
        }

    }

    public void testMenuForOneFile() throws IOException {
        myFixture.addFileToProject("tmp/file.received.adoc", "some text");
        final VirtualFile virtualFile = myFixture.findFileInTempDir("tmp/file.received.adoc");

        AnActionEvent actionEvent = new MockActionEvent() {
            @Override
            public <T> @Nullable T getData(@NotNull DataKey<T> key) {
                return key == PlatformDataKeys.VIRTUAL_FILE
                        ? (T) virtualFile
                        : null;
            }
        };

        new ApproveFileAction().update(actionEvent);

        assertEquals("Approved file", presentation.getText());
    }


    public void testMenuForOneFolder() throws IOException {
        myFixture.addFileToProject("tmp/file.received.adoc", "some text");
        final VirtualFile virtualFile = myFixture.findFileInTempDir("tmp");

        AnActionEvent actionEvent = new MockActionEvent() {
            @Override
            public <T> @Nullable T getData(@NotNull DataKey<T> key) {
                return key == PlatformDataKeys.VIRTUAL_FILE
                        ? (T) virtualFile
                        : null;
            }
        };
        new ApproveFileAction().update(actionEvent);

        assertEquals("Approved All", presentation.getText());
    }

    public void testName() throws IOException {

        myFixture.addFileToProject("tmp/file.received.adoc", "some text");
        VirtualFile virtualFile = myFixture.findFileInTempDir("tmp/file.received.adoc");
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
        String receivedFile = "tmp/file.multi.name.received.adoc";
        String approvedFile = "tmp/file.multi.name.approved.adoc";

        myFixture.addFileToProject(receivedFile, "some text");

        VirtualFile virtualFile = myFixture.findFileInTempDir(receivedFile);

        performAction(new ApproveFileAction.ApprovedRunnable(myFixture.getProject(), virtualFile));

        assertNotExists(receivedFile);
        assertExists(approvedFile);
    }

    public void test_approved_file_to_replace_a_previous_approbation() throws IOException {
        String receivedFile = "tmp/file.received.adoc";
        String approvedFile = "tmp/file.approved.adoc";

        myFixture.addFileToProject(receivedFile, "new text");
        myFixture.addFileToProject(approvedFile, "old text");

        VirtualFile virtualFile = myFixture.findFileInTempDir(receivedFile);

        performAction(new ApproveFileAction.ApprovedRunnable(myFixture.getProject(), virtualFile));

        VirtualFile newApprovedFile = myFixture.findFileInTempDir(approvedFile);
        assertEquals("new text", new String(newApprovedFile.contentsToByteArray()));
    }

    public void test_approved_one_directory() throws IOException {
        String receivedFileA = "tmp/fileA.received.adoc";
        String receivedFileB = "tmp/fileB.received.adoc";
        String approvedFileA = "tmp/fileA.approved.adoc";
        String approvedFileB = "tmp/fileB.approved.adoc";

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
        String receivedFileA = "tmp/fileA.received.adoc";
        String receivedFileB = "tmp/fileB.received.adoc";

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
