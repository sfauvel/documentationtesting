package docAsTest.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import docAsTest.approvalFile.ApprovalFile;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public abstract class SwitchToFileAction extends SwitchAction {
    private final ApprovalFile.Status approvalType;

    protected SwitchToFileAction(ApprovalFile.Status approvalType) {
        this.approvalType = approvalType;
    }

    @Override
    protected boolean isFileToSwitchExist(AnActionEvent actionEvent) {
        return getApprovedVirtualFile(actionEvent).isPresent();
    }

    @Override
    protected Optional<Runnable> getRunnableAction(@NotNull AnActionEvent actionEvent) {
        LOG.debug("getRunnableAction");
        final Optional<PsiFile> approvedPsiFile = getApprovedPsiFile(actionEvent);
        if (approvedPsiFile.isEmpty()) return Optional.empty();

        return Optional.of(new ApprovedRunnable(actionEvent.getProject(), approvedPsiFile.get()));
    }

    static class ApprovedRunnable implements Runnable {
        private final PsiFile approvedPsiFile;
        private final Project project;

        ApprovedRunnable(Project project, PsiFile approvedPsiFile) {
            this.project = project;
            this.approvedPsiFile = approvedPsiFile;
        }

        @Override
        public void run() {
            ApplicationManager.getApplication().runWriteAction(() -> runAction());
        }

        private void runAction() {
            FileEditorManager.getInstance(project)
                    .openTextEditor(new OpenFileDescriptor(project, approvedPsiFile.getVirtualFile()), true);
        }
    }

    protected Optional<PsiFile> getApprovedPsiFile(AnActionEvent actionEvent) {
        final Project project = actionEvent.getProject();
        return Optional.ofNullable(actionEvent.getData(CommonDataKeys.PSI_ELEMENT))
                .flatMap(psi -> getApprovedPsiFile(project, psi));

    }

    @NotNull
    protected Optional<PsiFile> getApprovedPsiFile(Project project, Editor editor, PsiFile psiFile) {
        PsiElement element = Optional.ofNullable(editor)
                .map(e -> editor.getCaretModel().getOffset())
                .map(offset -> psiFile.findElementAt(offset))
                .orElse(psiFile);

        return getApprovedPsiFile(project, element);
    }

    @NotNull
    protected Optional<PsiFile> getApprovedPsiFile(Project project, PsiElement element) {
        return getApprovedVirtualFile(project, element, this.approvalType)
                .map(virtualFile -> PsiManager.getInstance(project).findFile(virtualFile));
    }

    @NotNull
    protected Optional<VirtualFile> getApprovedVirtualFile(AnActionEvent actionEvent) {
        final Project project = actionEvent.getProject();
        return Optional.ofNullable(actionEvent.getData(CommonDataKeys.PSI_ELEMENT))
                .flatMap(psi -> getApprovedVirtualFile(project, psi, this.approvalType));
    }

}
