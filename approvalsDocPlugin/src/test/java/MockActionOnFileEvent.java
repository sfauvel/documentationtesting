import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mockito.Mockito;

class MockActionOnFileEvent extends MockActionEvent {

    public MockActionOnFileEvent(Project project, VirtualFile virtualFile, PsiFile psiFile) {
        super(project);
        Mockito.when(getDataContext().getData(PlatformDataKeys.PSI_FILE)).thenReturn(psiFile);
        Mockito.when(getDataContext().getData(PlatformDataKeys.VIRTUAL_FILE)).thenReturn(virtualFile);
    }

    public MockActionOnFileEvent(PsiFile psiFile) {
        this(psiFile.getProject(), psiFile.getVirtualFile(), psiFile);
    }

    public MockActionOnFileEvent(Project project, VirtualFile virtualFile) {
        this(project, virtualFile, null);
    }

}
