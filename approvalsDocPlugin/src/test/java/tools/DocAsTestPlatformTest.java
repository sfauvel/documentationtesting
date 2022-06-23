package tools;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

public abstract class DocAsTestPlatformTest extends BasePlatformTestCase {

    protected FileHelper fileHelper;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        fileHelper = new FileHelper(myFixture);
    }

    @NotNull
    protected String getFileNameInEditor() {
        return FileEditorManager.getInstance(myFixture.getProject()).getSelectedEditor().getFile().getName();
    }

    protected void assertExists(String filename) {
        assertExists(myFixture.findFileInTempDir(filename));
    }

    protected void assertExists(VirtualFile virtualFile) {
        assertNotNull(virtualFile);
    }

    protected void assertNotExists(String psiFile) {
        assertNotExists(myFixture.findFileInTempDir(psiFile));
    }

    protected void assertNotExists(VirtualFile virtualFile) {
        assertNull(virtualFile);
    }
}
