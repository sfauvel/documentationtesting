import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import docAsTest.DocAsTestFilenameIndex;
import docAsTest.DocAsTestStartupActivity;
import tools.DocAsTestPlatformTest;
import tools.FieldAutoNaming;
import tools.FileHelper.CaretOn;
import tools.MockActionOnFileEvent;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

public class SwitchToApprovedFileActionCustomFolderTest extends DocAsTestPlatformTest {

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
        public String documents_org_demo_fileA_received_adoc;
        public String documents_org_demo_fileA_approved_adoc;
        public String docs_fileA_myMethod_received_adoc;
    }

    private String CUSTOM_DOC_FOLDER="documents";
    private MockActionOnFileEvent actionEvent;
    private Presentation presentation;
    final private fileNames FILE_NAMES = new fileNames();

    @Override
    protected void setUp() throws Exception {
        DocAsTestFilenameIndex.setSlowOperationPolicy(DocAsTestPlatformTest.NO_SLOW_OPERATION_POLICY);
        super.setUp();
        DocAsTestStartupActivity.reset();
        new DocAsTestStartupActivity().runActivity(myFixture.getProject());

        actionEvent = new MockActionOnFileEvent(myFixture);
        presentation = actionEvent.getPresentation();

        myFixture.getTempDirFixture().findOrCreateDir("test/java/org/demo");
        myFixture.getTempDirFixture().findOrCreateDir(CUSTOM_DOC_FOLDER + "/org/demo");

        myFixture.addFileToProject(CUSTOM_DOC_FOLDER + "/_AnotherFile.approved.adoc", "approved content");
        myFixture.addFileToProject(CUSTOM_DOC_FOLDER + "/_AnotherFile.received.adoc", "approved content");


        final PsiFile propertyFile = myFixture.addFileToProject("docAsTest.properties", "DOC_PATH:src/" + CUSTOM_DOC_FOLDER);
        new DocAsTestStartupActivity().runActivity(myFixture.getProject());

    }

    public void test_menu_entry_when_approved_file_with_package() throws IOException {
        final Map<String, PsiFile> files = fileHelper.initFiles(
                FILE_NAMES.documents_org_demo_fileA_approved_adoc
        );
        menu_entry_when_file_with_package(actionApprovedUnderTest, "approved");
    }

    public void test_menu_entry_when_received_file_with_package() throws IOException {
        final Map<String, PsiFile> files = fileHelper.initFiles(
                FILE_NAMES.documents_org_demo_fileA_received_adoc
        );
        menu_entry_when_file_with_package(actionReceivedUnderTest, "received");
    }

    private void menu_entry_when_file_with_package(SwitchToFileAction actionUnderTest, String approvalType) {
        final PsiFile classFile = fileHelper.addTestClassFile(Paths.get("org", "demo"), "FileA", CaretOn.CLASS);

        DocAsTestStartupActivity.loadProperties(myFixture.getProject());

        actionEvent.performUpdateOnEditor(actionUnderTest, myFixture, classFile);
        assertTrue(presentation.isVisible());
        assertEquals("Switch to " + approvalType + " file", presentation.getText());

        actionEvent.performActionOnEditor(actionUnderTest, myFixture, classFile);
        assertEquals("_FileA." + approvalType + ".adoc", getFileNameInEditor());
    }

}
