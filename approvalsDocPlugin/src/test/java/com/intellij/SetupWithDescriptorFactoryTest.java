package com.intellij;

import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SetupWithDescriptorFactoryTest extends BasePlatformTestCase /*DocAsTestPlatformTestCase*/ {
    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return new MultiSourcePathLightProjectDescriptor(Arrays.asList(
                Paths.get("src/main/java"),
                Paths.get("src/test/java")
        ));
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


}
