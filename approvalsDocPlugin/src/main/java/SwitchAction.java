import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class SwitchAction extends AnAction {
    private final static Logger LOG = Logger.getInstance(SwitchAction.class);

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
        return DocAsTestStartupActivity.getSrcDocs();
    }

    public String getSrcPath() {
        return DocAsTestStartupActivity.getSrcPath();
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
