import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import docAsTest.DocAsTestStartupActivity;
import docAsTest.action.SwitchToJavaFileAction;
import docAsTest.approvalFile.JavaFile;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import tools.DocAsTestPlatformTestCase;
import tools.FieldAutoNaming;
import tools.FileHelper.CaretOn;
import tools.MockActionOnFileEvent;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

@RunWith(JUnit4.class)
public class SwitchToJavaFileActionTest extends DocAsTestPlatformTestCase {
    public static class fileNames extends FieldAutoNaming {
        public String docs_fileA_received_adoc;
        public String docs_fileA_approved_adoc;
        public String docs_fileA_myMethod_approved_adoc;
        public String docs_fileA_InnerClass_innerMethod_approved_adoc;
        public String docs_fileB_received_adoc;
        public String docs_fileB_approved_adoc;
    }

    public static class folderNames extends FieldAutoNaming {
        public String folder1;
        public String folder2;
    }

    private MockActionOnFileEvent actionEvent;
    private Presentation presentation;
    final private fileNames FILE_NAMES = new fileNames();
    final private folderNames FOLDER_NAMES = new folderNames();

    private final SwitchToJavaFileAction actionJavaUnderTest = new SwitchToJavaFileAction() {

        @Override
        protected String getProjectBasePath(Project project) {
            return "/";
        }
    };

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        final Properties properties = new Properties();
        properties.setProperty("TEST_PATH", "src");
        // We are not able to put a file outside of /src for now so we put docs folder in src.
        properties.setProperty("DOC_PATH", "src/docs");
        DocAsTestStartupActivity.setProperties(properties);

        actionEvent = new MockActionOnFileEvent(myFixture);
        presentation = actionEvent.getPresentation();

