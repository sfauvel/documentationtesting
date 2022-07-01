package docAsTest;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

public class DocAsTestFilenameIndex {
    private static SlowOperationPolicy slowOperationPolicy = new SlowOperationPolicy();

    public static class SlowOperationPolicy {
        public void run() {
        }
    }

    public static void setSlowOperationPolicy(SlowOperationPolicy slowOperationPolicy) {
        DocAsTestFilenameIndex.slowOperationPolicy = slowOperationPolicy;
    }

    public static PsiFile[] getFilesByName(Project project, String fileName) {
        slowOperationPolicy.run();
        return FilenameIndex.getFilesByName(project, fileName, GlobalSearchScope.projectScope(project));
    }
}
