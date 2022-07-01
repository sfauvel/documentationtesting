import com.intellij.MultiSourcePathLightProjectDescriptor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightProjectDescriptor;
import docAsTest.DocAsTestStartupActivity;
import tools.DocAsTestPlatformTestCase;
import tools.FieldAutoNaming;
import tools.FileHelper.CaretOn;
import tools.MockActionOnFileEvent;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class SwitchToJavaFileActionCustomeFolderTest extends DocAsTestPlatformTestCase {
    private final String CUSTOM_TEST_FOLDER = "src/test/custom_folder";
    private final String CUSTOM_DOC_FOLDER="documents/adoc";
    private final Path JAVA_TO_DOC = Paths.get(CUSTOM_TEST_FOLDER).relativize(Paths.get(CUSTOM_DOC_FOLDER));

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return new MultiSourcePathLightProjectDescriptor(Arrays.asList(
                Paths.get(CUSTOM_TEST_FOLDER)
        ));
    }

    private final SwitchToJavaFileAction actionJavaUnderTest = new SwitchToJavaFileAction() {
        @Override
        protected String getProjectBasePath(Project project) {
            return "/";
        }
    };
    private MockActionOnFileEvent actionEvent;

    public static class fileNames extends FieldAutoNaming {
        public String test_documents_fileA_approved_adoc;
    }

    final private fileNames FILE_NAMES = new fileNames();
    @Override
    protected void setUp() throws Exception {
        super.setUp();

//        final VirtualFile testDir = myFixture.getTempDirFixture().findOrCreateDir(CUSTOM_TEST_FOLDER);
        final VirtualFile docDir = myFixture.getTempDirFixture().findOrCreateDir(CUSTOM_DOC_FOLDER);
        actionEvent = new MockActionOnFileEvent(myFixture);

    }


    public void test_open_java_file_on_editor_when_on_approved() throws IOException {
        test_file_in_editor_when_open_it_from_approved_file(String.join("\n",
                "TEST_PATH:" + CUSTOM_TEST_FOLDER,
                "DOC_PATH:" + CUSTOM_DOC_FOLDER
        ),
                "MyClass.java",
                "_MyClass.approved.adoc");
    }

    public void test_not_open_java_file_on_editor_when_test_folder_is_not_set() throws IOException {
        test_file_in_editor_when_open_it_from_approved_file(String.join("\n",
                "DOC_PATH:" + CUSTOM_DOC_FOLDER
        ),
                "_MyClass.approved.adoc",
                "_MyClass.approved.adoc");
    }

    public void test_not_open_java_file_on_editor_when_doc_folder_is_not_set() throws IOException {
        test_file_in_editor_when_open_it_from_approved_file(String.join("\n",
                        "TEST_PATH:" + CUSTOM_TEST_FOLDER
                ),
                "_MyClass.approved.adoc",
                "_MyClass.approved.adoc");
    }

    private void test_file_in_editor_when_open_it_from_approved_file(String properties, String fileOnEditorAfterAction, String fileName) {
        final PsiFile propertyFile = myFixture.addFileToProject("docAsTest.properties",
                properties
        );
        new DocAsTestStartupActivity().runActivity(myFixture.getProject());

        final PsiFile javaFile = myFixture.addFileToProject("MyClass.java", fileHelper.generateCode(CaretOn.NONE));
        final VirtualFile virtualFile = findOrCreate(javaFile, Paths.get("..").resolve(JAVA_TO_DOC));
        final PsiFile approvedFile = configureByText(virtualFile, fileName, fileName + " content");

        assertEquals(fileName, getFileNameInEditor());

        AnActionEvent actionEvent = new MockActionOnPsiElementEvent(approvedFile);
        actionJavaUnderTest.actionPerformed(actionEvent);

        assertEquals(fileOnEditorAfterAction, getFileNameInEditor());
    }

}
