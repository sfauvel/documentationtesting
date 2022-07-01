import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import docAsTest.DocAsTestAction;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public abstract class SwitchAction extends DocAsTestAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        LOG.debug("SwitchAction.actionPerformed " + this.getClass().getName());
        traceActionEvent(actionEvent);

        final Optional<Runnable> runnable = getRunnableAction(actionEvent);
        if (runnable.isEmpty()) return;

        CommandProcessor.getInstance().executeCommand(
                actionEvent.getProject(),
                runnable.get(),
                getMenuText(),
                "Approvals");
    }


    protected abstract String getMenuText();

    @Override
    public void update(AnActionEvent actionEvent) {
        LOG.debug("SwitchAction.update " + this.getClass().getName());
        traceActionEvent(actionEvent);

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

}
