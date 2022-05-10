import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.CommandProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public abstract class SwitchAction extends AnAction {

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
    protected abstract Optional<Runnable> getRunnableAction(@NotNull AnActionEvent actionEvent);
}
