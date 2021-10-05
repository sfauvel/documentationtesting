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

public class SwitchToJavaFileActionTest extends BasePlatformTestCase {

    private final SwitchToJavaFileAction actionJavaUnderTest = new SwitchToJavaFileAction() {
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
    }

    public void test_no_menu_java_entry_when_not_an_approval_file() {
        String approvalType = "approved";
        addTestClassFile("MyClass", SwitchToApprovedFileActionTest.CaretOn.INNER_CLASS);
        myFixture.configureByText("MyClass.txt", approvalType + " content");

        final Presentation presentation = myFixture.testAction(actionJavaUnderTest);
        assertFalse(presentation.isVisible());
        assertNull(presentation.getText());
        assertEquals("MyClass.txt", getFileNameInEditor());
    }

    public void test_no_menu_java_entry_when_no_java_class() {
        String approvalType = "approved";
        addTestClassFile("MyClass", SwitchToApprovedFileActionTest.CaretOn.INNER_CLASS);
        myFixture.configureByText("_MyOtherClass." + approvalType + ".adoc", approvalType + " content");

        final Presentation presentation = myFixture.testAction(actionJavaUnderTest);
        assertFalse(presentation.isVisible());
        assertNull(presentation.getText());
        assertEquals("_MyOtherClass." + approvalType + ".adoc", getFileNameInEditor());
    }

    public void test_open_java_file_on_editor_when_on_approved_PsiElement() throws IOException {
        final String approvalType = "approved";
        final PsiFile javaFile = myFixture.addFileToProject("test/java/MyClass.java", generateCode(SwitchToApprovedFileActionTest.CaretOn.NONE));
        final PsiFile approvedFile = myFixture.configureByText("_MyClass." + approvalType + ".adoc", approvalType + " content");
        myFixture.moveFile(approvedFile.getName(), "test/docs");
        assertEquals("_MyClass." + approvalType + ".adoc", getFileNameInEditor());

        AnActionEvent actionEvent = new MockActionOnPsiElementEvent(approvedFile);
        actionJavaUnderTest.actionPerformed(actionEvent);

        assertEquals("MyClass.java", getFileNameInEditor());
    }

    public void test_java_file_path_from_approved_file() throws IOException {
        final VirtualFile approvedFile = createFile("/src/myproject/src/test/docs/_MyClass.approved.adoc");
        Optional<Path> javaFilePath = actionJavaUnderTest.getJavaFilePath(
                Paths.get("/src/myproject"),
                approvedFile);

        assertEquals("/src/myproject/src/test/java/MyClass.java", javaFilePath.map(Path::toString).get());
    }

    public void test_java_file_path_from_approved_file_with_subproject() throws IOException {
        final VirtualFile approvedFile = createFile("/src/myproject/subproject/src/test/docs/_MyClass.approved.adoc");
        Optional<Path> javaFilePath = actionJavaUnderTest.getJavaFilePath(
                Paths.get("/src/myproject"),
                approvedFile);

        assertEquals("/src/myproject/subproject/src/test/java/MyClass.java", javaFilePath.map(Path::toString).get());
    }

    public void test_java_file_path_from_approved_file_with_package() throws IOException {
        final VirtualFile approvedFile = createFile("/src/myproject/src/test/docs/org/demo/_MyClass.approved.adoc");
        Optional<Path> javaFilePath = actionJavaUnderTest.getJavaFilePath(
                Paths.get("/src/myproject"),
                approvedFile);

        assertEquals("/src/myproject/src/test/java/org/demo/MyClass.java", javaFilePath.map(Path::toString).get());
    }

    public void test_no_java_file_path_from_a_non_approved_file() throws IOException {
        final VirtualFile approvedFile = createFile("/src/myproject/src/test/docs/_MyClass.something.adoc");
        Optional<Path> javaFilePath = actionJavaUnderTest.getJavaFilePath(
                Paths.get("/src/myproject"),
                approvedFile);

        assertEquals(false, javaFilePath.isPresent());
    }

