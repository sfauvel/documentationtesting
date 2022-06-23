import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffManager;
import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import docAsTest.DocAsTestAction;
import docAsTest.approvalFile.ApprovalFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;

public class CompareFileAction extends DocAsTestAction {

    class FilesToCompare {
        final Optional<VirtualFile> approved;
        final Optional<VirtualFile> receveived;

        public FilesToCompare(Optional<VirtualFile> approved, Optional<VirtualFile> receveived) {
            this.approved = approved;
            this.receveived = receveived;
        }

        public boolean arePresents() {
            return approved.isPresent() && receveived.isPresent();
        }
    }
    @Override
    public void update(AnActionEvent e) {
        LOG.debug("CompareFileAction.update " + this.getClass().getName());
        traceActionEvent(e);

        boolean filesToCompareArePresents = getFilesToCompare(e).arePresents();

        e.getPresentation().setVisible(filesToCompareArePresents);
        e.getPresentation().setEnabled(filesToCompareArePresents);

        if (filesToCompareArePresents) {
            e.getPresentation().setText("Compare files");
        }
    }

    @NotNull
    private FilesToCompare getFilesToCompare(AnActionEvent e) {
        VirtualFile fileSelected = e.getData(PlatformDataKeys.VIRTUAL_FILE);

        final Project project = e.getProject();
        final Optional<PsiElement> psiElement = Optional.ofNullable(e.getData(CommonDataKeys.PSI_ELEMENT));

        boolean filesToCompareArePresents = false;
        FilesToCompare filesToCompare;
        if (psiElement.map(this::getPsiJavaFile).isPresent()) {
            filesToCompare = getFilesToCompare(project, psiElement.get());
        } else {
            final Optional<VirtualFile> approvalFileOptional = getFileToCompare(fileSelected);
            filesToCompare = new FilesToCompare(Optional.ofNullable(fileSelected), approvalFileOptional);
        }
        return filesToCompare;
    }

    @NotNull
    private FilesToCompare getFilesToCompare(Project project, PsiElement psiJavaFile) {
        return new FilesToCompare(
                getApprovedVirtualFile(project, psiJavaFile, ApprovalFile.Status.APPROVED),
                getApprovedVirtualFile(project, psiJavaFile, ApprovalFile.Status.RECEIVED)
        );
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        LOG.debug("CompareFileAction.actionPerformed");
        traceActionEvent(e);

        final FilesToCompare filesToCompare = getFilesToCompare(e);
        if (filesToCompare.arePresents()) {
            showDiff(e, filesToCompare.approved.get(), filesToCompare.receveived.get());
        }
    }

    protected void showDiff(AnActionEvent e, VirtualFile fileApproved, VirtualFile fileReceived) {
        DiffRequest diffRequest = getDiffRequest(e, fileReceived, fileApproved);
        DiffManager.getInstance().showDiff(e.getProject(), diffRequest);
    }

    private Optional<VirtualFile> getFileToCompare(VirtualFile fileSelected) {
        return Optional.ofNullable(fileSelected)
                .filter(Predicate.not(VirtualFile::isDirectory))
                .map(VirtualFile::getName)
                .map(this::getApprovalFile)
                .map(file -> file.to(file.isReceived()
                        ? ApprovalFile.Status.APPROVED
                        : ApprovalFile.Status.RECEIVED))
                .map(file -> fileSelected.findFileByRelativePath("../" + file.getName()));
    }

    @Nullable
    private ApprovalFile getApprovalFile(String filename) {
        return ApprovalFile.valueOf(filename).orElse(null);
    }

    protected DiffRequest getDiffRequest(@NotNull AnActionEvent e, VirtualFile fileReceived, VirtualFile fileApproved) {

        DiffContent dc1 = DiffContentFactory.getInstance().create(e.getProject(), fileReceived);
        DiffContent dc2 = DiffContentFactory.getInstance().create(e.getProject(), fileApproved);

        return new SimpleDiffRequest("Compare files", dc1, dc2, fileReceived.getName(), fileApproved.getName());

    }
}
