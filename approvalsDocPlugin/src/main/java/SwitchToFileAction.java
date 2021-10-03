import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class SwitchToFileAction extends AnAction {
    private final String DOCS_PATH = "src/test/docs";
    private final ApprovalFile.Status approvalType;

    protected SwitchToFileAction(ApprovalFile.Status approvalType) {
        this.approvalType = approvalType;
    }

    @Override
    public void update(AnActionEvent actionEvent) {
        final Optional<@NotNull PsiFile> approvedPsiFile = getApprovedPsiFile(actionEvent);
        if (approvedPsiFile.isEmpty()) {
            actionEvent.getPresentation().setVisible(false);
            return;
        }
        actionEvent.getPresentation().setEnabled(true);
        actionEvent.getPresentation().setVisible(true);

        actionEvent.getPresentation().setText(getMenuText());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        final Optional<@NotNull PsiFile> approvedPsiFile = getApprovedPsiFile(anActionEvent);
        if (approvedPsiFile.isEmpty()) return;

        CommandProcessor.getInstance().executeCommand(
                anActionEvent.getProject(),
                new ApprovedRunnable(anActionEvent.getProject(), approvedPsiFile.get()),
                getMenuText(),
                "Approvals");
    }

    protected abstract String getMenuText();

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

    @NotNull
    protected Optional<@NotNull PsiFile> getApprovedPsiFile(AnActionEvent actionEvent) {
        final Project project = actionEvent.getProject();
        Editor editor = actionEvent.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = actionEvent.getData(CommonDataKeys.PSI_FILE);
        if (psiFile != null) {
            return getApprovedPsiFile(project, editor, psiFile);
        }

        PsiElement psiElement = actionEvent.getData(CommonDataKeys.PSI_ELEMENT);
        if (psiElement != null) {
            return getApprovedPsiFile(project, psiElement);
        }

        return Optional.empty();
    }

    @NotNull
    protected Optional<@NotNull PsiFile> getApprovedPsiFile(Project project, Editor editor, PsiFile psiFile) {
        PsiElement element = Optional.ofNullable(editor)
                .map(e -> editor.getCaretModel().getOffset())
                .map(offset -> psiFile.findElementAt(offset))
                .orElse(psiFile);

        return getApprovedPsiFile(project, element);
    }

    @NotNull
    protected Optional<@NotNull PsiFile> getApprovedPsiFile(Project project, PsiElement element) {

        PsiJavaFile containingJavaFile = (element instanceof PsiJavaFile)
                ? (PsiJavaFile) element
                : PsiTreeUtil.getParentOfType(element, PsiJavaFile.class);

        if (containingJavaFile == null) {
            return Optional.empty();
        }
        PsiMethod containingMethod = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        final String packageName = containingJavaFile.getPackageName();

        PsiClass containingClazz = PsiTreeUtil.getParentOfType(element, PsiClass.class);
        final String fullClassName = getFullClassName(containingClazz);

        ApprovalFile approvalFile = containingClazz == null
                ? ApprovalFile.fromClass(packageName, containingJavaFile.getVirtualFile().getNameWithoutExtension())
                : (containingMethod == null
                ? ApprovalFile.fromClass(packageName, fullClassName)
                : ApprovalFile.fromMethod(packageName, fullClassName, containingMethod.getName()));

        final String fullPathToFile = DOCS_PATH +
                (packageName.isEmpty() ? "" : File.separator + packageName.replace(".", File.separator));

        final @NotNull PsiFile[] filesByName = FilenameIndex.getFilesByName(project, approvalFile.to(approvalType).getFileName(), GlobalSearchScope.projectScope(project));
        return Arrays.stream(filesByName)
                .filter(file -> fullPathToFile.equals(Paths.get(getProjectBasePath(project)).relativize(Paths.get(file.getParent().getVirtualFile().getPath())).toString()))
                .findFirst();
    }

    @NotNull
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

    protected String getProjectBasePath(Project project) {
        return project.getBasePath();
    }

}
