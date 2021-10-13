import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mockito.Mockito;

class MockActionEvent extends AnActionEvent {

    private Project project;

    public MockActionEvent(Project project) {
        super(null,
                Mockito.mock(DataContext.class),
                "Here",
                new Presentation(),
                Mockito.mock(ActionManager.class), 0);
        this.project = project;
    }

    @Override
    public @Nullable Project getProject() {
        return project;
    }

    @Override
    public <T> @Nullable T getData(@NotNull DataKey<T> key) {
        return getDataContext().getData(key);
    }
}
