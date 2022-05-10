import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class SwitchToJavaFileActionCustomeFolderTest extends BasePlatformTestCase {

    private final SwitchToJavaFileAction actionJavaUnderTest = new SwitchToJavaFileAction() {
        @Override
        protected String getProjectBasePath(Project project) {
            return "/";
        }
    };

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        final VirtualFile docsDir = myFixture.getTempDirFixture().findOrCreateDir("test/docs");
    }

    public void test_open_java_file_on_editor_when_on_approved_PsiElement() throws IOException {
        final PsiFile propertyFile = myFixture.addFileToProject("test/src/docAsTest.properties", "TEST_PATH:src/test/custom_folder");

        final String approvalType = "approved";
        myFixture.addFileToProject("test/custom_folder/MyClass.java", generateCode(SwitchToApprovedFileActionTest.CaretOn.NONE));
        final PsiFile approvedFile = myFixture.configureByText("_MyClass." + approvalType + ".adoc", approvalType + " content");
        myFixture.moveFile(approvedFile.getName(), "test/docs");
        assertEquals("_MyClass." + approvalType + ".adoc", getFileNameInEditor());

        AnActionEvent actionEvent = new MockActionOnPsiElementEvent(approvedFile);
        actionJavaUnderTest.actionPerformed(actionEvent);

        assertEquals("MyClass.java", getFileNameInEditor());
    }


    @NotNull
    private String getFileNameInEditor() {
        return FileEditorManager.getInstance(myFixture.getProject()).getSelectedEditor().getFile().getName();
    }

    private void addTestClassFile(final String className, SwitchToApprovedFileActionTest.CaretOn caretOn) {
        final PsiFile psiFile = myFixture.configureByText("MyClass.java", generateCode(caretOn));
    }

    private void addTestClassFile(final Path packagePath, final String className, SwitchToApprovedFileActionTest.CaretOn caretOn) {
        final PsiFile psiFile = myFixture.configureByText("MyClass.java", generateCode(packagePath, caretOn));
        myFixture.moveFile(psiFile.getName(), packagePath.toString());
    }

    private String generateCode(Path packagePath, SwitchToApprovedFileActionTest.CaretOn caretOn) {
        return String.format("package %s;\n%s",
                packagePath.toString().replace(java.io.File.separatorChar, '.'),
                generateCode(caretOn));
    }


    private String generateCode(SwitchToApprovedFileActionTest.CaretOn caretOn) {
        return String.format("import %sorg.demo; class %sMyClass { public void %smy_method() {} class %sInnerClass{ public void %sinner_method() {} } }",
                SwitchToApprovedFileActionTest.CaretOn.IMPORT.equals(caretOn) ? "<caret>" : "",
                SwitchToApprovedFileActionTest.CaretOn.CLASS.equals(caretOn) ? "<caret>" : "",
                SwitchToApprovedFileActionTest.CaretOn.METHOD.equals(caretOn) ? "<caret>" : "",
                SwitchToApprovedFileActionTest.CaretOn.INNER_CLASS.equals(caretOn) ? "<caret>" : "",
                SwitchToApprovedFileActionTest.CaretOn.INNER_METHOD.equals(caretOn) ? "<caret>" : "");
    }

}
