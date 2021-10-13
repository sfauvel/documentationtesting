import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SwitchToJavaFileAction extends AnAction {

    private static final String SRC_PATH = "src/test/java";
    private static final String SRC_DOCS = "src/test/docs";

    public String getSrcDocs() {
        return SRC_DOCS;
    }

    public String getSrcPath() {
        return SRC_PATH;
    }

    protected String getMenuText() {
        return "Switch to approved file";
    }

    @Override
    public void update(AnActionEvent actionEvent) {
        final Optional<ReturnJavaFile> approvalFileOptional = getJavaFile(actionEvent);

        if (approvalFileOptional.isEmpty()) {
            actionEvent.getPresentation().setVisible(false);
            return;
        }

        actionEvent.getPresentation().setEnabled(true);
        actionEvent.getPresentation().setVisible(true);

        actionEvent.getPresentation().setText("Switch to java file");
    }

    @Override
    public void actionPerformed(AnActionEvent actionEvent) {
        final Optional<ReturnJavaFile> javaFileOptional = getJavaFile(actionEvent);
        if (javaFileOptional.isEmpty()) return;

        CommandProcessor.getInstance().executeCommand(
                actionEvent.getProject(),
                new SwitchToJavaFileAction.ApprovedRunnable(actionEvent.getProject(), javaFileOptional.get()),
                getMenuText(),
                "Approvals");
    }

    public Optional<Path> getJavaFilePath(Path projectPath, VirtualFile file) {
        return getFullApprovalFilePath(projectPath, file)
                .map(FullApprovalFilePath::fullPath);
    }

    private Optional<FullApprovalFilePath> getFullApprovalFilePath(Path projectPath, VirtualFile file) {
        final String prefixFolder = getSrcDocs();
        Pattern pattern = Pattern.compile("(" + Paths.get(projectPath + File.separator).toString() + "(.*" + File.separator + ")?)"
                + prefixFolder + File.separator
                + "(.*" + File.separator + ")?"
                + "(" + file.getName() + ")");
        Matcher matcher = pattern.matcher(file.getPath());
        if (!matcher.find()) {
            return Optional.empty();
        }

        final String projectRootPath = matcher.group(1);
        final Optional<String> packagePath = Optional.ofNullable(matcher.group(3));
        final String filePath = matcher.group(4);

        return ApprovalFile.valueOf(packagePath.orElse("") + filePath)
                .map(approvalFile -> approvalFile.toJava())
                .map(javaFile -> new FullApprovalFilePath(Paths.get(projectRootPath), Paths.get(getSrcPath()), javaFile));
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
        final Project project = actionEvent.getProject();

        final Optional<Path> javaFilePath = getJavaFilePath(Paths.get(getProjectBasePath(project)), virtualFile);

        final Path pathFromDocPath = Paths.get(getProjectBasePath(project)).resolve(getSrcDocs()).relativize(Paths.get(virtualFile.getPath()));

        final Optional<JavaFile> javaFile = ApprovalFile.valueOf(pathFromDocPath.toString())
                .map(ApprovalFile::toJava);

        if (javaFile.isEmpty()) {
            return Optional.empty();
        }
        final String fullPathToFile = getSrcPath() + File.separator + javaFile.get().getName();
        final PsiFile[] filesByName = FilenameIndex.getFilesByName(project, javaFile.get().getFileName(), GlobalSearchScope.projectScope(project));
        final Optional<PsiFile> first = Arrays.stream(filesByName)
//                .filter(file -> fullPathToFile.equals(Paths.get(getProjectBasePath(project)).relativize(Paths.get(file.getVirtualFile().getPath())).toString()))
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