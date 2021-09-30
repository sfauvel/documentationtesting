import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class MockActionOnFileEvent extends MockActionEvent {
    private final PsiFile psiFile;
    private final VirtualFile virtualFile;

    public MockActionOnFileEvent(PsiFile psiFile) {
        this.psiFile = psiFile;
        this.virtualFile = psiFile.getVirtualFile();
    }

    public MockActionOnFileEvent(VirtualFile virtualFile) {
        this.psiFile = null;
        this.virtualFile = virtualFile;
    }

    @Override
    public <T> @Nullable T getData(@NotNull DataKey<T> key) {
        if (PlatformDataKeys.VIRTUAL_FILE == key) return (T) virtualFile;
        if (PlatformDataKeys.PSI_FILE == key) return (T) psiFile;
        return null;
    }

    @Override
    public @Nullable Project getProject() {
        return psiFile.getProject();
    }
}
