
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApproveFileAction extends AnAction {

    @Override
    public void update(AnActionEvent actionEvent) {
        VirtualFile data = actionEvent.getData(PlatformDataKeys.VIRTUAL_FILE);

        actionEvent.getPresentation().setText(getApproveActionName(data));
        if (data.isDirectory()) {
            actionEvent.getPresentation().setVisible(true);
        } else {
            actionEvent.getPresentation().setVisible(ApprovalFile.isReceivedFilename(data.getName()));
        }
//        e.getPresentation().setEnabled(visible && !isInProgress());

    }


    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {

        VirtualFile actionEventData = actionEvent.getData(PlatformDataKeys.VIRTUAL_FILE);
        List<VirtualFile> receivedChildren = getReceivedFiles(actionEventData);

        ApprovedAction approvedAction = new ApprovedAction(
                actionEvent.getProject(),
                receivedChildren
        );

        CommandProcessor.getInstance().executeCommand(
                actionEvent.getProject(),
                approvedAction,
                getApproveActionName(actionEventData),
                "Approvals");

    }

    @NotNull
    private String getApproveActionName(VirtualFile actionEventData) {
        return actionEventData.isDirectory() ? "Approved All" : "Approved file";
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

    private List<VirtualFile> getReceivedFiles(VirtualFile virtualFile) {
        List<VirtualFile> receivedFiles = new ArrayList<VirtualFile>();
        addReceivedFiles(receivedFiles, virtualFile);
        return receivedFiles;
    }

    static class ApprovedAction extends WriteAction {
        public ApprovedAction(Project project, List<VirtualFile> filesToRename) {
            super(() -> {
                for (VirtualFile fileToRename : filesToRename) {

                    ApprovalFile approvalFile = ApprovalFile.valueOf(fileToRename.getName()).get();
                    String newFileName = approvalFile.to(ApprovalFile.Status.APPROVED).getName();
                    try {
                        VirtualFile newFileByRelativePath = fileToRename.findFileByRelativePath("../" + newFileName);
                        if (newFileByRelativePath != null) {
                            newFileByRelativePath.delete(null);
                        }

                        fileToRename.rename(null, newFileName);
                        System.out.println(String.format("File '%s' renamed to '%s'", fileToRename.getName(), newFileName));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        System.err.println(String.format("File '%s' could not be renamed to '%s'", fileToRename.getName(), newFileName));
                    }

                }
            });
        }
    }

    static class WriteAction implements Runnable {
        WriteAction(Runnable cmd) {
            this.cmd = cmd;
        }

        public void run() {
            ApplicationManager.getApplication().runWriteAction(cmd);
        }

        Runnable cmd;
    }

}