package tools;

import com.intellij.MultiSourcePathLightProjectDescriptor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightProjectDescriptor;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class DocAsTestPlatformCustomFolderTest extends DocAsTestPlatformTestCase {
    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return new MultiSourcePathLightProjectDescriptor(Arrays.asList(
                Paths.get("src/main/java"),
                Paths.get("src/test/java")
        ));
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        fileHelper = new FileHelper(myFixture);
    }

    public void test_get_root_source_path() throws IOException {
        final VirtualFile srcPath = main_source_path();
        assertEquals("java", srcPath.getName());
        assertEquals("/src/test/java", srcPath.getPath());
        assertEquals("/src/test/java", srcPath.getCanonicalPath());
        assertEquals("temp:///src/test/java", srcPath.getUrl());
    }

    public void test_find_or_create_from_path() throws IOException {
        final Path pathToCreate = Paths.get("src/custom/docs");

        final VirtualFile docs_path = findOrCreate(pathToCreate);

        assertEquals("/" + pathToCreate.toString(), docs_path.getPath());
    }

    public void test_find_or_create_from_string() throws IOException {

        assertEquals(
                "/" + "tmp/custom/my/folder",
                findOrCreate("tmp/custom/my/folder").getPath()
        );
    }

    public void test_add_file_on_source_folders() throws IOException {
        final PsiFile testFile = myFixture.configureByText("TestMyClass.java", "My text");
        assertEquals("/src/test/java/TestMyClass.java", testFile.getVirtualFile().getPath());
        {
            VirtualFile fileByRelativePath = testFile.getVirtualFile()
                    .getParent()
                    .getParent()
                    .getParent()
                    .findFileByRelativePath("main")
                    .findFileByRelativePath("java");
            final PsiFile javaFile = configureByText(fileByRelativePath, "MyClass.java", "My text");
            assertEquals("/src/main/java/MyClass.java", javaFile.getVirtualFile().getPath());
        }
        {
            VirtualFile fileByRelativePath = testFile.getVirtualFile()
                    .findFileByRelativePath("../../../main/java");
            final PsiFile javaFile = configureByText(fileByRelativePath, "MyOtherClass.java", "My text");
            assertEquals("/src/main/java/MyOtherClass.java", javaFile.getVirtualFile().getPath());
        }
    }

    public void test_add_file_not_on_source_folders() throws IOException {
        final PsiFile testFile = myFixture.configureByText("TestMyClass.java", "My text");
        assertEquals("/src/test/java/TestMyClass.java", testFile.getVirtualFile().getPath());

        final VirtualFile virtualFileFoundOrCreated = findOrCreate(testFile, Paths.get("../../docs"));
        final PsiFile javaFile = configureByText(virtualFileFoundOrCreated, "MyClass.adoc", "My text");
        assertEquals("/src/test/docs/MyClass.adoc", javaFile.getVirtualFile().getPath());
    }

    public void test_add_file_on_the_last_source_folder() throws IOException {
        final PsiFile psiFile = myFixture.configureByText("file.txt", "My text");
        assertEquals("/src/test/java/file.txt", psiFile.getVirtualFile().getPath());

        final VirtualFile virtualFileFoundOrCreated = findOrCreate(psiFile, Paths.get("../../../../documents"));

        myFixture.configureByText("toto.txt", "coucou");
        final PsiFile customFile = configureByText(virtualFileFoundOrCreated, "file.adoc", "My text");

        assertEquals("/documents/file.adoc", customFile.getVirtualFile().getPath());
    }

}
