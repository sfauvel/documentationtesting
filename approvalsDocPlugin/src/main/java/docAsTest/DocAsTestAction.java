package docAsTest;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.command.UndoConfirmationPolicy;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import docAsTest.approvalFile.ApprovalFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Encapsulate FilenameIndex to check it's not call during `update`
 * to avoid SlowOperation in production.
 */
public abstract class DocAsTestAction extends AnAction {
    protected final static Logger LOG = Logger.getInstance(DocAsTestAction.class);

    private boolean traceActionEvent = false;

    public void setTraceActionEvent(boolean traceActionEvent) {
        this.traceActionEvent = traceActionEvent;
    }

    public String getSrcDocs() {
        return DocAsTestStartupActivity.getSrcDocs();
    }

    public String getSrcPath() {
        return DocAsTestStartupActivity.getSrcPath();
    }

    protected void traceActionEvent(AnActionEvent actionEvent) {
        if (!traceActionEvent) return;

        final List<DataKey<?>> dataKeys = Arrays.asList(
                CommonDataKeys.EDITOR,
                CommonDataKeys.PSI_FILE,
                CommonDataKeys.PSI_ELEMENT,
                CommonDataKeys.NAVIGATABLE,
                CommonDataKeys.NAVIGATABLE_ARRAY,
                CommonDataKeys.VIRTUAL_FILE,
                CommonDataKeys.VIRTUAL_FILE_ARRAY);

        final Editor editor = actionEvent.getData(CommonDataKeys.EDITOR);
        if (editor != null) {
            final int offset = editor.getCaretModel().getOffset();
            LOG.debug("DocAsTestAction.traceActionEvent offset " + offset);
            final PsiFile psiFile = actionEvent.getData(CommonDataKeys.PSI_FILE);
            final PsiElement elementAt = psiFile.findElementAt(offset);
            LOG.debug("DocAsTestAction.traceActionEvent elementAt " + elementAt);
            final PsiElement parent = elementAt.getParent();
            LOG.debug("DocAsTestAction.traceActionEvent parent " + parent);

        }

        try {
            for (DataKey<?> dataKey : dataKeys) {
                final Object data = actionEvent.getData(dataKey);

                if (data != null && data.getClass().isArray()) {
                    final Object[] array = (Object[]) data;
                    final String text = String.format("Common - %s:[%d] %s", dataKey.getName(), array.length, data);
                    LOG.debug(text);
                    for (Object o : array) {
                        LOG.debug(String.format("                    - %s", o));
                    }
                } else {
                    final String text = String.format("Common - %s: %s", dataKey.getName(), data);
                    LOG.debug(text);
                }
            }
        } catch (Exception e) {
            LOG.debug("traceActionEvent EXCEPTION " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

    }

    private UndoConfirmationPolicy undoConfirmationPolicy;

    public DocAsTestAction() {
        this(UndoConfirmationPolicy.DEFAULT);
    }

    public DocAsTestAction(UndoConfirmationPolicy undoConfirmationPolicy) {
        this.undoConfirmationPolicy = undoConfirmationPolicy;
    }

    public void setUndoConfirmationPolicy(UndoConfirmationPolicy undoConfirmationPolicy) {
        this.undoConfirmationPolicy = undoConfirmationPolicy;
    }

    protected UndoConfirmationPolicy getUndoConfirmationPolicy() {
        return undoConfirmationPolicy;
    }

    protected PsiFile[] getFilesByName(Project project, String fileName) {
        return DocAsTestFilenameIndex.getFilesByName(project, fileName);
    }

    protected String getProjectBasePath(Project project) {
        return project.getBasePath();
    }



    // ////////////////////:
    @NotNull
    protected Optional<VirtualFile> getApprovedVirtualFile(Project project, PsiElement element, ApprovalFile.Status approvalType) {
        // TODO add test to chech strict=false is useful when on PsiMethod/PsiClass and not PsiIdentifier.
        PsiJavaFile containingJavaFile = getPsiJavaFile(element);

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
                .resolve(approvalFile.to(approvalType).getName());

        return Optional.of(approvedFilePath)
                .map(file -> Paths.get(containingJavaFile.getVirtualFile().getPath()).relativize(file))
                .map(Path::toString)
                .map(path -> {
                    return containingJavaFile.getVirtualFile().findFileByRelativePath(path);
                });
    }

    @Nullable
    public PsiJavaFile getPsiJavaFile(PsiElement element) {
        return (element instanceof PsiJavaFile)
                ? (PsiJavaFile) element
                : PsiTreeUtil.getParentOfType(element, PsiJavaFile.class, false);
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
