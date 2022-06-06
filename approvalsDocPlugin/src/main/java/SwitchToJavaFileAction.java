import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SwitchToJavaFileAction extends SwitchAction {
    private final static Logger LOG = Logger.getInstance(SwitchToJavaFileAction.class);

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
        // TODO What happened if virtualFile is still null ?

        final Optional<Path> javaFilePath = getJavaFilePath(Paths.get(getProjectBasePath(project)), virtualFile);

        final Path pathFromDocPath = Paths.get(getProjectBasePath(project)).resolve(getSrcDocs()).relativize(Paths.get(virtualFile.getPath()));

        final Optional<JavaFile> javaFile = ApprovalFile.valueOf(pathFromDocPath.toString())
                .map(ApprovalFile::toJava);

        if (javaFile.isEmpty()) {
            return Optional.empty();
        }

        final PsiFile[] filesByName = FilenameIndex.getFilesByName(project, javaFile.get().getFileName(), GlobalSearchScope.projectScope(project));
        final Optional<PsiFile> first = Arrays.stream(filesByName)
                .filter(file -> javaFilePath.map(Path::toString).get().equals(file.getVirtualFile().getPath()))
                .findFirst();
        return first.map(f -> new ReturnJavaFile((PsiJavaFile) f, javaFile.get()));
    }


    protected String getProjectBasePath(Project project) {
        return project.getBasePath();
    }

    static class ApprovedRunnable implements Runnable {
        private final PsiJavaFile javaFile;
        private final Project project;
        private final JavaFile javaClassFile;

        ApprovedRunnable(Project project, ReturnJavaFile javaFile) {
            this.project = project;
            this.javaFile = javaFile.psiFile;
            this.javaClassFile = javaFile.javaFile;
        }

        @Override
        public void run() {
            ApplicationManager.getApplication().runWriteAction(() -> runAction());
        }

        private void runAction() {
            System.out.println("ApprovedRunnable.runAction");
            final int offset = getOffset();

            FileEditorManager.getInstance(project)
                    .openTextEditor(new OpenFileDescriptor(project, javaFile.getVirtualFile(), offset), true);
        }

        public int getOffset() {
            final String className = javaClassFile.getClassName();
            final List<String> classNames = new ArrayList<>(Arrays.asList(className.split("\\.")));

            String firstClass = classNames.remove(0);
            final Optional<PsiClass> first = Optional.of(this.javaFile).stream()
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

            if (javaClassFile.getMethodName() == null) {
                return clazzFound.map(c -> c.getTextOffset()).orElse(0);
            }
            final int offset = clazzFound.stream()
                    .flatMap(c -> Arrays.stream(c.getMethods()))
                    .filter(m -> m.getName().equals(javaClassFile.getMethodName()))
                    .map(PsiMethod::getTextOffset)
                    .findFirst()
                    .orElse(0);
            return offset;
        }

    }

}