        final VirtualFile javaDir = myFixture.getTempDirFixture().findOrCreateDir("test/java");
        final VirtualFile packageDir = myFixture.getTempDirFixture().findOrCreateDir("org/demo");
        final VirtualFile docsDir = myFixture.getTempDirFixture().findOrCreateDir("test/docs");
    }

    // //////////////////////////
    // Update

    @Test
    public void test_menu_present_from_approved_file_when_java_file_exists() throws IOException {
        check_menu_presence_from_approval_file_in_menu_or_editor_with_FileA_java_class(
                FILE_NAMES.docs_fileA_approved_adoc,
                true);
    }

    @Test
    public void test_menu_present_from_received_file_when_java_file_exists() throws IOException {
        check_menu_presence_from_approval_file_in_menu_or_editor_with_FileA_java_class(
                FILE_NAMES.docs_fileA_received_adoc,
                true);
    }

    @Test
    public void test_menu_not_present_from_approved_file_when_no_java_file_exists() throws IOException {
        check_menu_presence_from_approval_file_in_menu_or_editor_with_FileA_java_class(
                FILE_NAMES.docs_fileB_approved_adoc,
                false);
    }

    @Test
    public void test_menu_not_present_from_received_file_when_no_java_file_exists() throws IOException {
        check_menu_presence_from_approval_file_in_menu_or_editor_with_FileA_java_class(
                FILE_NAMES.docs_fileB_received_adoc,
                false);
    }

    public void check_menu_presence_from_approval_file_in_menu_or_editor_with_FileA_java_class(String selectedFile, boolean isMenuVisisble) throws IOException {
        final Map<String, PsiFile> files = fileHelper.initFiles(
                FILE_NAMES.docs_fileA_approved_adoc,
                FILE_NAMES.docs_fileA_received_adoc,
                FILE_NAMES.docs_fileB_approved_adoc,
                FILE_NAMES.docs_fileB_received_adoc
        );
        fileHelper.addTestClassFile("FileA", CaretOn.INNER_CLASS);

        // Contextual menu from menu
        actionEvent.performUpdate(actionJavaUnderTest, Arrays.asList(
                files.get(selectedFile)));

        assertEquals(isMenuVisisble, presentation.isEnabledAndVisible());
        if (isMenuVisisble) {
            assertEquals("Switch to java file", presentation.getText());
        }

        // Contextual menu from editor
        assertEquals("FileA.java", getFileNameInEditor());
        actionEvent.performUpdateOnEditor(actionJavaUnderTest, myFixture, files.get(selectedFile));
        assertEquals(files.get(selectedFile).getName(), getFileNameInEditor());

        assertEquals(isMenuVisisble, presentation.isEnabledAndVisible());
        if (isMenuVisisble) {
            assertEquals("Switch to java file", presentation.getText());
        }
    }

    // //////////////////////////
    // Action
    public void check_open_java_file_from_approval_file_in_menu_or_editor(String selectedFile) throws IOException {
        final Map<String, PsiFile> files = fileHelper.initFiles(
                FILE_NAMES.docs_fileA_approved_adoc,
                FILE_NAMES.docs_fileA_received_adoc,
                FILE_NAMES.docs_fileA_myMethod_approved_adoc,
                FILE_NAMES.docs_fileA_InnerClass_innerMethod_approved_adoc,
                FILE_NAMES.docs_fileB_approved_adoc,
                FILE_NAMES.docs_fileB_received_adoc
        );

        final PsiFile classFile = fileHelper.addTestClassFile("FileA", CaretOn.NONE);

        // From Menu
        myFixture.openFileInEditor(files.get(selectedFile).getVirtualFile());
        assertEquals(files.get(selectedFile).getName(), getFileNameInEditor());

        actionEvent.performAction(actionJavaUnderTest, files.get(selectedFile));

        assertEquals(classFile.getName(), getFileNameInEditor());

        // From Editor
        myFixture.openFileInEditor(files.get(selectedFile).getVirtualFile());
        assertEquals(files.get(selectedFile).getName(), getFileNameInEditor());

        actionEvent.performActionOnEditor(actionJavaUnderTest, myFixture, files.get(selectedFile));

        assertEquals(classFile.getName(), getFileNameInEditor());
    }

    public void check_open_java_file_from_approval_file_in_menu_or_editor(String selectedFile, String textUnderCursor) throws IOException {
        check_open_java_file_from_approval_file_in_menu_or_editor(selectedFile);

        final Editor selectedTextEditor = FileEditorManager.getInstance(myFixture.getProject()).getSelectedTextEditor();
        assertNotNull(selectedTextEditor);
        final Document document = selectedTextEditor.getDocument();
        final int offset = selectedTextEditor.getCaretModel().getCurrentCaret().getOffset();
        final String text = document.getText(TextRange.create(offset, document.getText().length()));
        assertTrue(text.trim().startsWith(textUnderCursor));
    }

    @Test
    public void test_open_java_file_from_approved_file_in_menu_or_editor() throws IOException {
        check_open_java_file_from_approval_file_in_menu_or_editor(FILE_NAMES.docs_fileA_approved_adoc);
    }

    @Test
    public void test_open_java_file_from_received_file_in_menu_or_editor() throws IOException {
        check_open_java_file_from_approval_file_in_menu_or_editor(FILE_NAMES.docs_fileA_received_adoc);
    }

    @Test
    public void test_open_method_on_java_file_from_approved_file_in_menu_or_editor() throws IOException {
        check_open_java_file_from_approval_file_in_menu_or_editor(FILE_NAMES.docs_fileA_myMethod_approved_adoc,"myMethod()");
    }

    @Test
    public void test_open_method_in_inner_class_in_java_file_from_approved_file_in_menu_or_editor() throws IOException {
        check_open_java_file_from_approval_file_in_menu_or_editor(FILE_NAMES.docs_fileA_InnerClass_innerMethod_approved_adoc,"innerMethod()");
    }

    @Test
    public void test_find_class_offset_from_approved_file_name() {
        final String beginOfCode = "class ";
        final String classCode = "MyClass { public void ";
        final String methodCode = "myMethod() {}";
        final String endOfCode = "}";

        JavaFile javaFile = new JavaFile("", "MyClass", null);

        final PsiJavaFile psiFile = (PsiJavaFile)myFixture.addFileToProject("test/java/MyClass.java",
                beginOfCode + classCode + methodCode + endOfCode);

        final SwitchToJavaFileAction.ReturnJavaFile returnJavaFile = new SwitchToJavaFileAction.ReturnJavaFile(psiFile, javaFile);
        final int offset = returnJavaFile.getOffset();
        assertEquals(beginOfCode.length(), offset);
    }

    @Test
    public void test_find_method_offset_from_approved_file_name() {
        final String beginOfCode = "class ";
        final String classCode = "MyClass { public void ";
        final String methodCode = "myMethod() {}";
        final String endOfCode = "}";

        JavaFile javaFile = new JavaFile("", "MyClass", "myMethod");

        final PsiJavaFile psiFile = (PsiJavaFile) myFixture.addFileToProject("test/java/MyClass.java",
                beginOfCode + classCode + methodCode + endOfCode);

        final SwitchToJavaFileAction.ReturnJavaFile javaFile1 = new SwitchToJavaFileAction.ReturnJavaFile(psiFile, javaFile);

        final int offset = javaFile1.getOffset();
        assertEquals((beginOfCode + classCode).length(), offset);
    }

    @Test
    public void test_find_inner_class_offset_from_approved_file_name() {
        final String beginOfCode = "class MyClass { class FirstInnerClass {} class ";
        final String innerClassCode = "SecondInnerClass {} ";
        final String endOfCode = "class ThirdInnerClass {} }";

        JavaFile javaFile = new JavaFile("", "MyClass.SecondInnerClass", null);

        final PsiJavaFile psiFile = (PsiJavaFile) myFixture.addFileToProject("test/java/MyClass.java", beginOfCode + innerClassCode + endOfCode);

        final SwitchToJavaFileAction.ReturnJavaFile javaFile1 = new SwitchToJavaFileAction.ReturnJavaFile((PsiJavaFile) psiFile, javaFile);

        final int offset = javaFile1.getOffset();
        assertEquals(beginOfCode.length(), offset);
    }

    @Test
    public void test_find_sub_inner_class_offset_from_approved_file_name() {
        final String beginOfCode = "class MyClass { class FirstInnerClass { class ";
        final String innerClassCode = "SecondInnerClass { ";
        final String endOfCode = "class ThirdInnerClass {} } } }";

        JavaFile javaFile = new JavaFile("", "MyClass.FirstInnerClass.SecondInnerClass", null);

        final PsiJavaFile psiFile = (PsiJavaFile) myFixture.addFileToProject("test/java/MyClass.java", beginOfCode + innerClassCode + endOfCode);

        final SwitchToJavaFileAction.ReturnJavaFile javaFile1 = new SwitchToJavaFileAction.ReturnJavaFile(psiFile, javaFile);

        final int offset = javaFile1.getOffset();
        assertEquals(beginOfCode.length(), offset);
    }

    @Test
    public void test_find_method_in_sub_inner_class_offset_from_approved_file_name() {
        final String beginOfCode = "class MyClass { class FirstInnerClass { class SecondInnerClass { void ";
        final String methodCode = "myMethod() {} ";
        final String endOfCode = "class ThirdInnerClass {} } } }";

        JavaFile javaFile = new JavaFile("", "MyClass.FirstInnerClass.SecondInnerClass", "myMethod");

        final PsiJavaFile psiFile = (PsiJavaFile) myFixture.addFileToProject("test/java/MyClass.java", beginOfCode + methodCode + endOfCode);

        final SwitchToJavaFileAction.ReturnJavaFile javaFile1 = new SwitchToJavaFileAction.ReturnJavaFile(psiFile, javaFile);

        final int offset = javaFile1.getOffset();
        assertEquals(beginOfCode.length(), offset);
    }

    // ////////////////////////////////::
    // Test on specific methods of Action
    @Test
    public void test_java_file_path_from_approved_file() throws IOException {
        final VirtualFile approvedFile = fileHelper.createFile("/myproject/src/docs/_MyClass.approved.adoc");
        Optional<Path> javaFilePath = actionJavaUnderTest.getJavaFilePath(Paths.get("/src/myproject"), approvedFile);

        assertEquals("/src/myproject/src/MyClass.java", javaFilePath.map(Path::toString).get());
    }

    @Test
    public void test_java_file_path_from_approved_file_with_subproject() throws IOException {
        final VirtualFile approvedFile = fileHelper.createFile("/myproject/subproject/src/docs/_MyClass.approved.adoc");
        Optional<Path> javaFilePath = actionJavaUnderTest.getJavaFilePath(Paths.get("/"), approvedFile);

        assertEquals("/src/myproject/subproject/src/MyClass.java", javaFilePath.map(Path::toString).get());
    }

    @Test
    public void test_java_file_path_from_approved_file_with_package() throws IOException {
        // Files are created under /src
        final VirtualFile approvedFile = fileHelper.createFile("docs/org/demo/_MyClass.approved.adoc");
        Optional<Path> javaFilePath = actionJavaUnderTest.getJavaFilePath(Paths.get("/"), approvedFile);

        assertEquals("/src/org/demo/MyClass.java", javaFilePath.map(Path::toString).get());
    }

    @Test
    public void test_java_file_path_from_approved_file_with_package_and_custom_path() throws IOException {
        final Properties properties = new Properties();
        properties.setProperty("TEST_PATH", "src/myproject/src/test/java");
        // We are not able to put a file outside of /src for now so we put docs folder in src.
        properties.setProperty("DOC_PATH", "src/myproject/src/test/docs");
        DocAsTestStartupActivity.setProperties(properties);

        final VirtualFile approvedFile = fileHelper.createFile("myproject/src/test/docs/org/demo/_MyClass.approved.adoc");
        Optional<Path> javaFilePath = actionJavaUnderTest.getJavaFilePath(Paths.get("/"), approvedFile);

        assertEquals("/src/myproject/src/test/java/org/demo/MyClass.java", javaFilePath.map(Path::toString).get());
    }

    @Test
    public void test_no_java_file_path_from_a_non_approved_file() throws IOException {
        final VirtualFile approvedFile = fileHelper.createFile("docs/org/demo/_MyClass.something.adoc");
        Optional<Path> javaFilePath = actionJavaUnderTest.getJavaFilePath(Paths.get("/"), approvedFile);

        assertEquals(false, javaFilePath.isPresent());
    }

}
