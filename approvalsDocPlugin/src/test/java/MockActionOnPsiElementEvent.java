import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import org.mockito.Mockito;
import tools.MockActionEvent;

class MockActionOnPsiElementEvent extends MockActionEvent {

    public MockActionOnPsiElementEvent(PsiElement psiElement) {
        super(psiElement.getProject());
        Mockito.when(getDataContext().getData(PlatformDataKeys.PSI_ELEMENT)).thenReturn(psiElement);
//        System.out.println("MockActionOnPsiElementEvent.MockActionOnPsiElementEvent");
//        System.out.println("MockActionOnPsiElementEvent.MockActionOnPsiElementEvent " + psiElement.getClass().getName());
        if(psiElement instanceof PsiClass) {
//            System.out.println("MockActionOnPsiElementEvent.MockActionOnPsiElementEvent");
            VirtualFile virtualFile = ((PsiClass) psiElement).getContainingFile().getVirtualFile();
            Mockito.when(getDataContext().getData(PlatformDataKeys.VIRTUAL_FILE)).thenReturn(virtualFile);
            Mockito.when(getDataContext().getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY)).thenReturn(new VirtualFile[]{virtualFile});
        }
//        if(psiElement instanceof PsiFile) {
//            System.out.println("MockActionOnPsiElementEvent.MockActionOnPsiElementEvent");
//            VirtualFile virtualFile = ((PsiFile)psiElement).getVirtualFile();
//            Mockito.when(getDataContext().getData(PlatformDataKeys.VIRTUAL_FILE)).thenReturn(virtualFile);
//            Mockito.when(getDataContext().getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY)).thenReturn(new VirtualFile[]{virtualFile});
//        }
    }
}
