import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.intellij.openapi.diagnostic.Logger;

public abstract class SwitchToFileAction extends SwitchAction {
    private final static Logger LOG = Logger.getInstance(SwitchToFileAction.class);
    private final ApprovalFile.Status approvalType;

    protected SwitchToFileAction(ApprovalFile.Status approvalType) {
        this.approvalType = approvalType;
    }

    @Override
    protected boolean isFileToSwitchExist(AnActionEvent actionEvent) {
        return getApprovedPsiFile(actionEvent).isPresent();
    }

    @Override
    protected Optional<Runnable> getRunnableAction(@NotNull AnActionEvent actionEvent) {
        LOG.debug("getRunnableAction");
        final Optional<PsiFile> approvedPsiFile = getApprovedPsiFile(actionEvent);
        if (approvedPsiFile.isEmpty()) return Optional.empty();

        return Optional.of(new ApprovedRunnable(actionEvent.getProject(), approvedPsiFile.get()));
    }

    public Optional<Path> getApprovedFilePath(Path projectPath, VirtualFile file, ApprovalFile.Status status) {

        final Optional<FileBuilder> fileBuilder = FileBuilder.extractFileInfo(projectPath, file, getSrcPath());

        return fileBuilder.flatMap(fileBuilderGet ->
                Optional.of(JavaFile.fromClass(fileBuilderGet.packagePath.orElse(""), fileBuilderGet.filePath.replaceFirst(".java$", "")))
                        .map(javaFile -> javaFile.to(status))
                        .map(approvedFile -> new FullApprovalFilePath(Paths.get(fileBuilderGet.projectRootPath), Paths.get(getSrcDocs()), approvedFile))
                        .map(FullApprovalFilePath::fullPath));
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

    protected Optional<PsiFile> getApprovedPsiFile(Project project, Editor editor, PsiFile psiFile) {
        PsiElement element = Optional.ofNullable(editor)
                .map(e -> editor.getCaretModel().getOffset())
                .map(offset -> psiFile.findElementAt(offset))
                .orElse(psiFile);

        return getApprovedPsiFile(project, element);
    }

    protected Optional<PsiFile> getApprovedPsiFile(Project project, PsiElement element) {

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

        final Optional<Path> approvedFilePath = getApprovedFilePath(Paths.get(getProjectBasePath(project)), containingJavaFile.getVirtualFile(), ApprovalFile.Status.APPROVED);

        final PsiFile[] filesByName = FilenameIndex.getFilesByName(project, approvalFile.to(approvalType).getFileName(), GlobalSearchScope.projectScope(project));
        return Arrays.stream(filesByName)
                .filter(file -> Paths.get(file.getVirtualFile().getPath()).getParent().equals(approvedFilePath.map(Path::getParent).orElse(null)))
                .findFirst();
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

    protected String getProjectBasePath(Project project) {
        return project.getBasePath();
    }

}
