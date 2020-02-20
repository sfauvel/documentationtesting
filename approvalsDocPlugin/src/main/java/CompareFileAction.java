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

public class CompareFileAction extends AnAction {

    @Override
    public void update(AnActionEvent e) {
        VirtualFile data = e.getData(PlatformDataKeys.VIRTUAL_FILE);

        String nameWithoutExtension = data.getNameWithoutExtension();
        if (data.isDirectory()) {
            e.getPresentation().setVisible(false);
        } else {
            e.getPresentation().setText("Compare files");
            ApprovalFile approvalFile = getApprovalFile(data.getName());
            if (approvalFile == null) {
                e.getPresentation().setVisible(false);
            } else {
                ApprovalFile filenameToCompare = approvalFile.to(approvalFile.isReceived()
                        ? ApprovalFile.Status.APPROVED
                        : ApprovalFile.Status.RECEIVED);
                VirtualFile fileToCompare = e.getData(PlatformDataKeys.VIRTUAL_FILE).findFileByRelativePath("../" + filenameToCompare.getName());
                e.getPresentation().setVisible(true);
                e.getPresentation().setEnabled(fileToCompare != null);
            }

        }

    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        VirtualFile data = e.getData(PlatformDataKeys.VIRTUAL_FILE);

        ApprovalFile approvalFile = getApprovalFile(data.getName());
        if (approvalFile == null) {
            return;
        }

        ApprovalFile filenameToCompare = approvalFile.to(approvalFile.isReceived()
                ? ApprovalFile.Status.APPROVED
                : ApprovalFile.Status.RECEIVED);


        VirtualFile fileSelected = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        VirtualFile fileToCompare = e.getData(PlatformDataKeys.VIRTUAL_FILE).findFileByRelativePath("../" + filenameToCompare.getName());
        if (fileToCompare != null) {
            DiffRequest diffRequest = getDiffRequest(e, fileSelected, fileToCompare);
            DiffManager.getInstance().showDiff(e.getProject(), diffRequest);
        }
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
