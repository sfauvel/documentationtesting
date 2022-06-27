package tools;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import docAsTest.DocAsTestFilenameIndex;
import docAsTest.DocAsTestFilenameIndex.SlowOperationPolicy;
import docAsTest.DocAsTestStartupActivity;
import org.jetbrains.annotations.NotNull;

public abstract class DocAsTestPlatformTest extends BasePlatformTestCase {

    public static final SlowOperationPolicy NO_SLOW_OPERATION_POLICY = new SlowOperationPolicy() {
        @Override
        public void run() {
            throw new RuntimeException("No slow operation authorized in this test");
        }
    };
    public static final SlowOperationPolicy SLOW_OPERATION_ALLOWED = new SlowOperationPolicy();

    protected FileHelper fileHelper;

    @Override
    protected void setUp() throws Exception {
        DocAsTestStartupActivity.reset();
        DocAsTestFilenameIndex.setSlowOperationPolicy(SLOW_OPERATION_ALLOWED);
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
