import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

public class CompareFileActionTest extends BasePlatformTestCase {

    public void test_compare_file_when_no_received_file() throws IOException {
        final PsiFile file = myFixture.addFileToProject("tmp/_file.approved.adoc", "some text");

        AnActionEvent actionEvent = new MockActionOnFileEvent(file);

        new CompareFileAction().update(actionEvent);

        assertFalse(actionEvent.getPresentation().isEnabledAndVisible());
    }

    public void test_compare_file_when_no_approved_file() throws IOException {
        final PsiFile file = myFixture.addFileToProject("tmp/_file.received.adoc", "some text");

        AnActionEvent actionEvent = new MockActionOnFileEvent(file);

        new CompareFileAction().update(actionEvent);

        assertFalse(actionEvent.getPresentation().isEnabledAndVisible());
    }

    public void test_compare_file_when_approved_and_received_files() throws IOException {
        final PsiFile fileApproved = myFixture.addFileToProject("tmp/_file.approved.adoc", "some text");
        final PsiFile fileReceived = myFixture.addFileToProject("tmp/_file.received.adoc", "some text");

        AnActionEvent actionEvent = new MockActionOnFileEvent(fileApproved);

        new CompareFileAction().update(actionEvent);

        assertTrue(actionEvent.getPresentation().isEnabledAndVisible());
    }


    public void test_compare_file_when_no_virtual_file() throws IOException {
        final PsiFile fileApproved = myFixture.addFileToProject("tmp/_file.approved.adoc", "some text");
        final PsiFile fileReceived = myFixture.addFileToProject("tmp/_file.received.adoc", "some text");

        AnActionEvent actionEvent = new MockActionOnFileEvent(fileApproved.getProject());

        new CompareFileAction().update(actionEvent);

        assertFalse(actionEvent.getPresentation().isEnabledAndVisible());
    }
}
