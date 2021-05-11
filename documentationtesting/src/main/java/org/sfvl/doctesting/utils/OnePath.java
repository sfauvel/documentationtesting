package org.sfvl.doctesting.utils;

import java.nio.file.Path;

public class OnePath {
    private final String suffix;
    private final Path rootPath;
    private final Path folder;
    private final String name;

    public OnePath(Path rootPath, Path folder, String name, String suffix) {
        this.name = name;
        this.folder = folder;
        this.suffix = suffix;
        this.rootPath = rootPath;
    }

    public Path path() {
        return rootPath.resolve(folder).resolve(fullname());
    }

    public Path folder() {
        return rootPath.resolve(folder);
    }

    public String fullname() {
        return name + suffix;
    }

    public Path from(Path pathToRelativized) {
        return pathToRelativized.relativize(this.path());
    }
    public Path from(Class<?> classToRelativized) {
        return from(new DocPath(classToRelativized).approved().folder());
    }
    public Path from(OnePath classToRelativized) {
        return classToRelativized.folder().relativize(this.path());
    }

    public Path to(OnePath classToRelativized) {
        return this.folder().relativize(classToRelativized.path());
    }

}
