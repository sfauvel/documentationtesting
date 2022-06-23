
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import docAsTest.DocAsTestAction;
import docAsTest.approvalFile.ApprovalFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ApproveFileAction extends DocAsTestAction {

    @Override
    public void update(AnActionEvent actionEvent) {
        LOG.debug("ApproveFileAction.update");
        traceActionEvent(actionEvent);

        final VirtualFile[] dataFiles = getData(actionEvent);
        actionEvent.getPresentation().setVisible(Arrays.stream(dataFiles).anyMatch(this::isVisible));
        actionEvent.getPresentation().setText(getApproveActionName(dataFiles));
    }

    @Nullable
    private VirtualFile[] getData(AnActionEvent actionEvent) {
        return Optional.ofNullable(actionEvent.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY))
                .orElse(new VirtualFile[0]);
    }

    private boolean isVisible(VirtualFile data) {
        if (data.isDirectory()) {
            return true;
        } else {
            return ApprovalFile.isReceivedFilename(data.getName());
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        LOG.debug("ApproveFileAction.actionPerformed");
        traceActionEvent(actionEvent);

        final @Nullable VirtualFile[] actionEventData = getData(actionEvent);

        CommandProcessor.getInstance().executeCommand(
                actionEvent.getProject(),
                new ApprovedRunnable(actionEvent.getProject(), actionEventData),
                getApproveActionName(actionEventData),
                "Approvals",
                getUndoConfirmationPolicy());
    }

    @NotNull
    private String getApproveActionName(VirtualFile[] actionEventData) {
        if (actionEventData.length == 1) {
            return actionEventData[0].isDirectory() ? "Approved All" : "Approved file";
        } else {
            return "Approved selected files";
        }
    }

    static class ApprovedRunnable implements Runnable {
        private final List<VirtualFile> virtualFiles;
        private final Project project;

        public ApprovedRunnable(Project project, VirtualFile virtualFile) {
            this.project = project;
            this.virtualFiles = Arrays.asList(virtualFile);
        }

        public ApprovedRunnable(Project project, VirtualFile[] virtualFiles) {
            this.project = project;
            this.virtualFiles = Arrays.asList(virtualFiles);
        }

        private void addReceivedFiles(List<VirtualFile> receivedFiles, VirtualFile virtualFile) {
            if (!virtualFile.isDirectory()) {
                if (ApprovalFile.isReceivedFilename(virtualFile.getName())) {
                    receivedFiles.add(virtualFile);
                }
            } else {
                Arrays.stream(virtualFile.getChildren())
                        .forEach(child -> addReceivedFiles(receivedFiles, child));
            }

        }

        private List<VirtualFile> getReceivedFiles(List<VirtualFile> virtualFiles) {
            List<VirtualFile> receivedFiles = new ArrayList<VirtualFile>();
            virtualFiles.forEach(virtualFile -> addReceivedFiles(receivedFiles, virtualFile));
            return receivedFiles;
        }

        @Override
        public void run() {
            ApplicationManager.getApplication().runWriteAction(() -> approveReceivedFiles());
        }

        private void approveReceivedFiles() {
            List<VirtualFile> filesToRename = getReceivedFiles(virtualFiles);

            if (!Messages.isApplicationInUnitTestOrHeadless()) {
                int result = Messages.showYesNoDialog(project, "You will approved " + filesToRename.size() + " files.\nDo you want to continue ?", "Approvals", Messages.getQuestionIcon());
                if (result == Messages.NO) {
                    return;
                }
            }

            for (VirtualFile fileToRename : filesToRename) {
                String oldFilename = fileToRename.getName();
                ApprovalFile approvalFile = ApprovalFile.valueOf(oldFilename).get();
                String newFileName = approvalFile.to(ApprovalFile.Status.APPROVED).getName();
                try {
                    VirtualFile newFileByRelativePath = fileToRename.findFileByRelativePath("../" + newFileName);
                    if (newFileByRelativePath != null) {
                        newFileByRelativePath.delete(null);
                    }

                    fileToRename.rename(null, newFileName);
                    LOG.debug(String.format("File '%s' renamed to '%s'", oldFilename, newFileName));
                } catch (IOException ex) {
                    ex.printStackTrace();
                    System.err.println(String.format("File '%s' could not be renamed to '%s'", oldFilename, newFileName));
                }

            }
        }
    }


}