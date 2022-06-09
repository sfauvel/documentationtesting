import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffManager;
import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;

public class CompareFileAction extends AnAction {

    @Override
    public void update(AnActionEvent e) {

        VirtualFile fileSelected = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        final Optional<VirtualFile> approvalFileOptional = getFileToCompare(fileSelected);

        e.getPresentation().setVisible(approvalFileOptional.isPresent());
        e.getPresentation().setEnabled(approvalFileOptional.isPresent());

        if (approvalFileOptional.isPresent()) {
            e.getPresentation().setText("Compare files");
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        VirtualFile fileSelected = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        final Optional<VirtualFile> fileToCompare = getFileToCompare(fileSelected);

        if (fileToCompare.isPresent()) {
            DiffRequest diffRequest = getDiffRequest(e, fileSelected, fileToCompare.get());
            DiffManager.getInstance().showDiff(e.getProject(), diffRequest);
        }
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
