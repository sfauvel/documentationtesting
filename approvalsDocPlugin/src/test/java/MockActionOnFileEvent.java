import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.mockito.Mockito;

import java.util.List;

class MockActionOnFileEvent extends MockActionEvent {

    public MockActionOnFileEvent(Project project) {
        super(project);
    }

    public MockActionOnFileEvent(Project project, VirtualFile virtualFile) {
        this(project);
        Mockito.when(getDataContext().getData(PlatformDataKeys.VIRTUAL_FILE)).thenReturn(virtualFile);
    }

    public MockActionOnFileEvent(Project project, List<VirtualFile> virtualFiles) {
        this(project);
        Mockito.when(getDataContext().getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY)).thenReturn(virtualFiles.toArray(new VirtualFile[0]));
    }

    public MockActionOnFileEvent(PsiFile psiFile) {
        this(psiFile.getProject(), psiFile.getVirtualFile());
        Mockito.when(getDataContext().getData(PlatformDataKeys.PSI_FILE)).thenReturn(psiFile);
    }
}
