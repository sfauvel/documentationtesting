package org.sfvl.doctesting.utils;

import java.io.File;
import java.nio.file.Path;

public class PathProvider {
    /**
     * Get path of the project as a module.
     * To be compatible in different system, a File is created from the path and then retransform to a path.
     * @return
     */
    public Path getProjectPath() {
        Path classesPath = new File(this.getClass().getClassLoader().getResource("").getPath()).toPath();
        // TODO We make getParent().getParent() because classes are in target/classes with Maven
        // If it's not the cas ewe must be able to change this.
        return classesPath.getParent().getParent();
    }

    /**
     * Return git root path.
     * This method is protected to allow a project to create a subclass and redefine git root path.
     *
     */
    public Path getGitRootPath() {

        final Path originalPath = getProjectPath();
        Path path = originalPath;
        while (!path.resolve(".git").toFile().exists()) {
            path = path.getParent();
            if (path == null) {
                throw new RuntimeException("No git repository found from parents of " + originalPath.toString());
            }
        }
        return path;
    }
}
