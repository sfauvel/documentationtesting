package org.sfvl.doctesting.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathProvider {
    /**
     * Get path of the project as a module.
     * To return the same value when executing from mvn command or from IDE, we return the absolute path.
     * @return
     */
    public Path getProjectPath() {
        return Paths.get("").toAbsolutePath();
    }

    /**
     * Return git root path.
     * This method is protected to allow a project to create a subclass and redefine git root path.
     *
     */
    public Path getGitRootPath() {
        System.out.println("PathProvider.getGitRootPath");
        final Path originalPath = getProjectPath();
        Path path = originalPath;
        System.out.println("PathProvider.getGitRootPath path.resolve(\".git\"):" + path.resolve(".git"));
        while (!path.resolve(".git").toFile().exists()) {
            path = path.getParent();
            System.out.println("PathProvider.getGitRootPath path.getParent():" + path);
            if (path == null) {
                throw new RuntimeException("No git repository found from parents of " + originalPath.toString());
            }
        }
        return path;
    }
}
