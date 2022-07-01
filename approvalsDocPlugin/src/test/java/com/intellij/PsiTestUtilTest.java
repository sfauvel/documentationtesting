package com.intellij;

import com.intellij.openapi.roots.SourceFolder;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.io.IOException;

/**
 * https://upsource.jetbrains.com/idea-ce/file/idea-ce-6ed4281aa53e1672d2e5870243c6606bb48afc84/platform/testFramework/src/com/intellij/testFramework/PsiTestUtil.java?_ga=2.222667998.458677862.1655848728-558951720.1649677350&_gl=1*1dhj1j8*_ga*NTU4OTUxNzIwLjE2NDk2NzczNTA.*_ga_9J976DJZ68*MTY1NTg5OTA0OS4yNS4xLjE2NTU5MDEyODUuMA..
 */
public class PsiTestUtilTest extends BasePlatformTestCase {
    public void test_addSourceRoot() throws IOException {

        final SourceFolder test = PsiTestUtil.addSourceRoot(myFixture.getModule(), myFixture.getTempDirFixture().findOrCreateDir("test"));

        final PsiFile psiFile = myFixture.configureByText("toto.java", "// Something");
        // Source root is not changed.
        assertEquals("/src/toto.java", psiFile.getVirtualFile().getPath());

    }
}
