import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class SwitchAction extends AnAction {

    protected static final String DEFAULT_SRC_PATH = "src/test/java";
    protected static final String DEFAULT_SRC_DOCS = "src/test/docs";
    private static final String DOC_AS_TEST_PROPERTIES_FILENAME = "docAsTest.properties";
    private Properties properties = null;

    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        final Optional<Runnable> runnable = getRunnableAction(actionEvent);
        if (runnable.isEmpty()) return;

        CommandProcessor.getInstance().executeCommand(
                actionEvent.getProject(),
                runnable.get(),
                getMenuText(),
                "Approvals");
    }


    protected abstract String getMenuText();

    public String getSrcDocs() {
        return properties.getProperty("DOC_PATH", DEFAULT_SRC_DOCS);
    }

    public String getSrcPath() {
        return properties.getProperty("TEST_PATH", DEFAULT_SRC_PATH);
    }

    protected void loadProperties(Project project) {
        if (properties != null) {
            return;
        }
        properties = new Properties();
        final PsiFile[] propertiesByName = FilenameIndex.getFilesByName(project, DOC_AS_TEST_PROPERTIES_FILENAME, GlobalSearchScope.projectScope(project));

        // TODO we assume there is only one property file with this name in the project.
        // TODO We probably need to load property file by project.
        if (propertiesByName.length > 0) {
            loadProperties(propertiesByName[0].getVirtualFile());
        }
    }

    private void loadProperties(VirtualFile virtualFile) {
        if (properties == null) {
            properties = new Properties();
        }
        try (final InputStream inputStream = virtualFile.getInputStream()) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(AnActionEvent actionEvent) {
        if (!isFileToSwitchExist(actionEvent)) {
            actionEvent.getPresentation().setVisible(false);
            return;
        }

        actionEvent.getPresentation().setEnabled(true);
        actionEvent.getPresentation().setVisible(true);

        actionEvent.getPresentation().setText(getMenuText());
    }

    protected abstract boolean isFileToSwitchExist(AnActionEvent actionEvent);

    protected abstract Optional<Runnable> getRunnableAction(@NotNull AnActionEvent actionEvent);

    static class FileBuilder {

        public final String projectRootPath;
        public final Optional<String> packagePath;
        public final String filePath;

        FileBuilder(String projectRootPath, Optional<String> packagePath, String filePath) {
            this.projectRootPath = projectRootPath;
            this.packagePath = packagePath;
            this.filePath = filePath;
        }

        static public Optional<FileBuilder> extractFileInfo(final Path projectPath,
                                                            final VirtualFile file,
                                                            final String prefixFolder) {
            Pattern pattern = Pattern.compile("(" + Paths.get(projectPath + File.separator).toString() + "(.*" + File.separator + ")?)"
                    + prefixFolder + File.separator
                    + "(.*" + File.separator + ")?"
                    + "(" + file.getName() + ")");
            Matcher matcher = pattern.matcher(file.getPath());
            if (!matcher.find()) {
                return Optional.empty();
            }

            return Optional.of(new FileBuilder(
                    matcher.group(1),
                    Optional.ofNullable(matcher.group(3)),
                    matcher.group(4)
            ));

        }
    }
}
