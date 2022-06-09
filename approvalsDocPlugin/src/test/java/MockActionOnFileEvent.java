import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.mockito.Mockito;

class MockActionOnFileEvent extends MockActionEvent {

    public MockActionOnFileEvent(Project project) {
        super(project);
    }

    public MockActionOnFileEvent withSelectedFiles(VirtualFile... virtualFiles) {
        Mockito.when(getDataContext().getData(PlatformDataKeys.VIRTUAL_FILE)).thenReturn(virtualFiles[0]);
        Mockito.when(getDataContext().getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY)).thenReturn(virtualFiles);
        return this;
    }

    public MockActionOnFileEvent(PsiFile psiFile) {
        this(psiFile.getProject());
        withSelectedFiles(psiFile.getVirtualFile());
//        Mockito.when(getDataContext().getData(PlatformDataKeys.PSI_FILE)).thenReturn(psiFile);
    }
}
