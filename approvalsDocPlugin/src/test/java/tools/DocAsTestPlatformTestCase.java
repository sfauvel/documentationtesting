package tools;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl;
import docAsTest.DocAsTestFilenameIndex;
import docAsTest.DocAsTestFilenameIndex.SlowOperationPolicy;
import docAsTest.DocAsTestStartupActivity;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class DocAsTestPlatformTestCase extends BasePlatformTestCase {

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

    public PsiFile configureByText(@NotNull final VirtualFile root, @NotNull final String fileName, @NotNull final String text) {

        try {
            VirtualFile vFile = WriteCommandAction.writeCommandAction(getProject()).compute(() -> {
                final VirtualFile file;
                if (!(myFixture.getTempDirFixture() instanceof LightTempDirTestFixtureImpl)) {
                    throw new RuntimeException("Handle only LightTempDirTestFixtureImpl as TempDirFixture");
                }
                root.refresh(false, false);
                file = root.findOrCreateChildData(this, fileName);
                assertNotNull(fileName + " not found in " + root.getPath(), file);

                final Document document = FileDocumentManager.getInstance().getCachedDocument(file);
                if (document != null) {
                    PsiDocumentManager.getInstance(getProject()).doPostponedOperationsAndUnblockDocument(document);
                    FileDocumentManager.getInstance().saveDocument(document);
                }
                VfsUtil.saveText(file, text);
                return file;
            });
            ((CodeInsightTestFixtureImpl) myFixture).configureFromExistingVirtualFile(vFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return myFixture.getFile();
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
