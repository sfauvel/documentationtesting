import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SwitchToApprovedFileActionTest extends BasePlatformTestCase {

    private final SwitchToApprovedFileAction actionApprovedUnderTest = new SwitchToApprovedFileAction()  {
        @Override
        protected String getProjectBasePath(Project project) {
            return "/";
        }
    };

    private final SwitchToReceivedFileAction actionReceivedUnderTest = new SwitchToReceivedFileAction()  {
        @Override
        protected String getProjectBasePath(Project project) {
            return "/";
        }
    };


    @Override
    protected void setUp() throws Exception {
        super.setUp();

        final VirtualFile javaDir = myFixture.getTempDirFixture().findOrCreateDir("test/java");
        final VirtualFile packageDir = myFixture.getTempDirFixture().findOrCreateDir("org/demo");
        final VirtualFile docsDir = myFixture.getTempDirFixture().findOrCreateDir("test/docs");

        myFixture.addFileToProject("test/docs/_AnotherFile.approved.adoc", "approved content");
        myFixture.addFileToProject("test/docs/_AnotherFile.received.adoc", "approved content");
    }

    public void test_no_approved_menu_entry_when_not_on_java_file() throws IOException {
        no_menu_entry_when_not_on_java_file(this.actionApprovedUnderTest);
    }

    public void test_no_received_menu_entry_when_not_on_java_file() throws IOException {
        no_menu_entry_when_not_on_java_file(this.actionReceivedUnderTest);
    }

    private void no_menu_entry_when_not_on_java_file(SwitchToFileAction actionUnderTest) {
        final PsiFile psiFile = myFixture.configureByText("MyClass.txt", "Text file content");

        final Presentation presentation = myFixture.testAction(actionUnderTest);
        assertFalse(presentation.isVisible());
        assertNull(presentation.getText());
        assertEquals("MyClass.txt", getFileNameInEditor());
    }

    public void test_no_menu_entry_when_no_approved_file() throws IOException {
        no_menu_entry_when_no_approval_file(this.actionApprovedUnderTest);
    }

    public void test_no_menu_entry_when_no_received_file() throws IOException {
        no_menu_entry_when_no_approval_file(this.actionReceivedUnderTest);
    }

    private void no_menu_entry_when_no_approval_file(SwitchToFileAction actionUnderTest) {
        addTestClassFile("MyClass", CaretOn.CLASS);

        final Presentation presentation = myFixture.testAction(actionUnderTest);
        assertFalse(presentation.isVisible());
        assertNull(presentation.getText());
        assertEquals("MyClass.java", getFileNameInEditor());
    }

    public void test_menu_entry_when_approved_file() throws IOException {
        menu_entry_when_approval_file_exists("approved", actionApprovedUnderTest);
    }

    public void test_menu_entry_when_received_file() throws IOException {
        menu_entry_when_approval_file_exists("received", actionReceivedUnderTest);
    }

    private void menu_entry_when_approval_file_exists(String approvalType, SwitchToFileAction actionApprovedUnderTest) {
        myFixture.addFileToProject("test/docs/_MyClass." + approvalType + ".adoc", approvalType + " content");
        addTestClassFile("MyClass", CaretOn.CLASS);

        final Presentation presentation = myFixture.testAction(actionApprovedUnderTest);
        assertTrue(presentation.isVisible());
        assertEquals("Switch to " + approvalType + " file", presentation.getText());
        assertEquals("_MyClass." + approvalType + ".adoc", getFileNameInEditor());
    }

    public void test_menu_entry_when_approved_file_with_package() throws IOException {
        menu_entry_when_file_with_package(actionApprovedUnderTest, "approved");
    }

    public void test_menu_entry_when_received_file_with_package() throws IOException {
        menu_entry_when_file_with_package(actionReceivedUnderTest, "received");
    }

    private void menu_entry_when_file_with_package(SwitchToFileAction actionUnderTest, String approvalType) {
        myFixture.getTempDirPath();
        myFixture.addFileToProject("test/docs/org/demo/_MyClass." + approvalType + ".adoc", approvalType + " content");
        addTestClassFile(Paths.get("org", "demo"), "MyClass", CaretOn.CLASS);

        final Presentation presentation = myFixture.testAction(actionUnderTest);
        assertTrue(presentation.isVisible());
        assertEquals("Switch to " + approvalType + " file", presentation.getText());
        assertEquals("_MyClass." + approvalType + ".adoc", getFileNameInEditor());
    }

    public void test_no_menu_entry_when_approved_file_not_on_the_same_folder() throws IOException {
        no_menu_entry_when_approval_file_not_on_the_same_folder("approved", this.actionApprovedUnderTest);
    }

    public void test_no_menu_entry_when_received_file_not_on_the_same_folder() throws IOException {
        no_menu_entry_when_approval_file_not_on_the_same_folder("received", this.actionReceivedUnderTest);
    }

    private void no_menu_entry_when_approval_file_not_on_the_same_folder(String approvalType, SwitchToFileAction actionUnderTest) {
        myFixture.addFileToProject("test/docs/otherfolder/_MyClass." + approvalType + ".adoc", approvalType + " content");
        addTestClassFile("MyClass", CaretOn.CLASS);

        final Presentation presentation = myFixture.testAction(actionUnderTest);
        assertFalse(presentation.isVisible());
        assertNull(presentation.getText());
        assertEquals("MyClass.java", getFileNameInEditor());
    }

    public void test_menu_entry_when_approved_file_on_method() throws IOException {
        menu_entry_when_approval_file_on_method("approved", this.actionApprovedUnderTest);
    }

    public void test_menu_entry_when_received_file_on_method() throws IOException {
        menu_entry_when_approval_file_on_method("received", this.actionReceivedUnderTest);
    }

    private void menu_entry_when_approval_file_on_method(String approvalType, SwitchToFileAction actionUnderTest) {
        myFixture.addFileToProject("test/docs/_MyClass.my_method." + approvalType + ".adoc", approvalType + " content");
        addTestClassFile("MyClass", CaretOn.METHOD);

        final Presentation presentation = myFixture.testAction(actionUnderTest);
        assertTrue(presentation.isVisible());
        assertEquals("Switch to " + approvalType + " file", presentation.getText());
        assertEquals("_MyClass.my_method." + approvalType + ".adoc", getFileNameInEditor());
    }

    public void test_menu_entry_when_approved_file_on_package() throws IOException {
        menu_entry_when_approval_file_on_package("approved", this.actionApprovedUnderTest);
    }

    public void test_menu_entry_when_received_file_on_package() throws IOException {
        menu_entry_when_approval_file_on_package("received", this.actionReceivedUnderTest);
    }

    private void menu_entry_when_approval_file_on_package(String approvalType, SwitchToFileAction actionUnderTest) {
        myFixture.addFileToProject("test/docs/_MyClass." + approvalType + ".adoc", approvalType + " content");
        addTestClassFile("MyClass", CaretOn.IMPORT);

        final Presentation presentation = myFixture.testAction(actionUnderTest);
        assertTrue(presentation.isVisible());
        assertEquals("Switch to " + approvalType + " file", presentation.getText());
        assertEquals("_MyClass." + approvalType + ".adoc", getFileNameInEditor());
    }


    public void test_menu_entry_when_approved_file_on_inner_class() throws IOException {
        menu_entry_when_approval_file_on_inner_class("approved", this.actionApprovedUnderTest);
    }

    public void test_menu_entry_when_received_file_on_inner_class() throws IOException {
        menu_entry_when_approval_file_on_inner_class("received", this.actionReceivedUnderTest);
    }

    private void menu_entry_when_approval_file_on_inner_class(String approvalType, SwitchToFileAction actionUnderTest) {
        myFixture.addFileToProject("test/docs/_MyClass.InnerClass." + approvalType + ".adoc", approvalType + " content");
        addTestClassFile("MyClass", CaretOn.INNER_CLASS);

        final Presentation presentation = myFixture.testAction(actionUnderTest);
        assertTrue(presentation.isVisible());
        assertEquals("Switch to " + approvalType + " file", presentation.getText());
        assertEquals("_MyClass.InnerClass." + approvalType + ".adoc", getFileNameInEditor());
    }

    public void test_menu_entry_when_approved_file_on_inner_method() throws IOException {
        menu_entry_when_approval_file_on_inner_method("approved", this.actionApprovedUnderTest);
    }

    public void test_menu_entry_when_received_file_on_inner_method() throws IOException {
        menu_entry_when_approval_file_on_inner_method("received", this.actionReceivedUnderTest);
    }

    private void menu_entry_when_approval_file_on_inner_method(String approvalType, SwitchToFileAction actionUnderTest) {
        myFixture.addFileToProject("test/docs/_MyClass.InnerClass.inner_method." + approvalType + ".adoc", approvalType + " content");
        addTestClassFile("MyClass", CaretOn.INNER_METHOD);

        final Presentation presentation = myFixture.testAction(actionUnderTest);
        assertTrue(presentation.isVisible());
        assertEquals("Switch to " + approvalType + " file", presentation.getText());
        assertEquals("_MyClass.InnerClass.inner_method." + approvalType + ".adoc", getFileNameInEditor());
    }

    public void test_menu_when_not_on_editor() throws IOException {
        final String approvalType = "approved";
        myFixture.addFileToProject("test/docs/_MyClass." + approvalType + ".adoc", approvalType + " content");
        final PsiFile psiFile = myFixture.addFileToProject("test/java/MyClass.java", generateCode(CaretOn.NONE));

        AnActionEvent actionEvent = new MockActionOnFileEvent(psiFile);

        new SwitchToApprovedFileAction() {
            @Override
            protected String getProjectBasePath(Project project) {
                return "/";
            }
        }.update(actionEvent);

        final Presentation presentation = actionEvent.getPresentation();
        assertTrue(presentation.isVisible());
        assertEquals("Switch to " + approvalType + " file", presentation.getText());
    }

    static enum CaretOn {
        NONE,
        IMPORT,
        CLASS,
        METHOD,
        INNER_CLASS,
        INNER_METHOD;
    }

    @NotNull
    private String getFileNameInEditor() {
        return FileEditorManager.getInstance(myFixture.getProject()).getSelectedEditor().getFile().getName();
    }

    private void addTestClassFile(final String className, CaretOn caretOn) {
        final PsiFile psiFile = myFixture.configureByText("MyClass.java", generateCode(caretOn));
    }

    private void addTestClassFile(final Path packagePath, final String className, CaretOn caretOn) {
        final PsiFile psiFile = myFixture.configureByText("MyClass.java", generateCode(packagePath, caretOn));
        myFixture.moveFile(psiFile.getName(), packagePath.toString());
    }

    private String generateCode(Path packagePath, CaretOn caretOn) {
        return String.format("package %s;\n%s",
                packagePath.toString().replace(java.io.File.separatorChar, '.'),
                generateCode(caretOn));
    }


    private String generateCode(CaretOn caretOn) {
        return String.format("import %sorg.demo; class %sMyClass { public void %smy_method() {} class %sInnerClass{ public void %sinner_method() {} } }",
                CaretOn.IMPORT.equals(caretOn)?"<caret>":"",
                CaretOn.CLASS.equals(caretOn)?"<caret>":"",
                CaretOn.METHOD.equals(caretOn)?"<caret>":"",
                CaretOn.INNER_CLASS.equals(caretOn)?"<caret>":"",
                CaretOn.INNER_METHOD.equals(caretOn)?"<caret>":"");
    }

}
