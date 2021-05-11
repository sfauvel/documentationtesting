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
        return rootPath.resolve(folder).resolve(name + suffix);
    }

    public Path folder() {
        return rootPath.resolve(folder);
    }

    public Path from(Class<?> classToRelativized) {
        final DocPath from = new DocPath(classToRelativized);
        return from.approved().folder().relativize(this.path());
    }
    public Path from(OnePath classToRelativized) {
        return classToRelativized.folder().relativize(this.path());
    }

    public Path to(OnePath classToRelativized) {
        return this.folder().relativize(classToRelativized.path());
    }
}
