package com.intellij;

import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightProjectDescriptor;
import tools.DocAsTestPlatformTest;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SetupWithDescriptorFactoryTest extends DocAsTestPlatformTest {
    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return new MultiSourcePathLightProjectDescriptor(Arrays.asList(
                Paths.get("src/main/java"),
                Paths.get("src/test/java")
        ));
    }


    protected void setUp() throws Exception {
        System.out.println("SetupWithDescriptorFactoryTest.test_setup");
        super.setUp();
    }

    public void test_set_up() throws Exception {
        System.out.println("SetupWithDescriptorFactoryTest.test_setup");

        final VirtualFile[] contentSourceRoots = ProjectRootManager.getInstance(myFixture.getProject()).getContentSourceRoots();

        final List<String> collect = Arrays.stream(contentSourceRoots)
                .map(VirtualFile::toString)
                .collect(Collectors.toList());

        System.out.println("sources:");
        for (String source : collect) {
            System.out.println(source);
        }
        assertTrue(collect.containsAll(Arrays.asList(
                "temp:///src/main/java",
                "temp:///src/test/java"
        )));
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
