package tools;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.UndoConfirmationPolicy;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.PsiElementNavigatable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import docAsTest.DocAsTestAction;
import org.jetbrains.annotations.NotNull;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;


public class MockActionOnFileEvent extends MockActionEvent {

    public MockActionOnFileEvent(PsiFile psiFile) {
        super(psiFile.getProject());
        withSelectedFilesFromMenuSecondCall(Arrays.asList(psiFile));
        Mockito.when(getDataContext().getData(PlatformDataKeys.PSI_FILE)).thenReturn(psiFile);
    }

    public MockActionOnFileEvent(CodeInsightTestFixture myFixture) {
        super(myFixture.getProject());
    }

    @NotNull
    private MockActionOnFileEvent withSelectedFromMenuFirstCall(List<? extends PsiFileSystemItem> psiFiles) {
        Mockito.when(getDataContext().getData(CommonDataKeys.EDITOR)).thenReturn(null);
        Mockito.when(getDataContext().getData(PlatformDataKeys.PSI_FILE)).thenReturn(null);
        Mockito.when(getDataContext().getData(PlatformDataKeys.PSI_ELEMENT)).thenReturn(
                psiFiles.size() == 1 ? psiFiles.get(0) : null
        );
        Mockito.when(getDataContext().getData(PlatformDataKeys.NAVIGATABLE)).thenReturn(null);
        Mockito.when(getDataContext().getData(PlatformDataKeys.NAVIGATABLE_ARRAY)).thenReturn(
                psiFiles.isEmpty()
                        ? null
                        : psiFiles.stream()
                        .map(PsiElementNavigatable::new)
                        .toArray(Navigatable[]::new)
        );

        Mockito.when(getDataContext().getData(PlatformDataKeys.VIRTUAL_FILE)).thenReturn(null);
        Mockito.when(getDataContext().getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)).thenReturn(null);
        return this;
    }

    @NotNull
    public MockActionOnFileEvent withSelectedFilesFromMenuSecondCall(List<? extends PsiFileSystemItem> psiFiles) {
        Mockito.when(getDataContext().getData(CommonDataKeys.EDITOR)).thenReturn(null);
        Mockito.when(getDataContext().getData(PlatformDataKeys.PSI_FILE)).thenReturn(
                (psiFiles.size() >= 1 && psiFiles.get(0) instanceof PsiFile)
                        ? (PsiFile) psiFiles.get(0)
                        : null
        );
        Mockito.when(getDataContext().getData(PlatformDataKeys.PSI_ELEMENT)).thenReturn(
                (psiFiles.size() == 1) ? psiFiles.get(0) : null
        );
        Mockito.when(getDataContext().getData(PlatformDataKeys.NAVIGATABLE)).thenReturn(
                (psiFiles.size() == 1) ? psiFiles.get(0) : null
        );
        Mockito.when(getDataContext().getData(PlatformDataKeys.NAVIGATABLE_ARRAY)).thenReturn(
                psiFiles.isEmpty()
                        ? null
                        : psiFiles.stream()
                        .map(PsiElementNavigatable::new)
                        .toArray(Navigatable[]::new)
        );

        final VirtualFile[] virtualFiles = psiFiles.stream().map(PsiFileSystemItem::getVirtualFile).toArray(VirtualFile[]::new);
        Mockito.when(getDataContext().getData(PlatformDataKeys.VIRTUAL_FILE)).thenReturn(
                psiFiles.isEmpty() ? null : virtualFiles[0]);
        Mockito.when(getDataContext().getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)).thenReturn(
                psiFiles.isEmpty() ? null : virtualFiles);
        return this;
    }

    public MockActionOnFileEvent withSelectedFileOnEditorFirstCall(Editor editor, PsiFile psiFile) {

        Mockito.when(getDataContext().getData(CommonDataKeys.EDITOR)).thenReturn(editor);
        Mockito.when(getDataContext().getData(PlatformDataKeys.PSI_FILE)).thenReturn(psiFile);
        Mockito.when(getDataContext().getData(PlatformDataKeys.PSI_ELEMENT)).thenReturn(null);
        Mockito.when(getDataContext().getData(PlatformDataKeys.NAVIGATABLE)).thenReturn(null);
        Mockito.when(getDataContext().getData(PlatformDataKeys.NAVIGATABLE_ARRAY)).thenReturn(null);
        Mockito.when(getDataContext().getData(PlatformDataKeys.VIRTUAL_FILE)).thenReturn(psiFile.getVirtualFile());
        Mockito.when(getDataContext().getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)).thenReturn(
                Arrays.stream(new PsiFile[]{psiFile})
                        .map(PsiFile::getVirtualFile)
                        .toArray(VirtualFile[]::new));
        return this;
    }

    public MockActionOnFileEvent withSelectedFileOnEditorSecondCall(Editor editor, PsiFile psiFile) {
        final int offset = editor.getCaretModel().getOffset();
        final PsiElement parentElement = offset == 0
                ? null
                : psiFile.findElementAt(offset);//.getParent();
        Mockito.when(getDataContext().getData(CommonDataKeys.EDITOR)).thenReturn(editor);
        Mockito.when(getDataContext().getData(PlatformDataKeys.PSI_FILE)).thenReturn(psiFile);

        Mockito.when(getDataContext().getData(PlatformDataKeys.PSI_ELEMENT)).thenReturn(
                parentElement
        );
        Mockito.when(getDataContext().getData(PlatformDataKeys.NAVIGATABLE)).thenReturn(
                (Navigatable) parentElement
        );
        Mockito.when(getDataContext().getData(PlatformDataKeys.NAVIGATABLE_ARRAY)).thenReturn(
                (parentElement == null)
                        ? null
//                        : new Navigatable[]{(Navigatable) psiFile.findElementAt(editor.getCaretModel().getOffset()).getParent()}
                        : new Navigatable[]{(Navigatable) parentElement}
        );
        Mockito.when(getDataContext().getData(PlatformDataKeys.VIRTUAL_FILE)).thenReturn(psiFile.getVirtualFile());
        Mockito.when(getDataContext().getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)).thenReturn(
                Arrays.stream(new PsiFile[]{psiFile})
                        .map(PsiFile::getVirtualFile)
                        .toArray(VirtualFile[]::new));
        return this;
    }

    final DocAsTestAction.SlowOperationPolicy noSlowOperationPolicy = new DocAsTestAction.SlowOperationPolicy() {
        @Override
        public void runSlowOperation() {
            throw new RuntimeException("No slow operation authorized in this test");
        }
    };

    public void performUpdate(DocAsTestAction action, PsiFileSystemItem psiFile) {
        performUpdate(action, Arrays.asList(psiFile));
    }

    public void performUpdate(DocAsTestAction action, List<? extends PsiFileSystemItem> psiFiles) {
        action.setSlowOperationPolicy(noSlowOperationPolicy);

        withSelectedFromMenuFirstCall(psiFiles);
        action.update(this);

        withSelectedFilesFromMenuSecondCall(psiFiles);
        action.update(this);
    }

    public void performAction(DocAsTestAction action, PsiFileSystemItem psiItem) {
        performAction(action, Arrays.asList(psiItem));
    }

    public void performAction(DocAsTestAction action, List<? extends PsiFileSystemItem> psiItems) {
        action.setUndoConfirmationPolicy(UndoConfirmationPolicy.DO_NOT_REQUEST_CONFIRMATION);

        // performAction call update and actionPerformed with the same dataContext
        // with the one that is send on the second call used in update method.
        withSelectedFilesFromMenuSecondCall(psiItems);

        action.update(this);
        action.actionPerformed(this);
    }

    public void performUpdateOnEditor(DocAsTestAction action, CodeInsightTestFixture myFixture, PsiFile psiFile) {
        FileDocumentManager.getInstance().reloadFiles(psiFile.getVirtualFile());
        myFixture.configureFromExistingVirtualFile(psiFile.getVirtualFile());

        withSelectedFileOnEditorFirstCall(myFixture.getEditor(), psiFile);
        action.update(this);
        withSelectedFileOnEditorSecondCall(myFixture.getEditor(), psiFile);
        action.update(this);
    }

    public void performActionOnEditor(DocAsTestAction action, CodeInsightTestFixture myFixture, PsiFile psiFile) {
        action.setUndoConfirmationPolicy(UndoConfirmationPolicy.DO_NOT_REQUEST_CONFIRMATION);

        FileDocumentManager.getInstance().reloadFiles(psiFile.getVirtualFile());
        myFixture.configureFromExistingVirtualFile(psiFile.getVirtualFile());

        withSelectedFileOnEditorSecondCall(myFixture.getEditor(), psiFile);

        action.update(this);
        action.actionPerformed(this);
    }

}