    private VirtualFile createFile(String path) {
        return myFixture.getTempDirFixture().createFile(path);
    }

    public void test_open_java_file_on_editor_on_method_when_on_approved_PsiElement() throws IOException {
        final String approvalType = "approved";
        final PsiFile javaFile = myFixture.addFileToProject("test/java/MyClass.java", generateCode(SwitchToApprovedFileActionTest.CaretOn.NONE));
        final PsiFile approvedFile = myFixture.configureByText("_MyClass.my_method." + approvalType + ".adoc", approvalType + " content");
        myFixture.moveFile(approvedFile.getName(), "test/docs");
        assertEquals("_MyClass.my_method." + approvalType + ".adoc", getFileNameInEditor());

        SwitchToJavaFileAction actionJavaUnderTest = new SwitchToJavaFileAction() {
            @Override
            protected String getProjectBasePath(Project project) {
                return "/";
            }

            @Override
            public String getSrcDocs() {
                return "src/test/docs";
            }

            @Override
            public String getSrcPath() {
                return "src/test/java";
            }
        };

        AnActionEvent actionEvent = new MockActionOnPsiElementEvent(approvedFile);
        actionJavaUnderTest.actionPerformed(actionEvent);

        assertEquals("MyClass.java", getFileNameInEditor());

        final Editor selectedTextEditor = FileEditorManager.getInstance(myFixture.getProject()).getSelectedTextEditor();
        assertNotNull(selectedTextEditor);
        final Document document = selectedTextEditor.getDocument();
        final int offset = selectedTextEditor.getCaretModel().getCurrentCaret().getOffset();
        final String text = document.getText(TextRange.create(offset, document.getText().length()));
        assertTrue(text.trim().startsWith("my_method()"));
    }

    public void test_open_java_file_on_editor_on_method_in_inner_class_when_on_approved_PsiElement() throws IOException {
        final String approvalType = "approved";
        final PsiFile javaFile = myFixture.addFileToProject("test/java/MyClass.java", generateCode(SwitchToApprovedFileActionTest.CaretOn.NONE));
        final PsiFile approvedFile = myFixture.configureByText("_MyClass.InnerClass.inner_method." + approvalType + ".adoc", approvalType + " content");
        myFixture.moveFile(approvedFile.getName(), "test/docs");
        assertEquals("_MyClass.InnerClass.inner_method." + approvalType + ".adoc", getFileNameInEditor());

        SwitchToJavaFileAction actionJavaUnderTest = new SwitchToJavaFileAction() {
            @Override
            protected String getProjectBasePath(Project project) {
                return "/";
            }

            @Override
            public String getSrcDocs() {
                return "src/test/docs";
            }

            @Override
            public String getSrcPath() {
                return "src/test/java";
            }
        };

        AnActionEvent actionEvent = new MockActionOnPsiElementEvent(approvedFile);
        actionJavaUnderTest.actionPerformed(actionEvent);

        assertEquals("MyClass.java", getFileNameInEditor());

        final Editor selectedTextEditor = FileEditorManager.getInstance(myFixture.getProject()).getSelectedTextEditor();
        assertNotNull(selectedTextEditor);
        final Document document = selectedTextEditor.getDocument();
        final int offset = selectedTextEditor.getCaretModel().getCurrentCaret().getOffset();
        final String text = document.getText(TextRange.create(offset, document.getText().length()));
        assertTrue(text.trim().startsWith("inner_method()"));
    }

    public void test_find_class_offset_from_approved_file_name() {
        final String beginOfCode = "class ";
        final String classCode = "MyClass { public void ";
        final String methodCode = "my_method() {}";
        final String endOfCode = "}";

        JavaFile javaFile = new JavaFile("", "MyClass", null);

        final PsiFile psiFile = myFixture.addFileToProject("test/java/MyClass.java",
                beginOfCode + classCode + methodCode + endOfCode);

        final SwitchToJavaFileAction.ApprovedRunnable approvedRunnable = new SwitchToJavaFileAction.ApprovedRunnable(
                myFixture.getProject(),
                new SwitchToJavaFileAction.ReturnJavaFile((PsiJavaFile) psiFile, javaFile));

        final int offset = approvedRunnable.getOffset();
        assertEquals(beginOfCode.length(), offset);
    }

