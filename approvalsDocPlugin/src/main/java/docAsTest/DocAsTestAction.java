package docAsTest;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.command.UndoConfirmationPolicy;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

import java.util.Arrays;
import java.util.List;

/**
 * Encapsulate FilenameIndex to check it's not call during `update`
 * to avoid SlowOperation in production.
 */
public abstract class DocAsTestAction extends AnAction {
    protected final static Logger LOG = Logger.getInstance(DocAsTestAction.class);

    protected void traceActionEvent(AnActionEvent actionEvent) {
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
            System.out.println("DocAsTestAction.traceActionEvent offset " + offset);
            final PsiFile psiFile = actionEvent.getData(CommonDataKeys.PSI_FILE);
            final PsiElement elementAt = psiFile.findElementAt(offset);
            System.out.println("DocAsTestAction.traceActionEvent elementAt " + elementAt);
            final PsiElement parent = elementAt.getParent();
            System.out.println("DocAsTestAction.traceActionEvent parent " + parent);

        }

        try {
            for (DataKey<?> dataKey : dataKeys) {
                final Object data = actionEvent.getData(dataKey);

                if (data != null && data.getClass().isArray()) {
                    final Object[] array = (Object[]) data;
                    final String text = String.format("Common - %s:[%d] %s", dataKey.getName(), array.length, data);
                    System.out.println(text);
                    for (Object o : array) {
                        System.out.println(String.format("                    - %s", o));
                    }
                } else {
                    final String text = String.format("Common - %s: %s", dataKey.getName(), data);
                    System.out.println(text);
                }
            }
        } catch (Exception e) {
            System.out.println("traceActionEvent EXCEPTION " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

    }

    public static class SlowOperationPolicy {
        public void runSlowOperation() {
        }
    }

    private static SlowOperationPolicy defaultSlowOperationPolicy = new SlowOperationPolicy();
    private SlowOperationPolicy slowOperationPolicy;
    private UndoConfirmationPolicy undoConfirmationPolicy;

    public DocAsTestAction() {
        this(defaultSlowOperationPolicy, UndoConfirmationPolicy.DEFAULT);
    }

    public DocAsTestAction(SlowOperationPolicy slowOperationPolicy, UndoConfirmationPolicy undoConfirmationPolicy) {
        this.slowOperationPolicy = slowOperationPolicy;
        this.undoConfirmationPolicy = undoConfirmationPolicy;
    }

    public void setSlowOperationPolicy(SlowOperationPolicy slowOperationPolicy) {
        this.slowOperationPolicy = slowOperationPolicy;
    }

    public void setUndoConfirmationPolicy(UndoConfirmationPolicy undoConfirmationPolicy) {
        this.undoConfirmationPolicy = undoConfirmationPolicy;
    }

    protected UndoConfirmationPolicy getUndoConfirmationPolicy() {
        return undoConfirmationPolicy;
    }

    protected PsiFile[] getFilesByName(Project project, String fileName) {
        slowOperationPolicy.runSlowOperation();
        return FilenameIndex.getFilesByName(project, fileName, GlobalSearchScope.projectScope(project));
    }

    protected String getProjectBasePath(Project project) {
        return project.getBasePath();
    }
}
