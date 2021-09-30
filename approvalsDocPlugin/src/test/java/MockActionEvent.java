import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import org.mockito.Mockito;

class MockActionEvent extends AnActionEvent {

    public MockActionEvent() {
        super(null,
                Mockito.mock(DataContext.class),
                "Here",
                new Presentation(),
                Mockito.mock(ActionManager.class), 0);
    }

}
