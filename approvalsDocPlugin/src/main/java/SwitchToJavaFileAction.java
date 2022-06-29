import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import docAsTest.approvalFile.ApprovalFile;
import docAsTest.approvalFile.JavaFile;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SwitchToJavaFileAction extends SwitchAction {

    @Override
    protected String getMenuText() {
        return "Switch to java file";
    }

    @Override
    protected boolean isFileToSwitchExist(AnActionEvent actionEvent) {
        return getJavaFile(actionEvent).isPresent();
    }

    @Override
    protected Optional<Runnable> getRunnableAction(@NotNull AnActionEvent actionEvent) {
        LOG.debug("getRunnableAction");

        final Optional<ReturnJavaFile> javaFileOptional = getJavaFile(actionEvent);
        if (javaFileOptional.isEmpty()) return Optional.empty();

        return Optional.of(new SwitchToJavaFileAction.ApprovedRunnable(actionEvent.getProject(), javaFileOptional.get()));
    }

    public Optional<Path> getJavaFilePath(Path projectPath, VirtualFile file) {

        final Optional<FileBuilder> fileBuilder = FileBuilder.extractFileInfo(projectPath, file, getSrcDocs());

        return fileBuilder.flatMap(fileBuilderGet ->
                ApprovalFile.valueOf(fileBuilderGet.packagePath.orElse("") + fileBuilderGet.filePath)
                        .map(approvalFile -> approvalFile.toJava())
                        .map(javaFile -> new FullApprovalFilePath(Paths.get(fileBuilderGet.projectRootPath), Paths.get(getSrcPath()), javaFile))
                        .map(FullApprovalFilePath::fullPath));
    }

    public static class ReturnJavaFile {
        PsiJavaFile psiFile;
        JavaFile javaFile;

        public ReturnJavaFile(PsiJavaFile psiFile, JavaFile javaFile) {
            this.psiFile = psiFile;
            this.javaFile = javaFile;
        }

        public int getOffset() {
            final String className = this.javaFile.getClassName();
            final List<String> classNames = new ArrayList<>(Arrays.asList(className.split("\\.")));

            String firstClass = classNames.remove(0);
            final Optional<PsiClass> first = Optional.of(psiFile).stream()
                    .flatMap(c -> Arrays.stream(c.getClasses()))
                    .filter(c -> c.getName().equals(firstClass))
                    .findFirst();

            Optional<PsiClass> clazzFound = first;
            while (!classNames.isEmpty()) {
                final String firstInnerClass = classNames.remove(0);
                clazzFound = clazzFound.stream()
                        .flatMap(c -> Arrays.stream(c.getInnerClasses()))
                        .filter(c -> c.getName().equals(firstInnerClass))
                        .findFirst();
            }

            if (this.javaFile.getMethodName() == null) {
                return clazzFound.map(c -> c.getTextOffset()).orElse(0);
            }
            final int offset = clazzFound.stream()
                    .flatMap(c -> Arrays.stream(c.getMethods()))
                    .filter(m -> m.getName().equals(this.javaFile.getMethodName()))
                    .map(PsiMethod::getTextOffset)
                    .findFirst()
                    .orElse(0);
            return offset;
        }
    }

    private Optional<ReturnJavaFile> getJavaFile(AnActionEvent actionEvent) {
        final Project project = actionEvent.getProject();

        VirtualFile virtualFile = null;
        final PsiFile psiFile = actionEvent.getData(CommonDataKeys.PSI_FILE);
        if (psiFile != null) {
            virtualFile = psiFile.getVirtualFile();
        } else {
            final PsiElement psiElement = actionEvent.getData(CommonDataKeys.PSI_ELEMENT);
            if (psiElement != null) {
                virtualFile = psiElement.getContainingFile().getVirtualFile();
            }
        }
        if (virtualFile == null) {
            return Optional.empty();
        }

        final Optional<Path> javaFilePath = getJavaFilePath(Paths.get(getProjectBasePath(project)), virtualFile);

        Path pathFromDocPath = Paths.get(getProjectBasePath(project)).resolve(getSrcDocs()).relativize(Paths.get(virtualFile.getPath()));

        final Optional<JavaFile> javaFile = ApprovalFile.valueOf(pathFromDocPath.toString())
                .map(ApprovalFile::toJava);

        if (javaFile.isEmpty()) {
            return Optional.empty();
        }

//        final PsiFile[] filesByName = DocAsTestFilenameIndex.getFilesByName(project, javaFile.get().getFileName());
        final PsiFile[] filesByName = FilenameIndex.getFilesByName(project, javaFile.get().getFileName(), GlobalSearchScope.projectScope(project));

        final Optional<PsiFile> first = Arrays.stream(filesByName)
                .filter(file -> file.getVirtualFile().getPath().equals(javaFilePath.map(Path::toString).orElse(null)))
                .findFirst();
        return first.map(f -> new ReturnJavaFile((PsiJavaFile) f, javaFile.get()));
    }

    static class ApprovedRunnable implements Runnable {
        private final Project project;
        private ReturnJavaFile javaFile;

        ApprovedRunnable(Project project, ReturnJavaFile javaFile) {
            this.project = project;
            this.javaFile = javaFile;
        }

        @Override
        public void run() {
            ApplicationManager.getApplication().runWriteAction(() -> runAction());
        }

        private void runAction() {
            LOG.debug("ApprovedRunnable.runAction");
            final int offset = javaFile.getOffset();

            FileEditorManager.getInstance(project)
                    .openTextEditor(new OpenFileDescriptor(project, javaFile.psiFile.getVirtualFile(), offset), true);
        }

        // TODO remove this method when no more usage

    }

}