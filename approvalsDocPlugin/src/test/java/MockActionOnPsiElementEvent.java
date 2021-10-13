import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.mockito.Mockito;

class MockActionOnPsiElementEvent extends MockActionEvent {

    public MockActionOnPsiElementEvent(PsiElement psiElement) {
        super(psiElement.getProject());
        Mockito.when(getDataContext().getData(PlatformDataKeys.PSI_ELEMENT)).thenReturn(psiElement);
    }
}
