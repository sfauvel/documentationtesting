package tools;

import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;

import java.io.File;
import java.nio.file.Path;

public class CustomApprovalsExtension extends SimpleApprovalsExtension {

    /**
     * Get path of the project as a module.
     * To be compatible in different system, a File is created from the path and then retransform to a path.
     *
     * @return
     */
    public final Path getProjectPath() {
        Path classesPath = new File(this.getClass().getClassLoader().getResource("").getPath()).toPath();
        return classesPath.getParent().getParent();
    }
}
