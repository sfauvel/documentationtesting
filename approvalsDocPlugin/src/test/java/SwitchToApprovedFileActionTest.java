import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import docAsTest.DocAsTestAction;
import docAsTest.DocAsTestStartupActivity;
import docAsTest.action.SwitchToApprovedFileAction;
import docAsTest.action.SwitchToFileAction;
import docAsTest.action.SwitchToReceivedFileAction;
import docAsTest.approvalFile.ApprovalFile;
import org.jetbrains.annotations.NotNull;
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
import java.util.Map;
import java.util.Properties;

@RunWith(JUnit4.class)
public class SwitchToApprovedFileActionTest extends DocAsTestPlatformTestCase {

    private final SwitchToApprovedFileAction actionApprovedUnderTest = new SwitchToApprovedFileAction() {
        @Override
        protected String getProjectBasePath(Project project) {
            return "/";
        }
    };

    private final SwitchToReceivedFileAction actionReceivedUnderTest = new SwitchToReceivedFileAction() {
        @Override
        protected String getProjectBasePath(Project project) {
            return "/";
        }
    };

    public static class fileNames extends FieldAutoNaming {

        public String docs_fileA_received_adoc;
        public String docs_fileA_approved_adoc;
        public String docs_fileA_myMethod_received_adoc;
        public String docs_fileA_myMethod_approved_adoc;
        public String docs_fileA_InnerClass_received_adoc;
        public String docs_fileA_InnerClass_approved_adoc;
        public String docs_fileA_InnerClass_innerMethod_received_adoc;
        public String docs_fileA_InnerClass_innerMethod_approved_adoc;
        public String documents_org_demo_fileX_approved_adoc;
        public String documents_org_demo_fileX_received_adoc;
        public String folder1_fileB_received_adoc;

    }

    public static class folderNames extends FieldAutoNaming {
    }

    private static final String DOC_PATH = "src/docs";
    private MockActionOnFileEvent actionEvent;
    private Presentation presentation;
    final private fileNames FILE_NAMES = new fileNames();
    final private folderNames FOLDER_NAMES = new folderNames();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.setTestDataPath(myFixture.getProject().getBasePath());

        final Properties properties = new Properties();
        properties.setProperty("TEST_PATH", "src");
        // We are not able to put a file outside of /src for now so we put docs folder in src.
        properties.setProperty("DOC_PATH", DOC_PATH);
        DocAsTestStartupActivity.setProperties(properties);


        actionEvent = new MockActionOnFileEvent(myFixture);
        presentation = actionEvent.getPresentation();

        myFixture.getTempDirFixture().findOrCreateDir("org/demo");
        myFixture.getTempDirFixture().findOrCreateDir("test/java/org/demo");
        myFixture.getTempDirFixture().findOrCreateDir("test/docs/org/demo");

