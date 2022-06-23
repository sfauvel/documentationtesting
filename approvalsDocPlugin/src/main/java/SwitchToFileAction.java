import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        System.out.println("SwitchToFileAction.getRunnableAction approvedPsiFile: "+ approvedPsiFile.map(PsiFile::getName).orElse("???"));
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
        return getApprovedVirtualFile(project, element)
                .map(virtualFile -> PsiManager.getInstance(project).findFile(virtualFile));
    }

    @NotNull
    protected Optional<VirtualFile> getApprovedVirtualFile(AnActionEvent actionEvent) {
        final Project project = actionEvent.getProject();
        return Optional.ofNullable(actionEvent.getData(CommonDataKeys.PSI_ELEMENT))
                .flatMap(psi -> getApprovedVirtualFile(project, psi));
    }
    @NotNull
    protected Optional<VirtualFile> getApprovedVirtualFile(Project project, PsiElement element) {
        // TODO add test to chech strict=false is useful when on PsiMethod/PsiClass and not PsiIdentifier.
        PsiJavaFile containingJavaFile = (element instanceof PsiJavaFile)
                ? (PsiJavaFile) element
                : PsiTreeUtil.getParentOfType(element, PsiJavaFile.class, false);

        if (containingJavaFile == null) {
            return Optional.empty();
        }

        final String packageName = containingJavaFile.getPackageName();
        // TODO add test to chech strict=false is useful when on PsiMethod/PsiClass and not PsiIdentifier.
        final PsiClass containingClazz = PsiTreeUtil.getParentOfType(element, PsiClass.class, false);

        ApprovalFile approvalFile = containingClazz == null
                ? ApprovalFile.fromClass(packageName, containingJavaFile.getVirtualFile().getNameWithoutExtension())
                : approvalFileFromMethod(element, packageName, containingClazz);

        final Path approvedFilePath = Paths.get(getProjectBasePath(project))
                .resolve(getSrcDocs())
                .resolve(approvalFile.to(this.approvalType).getName());

        return Optional.of(approvedFilePath)
                .map(file -> Paths.get(containingJavaFile.getVirtualFile().getPath()).relativize(file))
                .map(Path::toString)
                .map(path -> {
                    return containingJavaFile.getVirtualFile().findFileByRelativePath(path);
                });

    }

    private ApprovalFile approvalFileFromMethod(PsiElement element, String packageName, PsiClass containingClazz) {

        // TODO add test to chech strict=false is useful when on PsiMethod/PsiClass and not PsiIdentifier.
        final PsiMethod containingMethod = PsiTreeUtil.getParentOfType(element, PsiMethod.class, false);
        final String fullClassName = getFullClassName(containingClazz);
        return containingMethod == null
                ? ApprovalFile.fromClass(packageName, fullClassName)
                : ApprovalFile.fromMethod(packageName, fullClassName, containingMethod.getName());
    }

    private String getFullClassName(PsiClass containingClazz) {
        String className = "";
        List<String> classesHierarchy = new ArrayList<>();
        PsiClass currentClass = containingClazz;
        while (currentClass != null) {
            classesHierarchy.add(0, currentClass.getName());
            currentClass = currentClass.getContainingClass();
        }
        final String fullClassName = classesHierarchy.stream().collect(Collectors.joining("."));
        return fullClassName;
    }


}