    public void test_find_method_offset_from_approved_file_name() {
        final String beginOfCode = "class ";
        final String classCode = "MyClass { public void ";
        final String methodCode = "my_method() {}";
        final String endOfCode = "}";

        JavaFile javaFile = new JavaFile("", "MyClass", "my_method");

        final PsiFile psiFile = myFixture.addFileToProject("test/java/MyClass.java",
                beginOfCode + classCode + methodCode + endOfCode);

        final SwitchToJavaFileAction.ApprovedRunnable approvedRunnable = new SwitchToJavaFileAction.ApprovedRunnable(
                myFixture.getProject(),
                new SwitchToJavaFileAction.ReturnJavaFile((PsiJavaFile) psiFile, javaFile));

        final int offset = approvedRunnable.getOffset();
        assertEquals((beginOfCode + classCode).length(), offset);
    }

    public void test_find_inner_class_offset_from_approved_file_name() {
        final String beginOfCode = "class MyClass { class FirstInnerClass {} class ";
        final String innerClassCode = "SecondInnerClass {} ";
        final String endOfCode = "class ThirdInnerClass {} }";

        JavaFile javaFile = new JavaFile("", "MyClass.SecondInnerClass", null);

        final PsiFile psiFile = myFixture.addFileToProject("test/java/MyClass.java", beginOfCode + innerClassCode + endOfCode);

        final SwitchToJavaFileAction.ApprovedRunnable approvedRunnable = new SwitchToJavaFileAction.ApprovedRunnable(
                myFixture.getProject(),
                new SwitchToJavaFileAction.ReturnJavaFile((PsiJavaFile) psiFile, javaFile));

        final int offset = approvedRunnable.getOffset();
        assertEquals(beginOfCode.length(), offset);
    }

    public void test_find_sub_inner_class_offset_from_approved_file_name() {
        final String beginOfCode = "class MyClass { class FirstInnerClass { class ";
        final String innerClassCode = "SecondInnerClass { ";
        final String endOfCode = "class ThirdInnerClass {} } } }";

        JavaFile javaFile = new JavaFile("", "MyClass.FirstInnerClass.SecondInnerClass", null);

        final PsiFile psiFile = myFixture.addFileToProject("test/java/MyClass.java", beginOfCode + innerClassCode + endOfCode);

        final SwitchToJavaFileAction.ApprovedRunnable approvedRunnable = new SwitchToJavaFileAction.ApprovedRunnable(
                myFixture.getProject(),
                new SwitchToJavaFileAction.ReturnJavaFile((PsiJavaFile) psiFile, javaFile));

        final int offset = approvedRunnable.getOffset();
        assertEquals(beginOfCode.length(), offset);
    }

    public void test_find_method_in_sub_inner_class_offset_from_approved_file_name() {
        final String beginOfCode = "class MyClass { class FirstInnerClass { class SecondInnerClass { void ";
        final String methodCode = "my_method() {} ";
        final String endOfCode = "class ThirdInnerClass {} } } }";

        JavaFile javaFile = new JavaFile("", "MyClass.FirstInnerClass.SecondInnerClass", "my_method");

        final PsiFile psiFile = myFixture.addFileToProject("test/java/MyClass.java", beginOfCode + methodCode + endOfCode);

        final SwitchToJavaFileAction.ApprovedRunnable approvedRunnable = new SwitchToJavaFileAction.ApprovedRunnable(
                myFixture.getProject(),
                new SwitchToJavaFileAction.ReturnJavaFile((PsiJavaFile) psiFile, javaFile));

        final int offset = approvedRunnable.getOffset();
        assertEquals(beginOfCode.length(), offset);
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