        myFixture.addFileToProject("test/docs/_AnotherFile.approved.adoc", "approved content");
        myFixture.addFileToProject("test/docs/_AnotherFile.received.adoc", "approved content");

    }

    @Test
    public void test_no_approved_menu_entry_when_not_on_java_file() throws IOException {
        no_menu_entry_when_not_on_java_file(this.actionApprovedUnderTest);
    }

    @Test
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

    @Test
    public void test_no_menu_entry_when_no_approved_file() throws IOException {
        no_menu_entry_when_no_approval_file(new SwitchToApprovedFileAction());
    }

    @Test
    public void test_no_menu_entry_when_no_received_file() throws IOException {
        no_menu_entry_when_no_approval_file(new SwitchToReceivedFileAction());
    }

    private void no_menu_entry_when_no_approval_file(SwitchToFileAction actionUnderTest) {
        final String selectedClassName = "MyClass";
        final PsiFile psiFile = fileHelper.addTestClassFile(selectedClassName, CaretOn.CLASS);

        actionEvent.performAction(actionApprovedUnderTest, psiFile);

        assertFalse(presentation.isEnabledAndVisible());
        assertNull(presentation.getText());
        assertEquals("MyClass.java", getFileNameInEditor());
    }

    @Test
    public void test_menu_entry_when_approved_file() throws IOException {
        final Map<String, PsiFile> files = fileHelper.initFiles(
                FILE_NAMES.docs_fileA_approved_adoc,
                FILE_NAMES.folder1_fileB_received_adoc
        );
        menu_entry_when_approval_file_exists("approved", actionApprovedUnderTest);
    }

    @Test
    public void test_menu_entry_when_received_file() throws IOException {
        final Map<String, PsiFile> files = fileHelper.initFiles(
                FILE_NAMES.docs_fileA_received_adoc
        );
        menu_entry_when_approval_file_exists("received", actionReceivedUnderTest);
    }

    private void menu_entry_when_approval_file_exists(String approvalType, SwitchToFileAction actionApprovedUnderTest) {
        final String selectedClassName = "FileA";
        final PsiFile psiFile = fileHelper.addTestClassFile(selectedClassName, CaretOn.CLASS);

        actionEvent.performUpdateOnEditor(actionApprovedUnderTest, myFixture, psiFile);

        assertTrue(presentation.isEnabledAndVisible());
        assertEquals("Switch to " + approvalType + " file", presentation.getText());
    }

    @Test
    public void test_menu_switch_when_approved_file() throws IOException {
        final Map<String, PsiFile> files = fileHelper.initFiles(
                FILE_NAMES.docs_fileA_approved_adoc,
                FILE_NAMES.folder1_fileB_received_adoc
        );
        menu_switch_when_approval_file_exists("approved", actionApprovedUnderTest);
    }

    @Test
    public void test_menu_switch_when_received_file() throws IOException {
        final Map<String, PsiFile> files = fileHelper.initFiles(
                FILE_NAMES.docs_fileA_received_adoc
        );
        menu_switch_when_approval_file_exists("received", actionReceivedUnderTest);
    }

    private void menu_switch_when_approval_file_exists(String approvalType, SwitchToFileAction actionApprovedUnderTest) {
        final String selectedClassName = "FileA";
        final PsiFile psiFile = fileHelper.addTestClassFile(selectedClassName, CaretOn.CLASS);

        assertEquals("FileA.java", getFileNameInEditor());

        actionEvent.performAction(actionApprovedUnderTest, psiFile);

        assertEquals(String.format("_%s.%s.adoc", selectedClassName, approvalType), getFileNameInEditor());
    }

    @Test
    public void test_menu_entry_when_approved_file_with_package() throws IOException {
        menu_entry_when_file_with_package(actionApprovedUnderTest, "approved");
    }

    @Test
    public void test_menu_entry_when_received_file_with_package() throws IOException {
        menu_entry_when_file_with_package(actionReceivedUnderTest, "received");
    }

    private void menu_entry_when_file_with_package(SwitchToFileAction actionUnderTest, String approvalType) {
        final PsiFile psiFile = myFixture.addFileToProject("docs/org/demo/_MyClass." + approvalType + ".adoc", approvalType + " content");
        System.out.println("SwitchToApprovedFileActionTest.menu_entry_when_file_with_package approved added: " + psiFile.getVirtualFile().getPath() + "/" + psiFile.getVirtualFile().getName());
        fileHelper.addTestClassFile(Paths.get("org", "demo"), "MyClass", CaretOn.CLASS);

        final Presentation presentation = myFixture.testAction(actionUnderTest);
        assertTrue(presentation.isVisible());
        assertEquals("Switch to " + approvalType + " file", presentation.getText());
        assertEquals("_MyClass." + approvalType + ".adoc", getFileNameInEditor());
    }

    @Test
    public void test_no_menu_entry_when_approved_file_not_on_the_same_folder() throws IOException {
        no_menu_entry_when_approval_file_not_on_the_same_folder("approved", this.actionApprovedUnderTest);
    }

    @Test
    public void test_no_menu_entry_when_received_file_not_on_the_same_folder() throws IOException {
        no_menu_entry_when_approval_file_not_on_the_same_folder("received", this.actionReceivedUnderTest);
    }

    private void no_menu_entry_when_approval_file_not_on_the_same_folder(String approvalType, SwitchToFileAction actionUnderTest) {
        myFixture.addFileToProject("test/docs/otherfolder/_MyClass." + approvalType + ".adoc", approvalType + " content");

        fileHelper.addTestClassFile("MyClass", CaretOn.CLASS);

        final Presentation presentation = myFixture.testAction(actionUnderTest);
        assertFalse(presentation.isVisible());
        assertNull(presentation.getText());
        assertEquals("MyClass.java", getFileNameInEditor());
    }

    @Test
    public void test_menu_entry_when_approved_file_on_method() throws IOException {
        final Map<String, PsiFile> files = fileHelper.initFiles(
                FILE_NAMES.docs_fileA_myMethod_approved_adoc
        );
        menu_entry_when_approval_file_on_method("approved", this.actionApprovedUnderTest);
    }

    @Test
    public void test_menu_entry_when_received_file_on_method() throws IOException {
        final Map<String, PsiFile> files = fileHelper.initFiles(
                FILE_NAMES.docs_fileA_myMethod_received_adoc
        );
        menu_entry_when_approval_file_on_method("received", this.actionReceivedUnderTest);
    }

    private void menu_entry_when_approval_file_on_method(String approvalType, SwitchToFileAction actionUnderTest) {
        final PsiFile classFile = fileHelper.addTestClassFile("FileA", CaretOn.METHOD);

        actionEvent.performUpdateOnEditor(actionUnderTest, myFixture, classFile);
        assertTrue(presentation.isVisible());
        ;
        assertEquals("Switch to " + approvalType + " file", presentation.getText());

        actionEvent.performActionOnEditor(actionUnderTest, myFixture, classFile);
        assertEquals("_FileA.myMethod." + approvalType + ".adoc", getFileNameInEditor());
    }

    @Test
    public void test_menu_entry_when_approved_file_on_package() throws IOException {
        final Map<String, PsiFile> files = fileHelper.initFiles(
                FILE_NAMES.docs_fileA_approved_adoc
        );
        menu_entry_when_approval_file_on_package("approved", this.actionApprovedUnderTest);
    }

    @Test
    public void test_menu_entry_when_received_file_on_package() throws IOException {
        final Map<String, PsiFile> files = fileHelper.initFiles(
                FILE_NAMES.docs_fileA_received_adoc
        );
        menu_entry_when_approval_file_on_package("received", this.actionReceivedUnderTest);
    }

    private void menu_entry_when_approval_file_on_package(String approvalType, SwitchToFileAction actionUnderTest) {
        final PsiFile classFile = fileHelper.addTestClassFile("FileA", CaretOn.IMPORT);

        actionEvent.performUpdate(actionUnderTest, classFile);

        assertTrue(presentation.isVisible());
        assertEquals("Switch to " + approvalType + " file", presentation.getText());

        actionEvent.performAction(actionUnderTest, classFile);
        assertEquals("_FileA." + approvalType + ".adoc", getFileNameInEditor());
    }


    @Test
    public void test_menu_entry_when_approved_file_on_inner_class() throws IOException {
        final Map<String, PsiFile> files = fileHelper.initFiles(
                FILE_NAMES.docs_fileA_InnerClass_approved_adoc
        );
        menu_entry_when_approval_file_on_inner_class("approved", this.actionApprovedUnderTest);
    }

    @Test
    public void test_menu_entry_when_received_file_on_inner_class() throws IOException {
        final Map<String, PsiFile> files = fileHelper.initFiles(
                FILE_NAMES.docs_fileA_InnerClass_received_adoc
        );
        menu_entry_when_approval_file_on_inner_class("received", this.actionReceivedUnderTest);
    }

    private void menu_entry_when_approval_file_on_inner_class(String approvalType, SwitchToFileAction actionUnderTest) {
        final PsiFile classFile = fileHelper.addTestClassFile("FileA", CaretOn.INNER_CLASS);

        actionEvent.performUpdateOnEditor(actionUnderTest, myFixture, classFile);
        assertTrue(presentation.isVisible());
        assertEquals("Switch to " + approvalType + " file", presentation.getText());

        actionEvent.performActionOnEditor(actionUnderTest, myFixture, classFile);
        assertEquals("_FileA.InnerClass." + approvalType + ".adoc", getFileNameInEditor());
    }

    @Test
    public void test_menu_entry_when_approved_file_on_inner_method() throws IOException {
        final Map<String, PsiFile> files = fileHelper.initFiles(
                FILE_NAMES.docs_fileA_InnerClass_innerMethod_approved_adoc
        );
        menu_entry_when_approval_file_on_inner_method("approved", this.actionApprovedUnderTest);
    }

    @Test
    public void test_menu_entry_when_received_file_on_inner_method() throws IOException {
        final Map<String, PsiFile> files = fileHelper.initFiles(
                FILE_NAMES.docs_fileA_InnerClass_innerMethod_received_adoc
        );
        menu_entry_when_approval_file_on_inner_method("received", this.actionReceivedUnderTest);
    }

    private void menu_entry_when_approval_file_on_inner_method(String approvalType, SwitchToFileAction actionUnderTest) {
        final PsiFile classFile = fileHelper.addTestClassFile("FileA", CaretOn.INNER_METHOD);

        actionEvent.performUpdateOnEditor(actionUnderTest, myFixture, classFile);

        assertTrue(presentation.isVisible());
        assertEquals("Switch to " + approvalType + " file", presentation.getText());

        actionEvent.performActionOnEditor(actionUnderTest, myFixture, classFile);
        assertEquals("_FileA.InnerClass.innerMethod." + approvalType + ".adoc", getFileNameInEditor());
    }

    @Test
    public void test_menu_when_not_on_editor() throws IOException {
        final Map<String, PsiFile> files = fileHelper.initFiles(
                FILE_NAMES.docs_fileA_approved_adoc
        );

        String approvalType = "approved";
        final String selectedClassName = "FileA";
        final PsiFile psiFile = fileHelper.addTestClassFile(selectedClassName, CaretOn.NONE);

        actionEvent.performUpdate(actionApprovedUnderTest, psiFile);

        final Presentation presentation = actionEvent.getPresentation();
        assertTrue(presentation.isVisible());
        assertEquals("Switch to " + approvalType + " file", presentation.getText());
    }

    @Test
    public void test_open_approved_file_on_editor_when_on_java_PsiElement() throws IOException {
        final Map<String, PsiFile> files = fileHelper.initFiles(
                FILE_NAMES.docs_fileA_approved_adoc
        );
        final String approvalType = "approved";
        final String selectedClassName = "FileA";
        final PsiFile psiFile = fileHelper.addTestClassFile(selectedClassName, CaretOn.NONE);

        actionEvent.performAction(actionApprovedUnderTest, psiFile);

        assertEquals("_FileA." + approvalType + ".adoc", getFileNameInEditor());
    }

    @Test
    public void test_open_approved_file_on_editor_when_on_java_PsiFile() throws IOException {
        final Map<String, PsiFile> files = fileHelper.initFiles(
                FILE_NAMES.docs_fileA_approved_adoc
        );
        final String approvalType = "approved";
        final String selectedClassName = "FileA";
        final PsiFile psiFile = fileHelper.addTestClassFile(selectedClassName, CaretOn.CLASS);

        actionEvent.performAction(actionApprovedUnderTest, psiFile);

        assertEquals("_FileA." + approvalType + ".adoc", getFileNameInEditor());
    }

    @Test
    public void test_menu_java_entry_when_approval_file() {

        final Map<String, PsiFile> files = fileHelper.initFiles(
                FILE_NAMES.docs_fileA_approved_adoc
        );
        final String approvalType = "approved";
        final String selectedClassName = "FileA";
        final PsiFile psiFile = fileHelper.addTestClassFile(selectedClassName, CaretOn.INNER_CLASS);

        actionEvent.performUpdate(actionApprovedUnderTest, psiFile);

        assertTrue(presentation.isVisible());
        assertEquals("Switch to " + approvalType + " file", presentation.getText());

        actionEvent.performAction(actionApprovedUnderTest, psiFile);
        assertEquals("_FileA." + approvalType + ".adoc", getFileNameInEditor());
    }


    @Test
    public void test_menu_entry_when_approved_file_with_package_custom_folder() throws IOException {
        final Map<String, PsiFile> files = fileHelper.initFiles(
                FILE_NAMES.documents_org_demo_fileX_approved_adoc
        );
        menu_entry_when_file_with_package_custom_folder(actionApprovedUnderTest, "approved");
    }

    @Test
    public void test_menu_entry_when_received_file_with_package_custom_folder() throws IOException {
        final Map<String, PsiFile> files = fileHelper.initFiles(
                FILE_NAMES.documents_org_demo_fileX_received_adoc
        );
        menu_entry_when_file_with_package_custom_folder(actionReceivedUnderTest, "received");
    }

    private void menu_entry_when_file_with_package_custom_folder(SwitchToFileAction actionUnderTest, String approvalType) {
        final PsiFile propertyFile = myFixture.addFileToProject(
                "docAsTest.properties",
                String.join("\n",
                        "TEST_PATH:src",
                        "DOC_PATH:src/documents"));

        final PsiFile psiFile = fileHelper.addTestClassFile(Paths.get("org", "demo"), "FileX", CaretOn.CLASS);

        DocAsTestStartupActivity.loadProperties(myFixture.getProject());

        actionEvent.performUpdate(actionUnderTest, psiFile);
        assertTrue(presentation.isVisible());
        assertEquals("Switch to " + approvalType + " file", presentation.getText());

        actionEvent.performAction(actionUnderTest, psiFile);
        assertEquals("_FileX." + approvalType + ".adoc", getFileNameInEditor());
    }


    private static class DocAsTestActionForTest extends DocAsTestAction {

        private Project project;
        public DocAsTestActionForTest(Project project) {
            this.project = project;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
        }

        @NotNull
        public Path getApprovedFilePathFromProjectRootPath(PsiJavaFile classFile, PsiElement elementAt) {
            final Path approvedFilePath = getApprovedFilePath(project, elementAt, ApprovalFile.Status.APPROVED, classFile);
            return Paths.get(project.getBasePath()).relativize(approvedFilePath);
        }
    }

    @Test
    public void test_getApprovedVirtualFile_from_method() {
        final DocAsTestActionForTest action = new DocAsTestActionForTest(myFixture.getProject());

        final PsiJavaFile classFile = (PsiJavaFile) fileHelper.addTestClassFile("MyClass", CaretOn.METHOD);
        final PsiElement currentElement = classFile.findElementAt(myFixture.getEditor().getCaretModel().getCurrentCaret().getOffset());

        assertElementType(currentElement, PsiIdentifier.class);
        assertEquals("myMethod", currentElement.getText());
        assertEquals(
                Paths.get(DOC_PATH, "_MyClass.myMethod.approved.adoc"),
                action.getApprovedFilePathFromProjectRootPath(classFile, currentElement)
        );

        final PsiElement methodElement = currentElement.getParent();
        assertElementType(methodElement, PsiMethod.class);
        assertEquals("myMethod", ((PsiMethod) methodElement).getName());
        assertEquals(
                Paths.get(DOC_PATH, "_MyClass.myMethod.approved.adoc"),
                action.getApprovedFilePathFromProjectRootPath(classFile, methodElement)
        );

        final PsiElement classElement = methodElement.getParent();
        assertElementType(classElement, PsiClass.class);
        assertEquals("MyClass", ((PsiClass) classElement).getName());
        assertEquals(
                Paths.get(DOC_PATH, "_MyClass.approved.adoc"),
                action.getApprovedFilePathFromProjectRootPath(classFile, classElement)
        );
    }

    @Test
    public void test_getApprovedVirtualFile_from_class() {
        final DocAsTestActionForTest action = new DocAsTestActionForTest(myFixture.getProject());

        final PsiJavaFile classFile = (PsiJavaFile) fileHelper.addTestClassFile("MyClass", CaretOn.CLASS);
        final PsiElement currentElement = classFile.findElementAt(myFixture.getEditor().getCaretModel().getCurrentCaret().getOffset());

        assertElementType(currentElement, PsiIdentifier.class);
        assertEquals("MyClass", currentElement.getText());
        assertEquals(
                Paths.get(DOC_PATH, "_MyClass.approved.adoc"),
                action.getApprovedFilePathFromProjectRootPath(classFile, currentElement)
        );

        final PsiElement classElement = currentElement.getParent();
        assertElementType(classElement, PsiClass.class);
        assertEquals("MyClass", ((PsiClass) classElement).getName());
        assertEquals(
                Paths.get(DOC_PATH, "_MyClass.approved.adoc"),
                action.getApprovedFilePathFromProjectRootPath(classFile, classElement)
        );
    }

    @Test
    public void test_getApprovedVirtualFile_from_import() {
        final DocAsTestActionForTest action = new DocAsTestActionForTest(myFixture.getProject());

        final PsiJavaFile classFile = (PsiJavaFile) fileHelper.addTestClassFile("MyClass", CaretOn.NONE);
        final PsiElement currentElement = classFile.findElementAt(myFixture.getEditor().getCaretModel().getCurrentCaret().getOffset());

        // Check we are not in a class
        assertNull(PsiTreeUtil.getParentOfType(currentElement, PsiClass.class, false));
        assertEquals(
                Paths.get(DOC_PATH, "_MyClass.approved.adoc"),
                action.getApprovedFilePathFromProjectRootPath(classFile, currentElement)
        );

    }

    private void assertElementType(PsiElement elementAt, Class<? extends PsiElement> expectedType) {
        assertTrue("Class is " + elementAt.getClass().getSimpleName(),
                expectedType.isAssignableFrom(elementAt.getClass()));
    }
}
