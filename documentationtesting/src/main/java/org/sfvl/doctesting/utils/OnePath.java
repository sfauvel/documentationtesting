package org.sfvl.doctesting.utils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class OnePath {
    private final Class<?> clazz;
    private final String suffix;
    private final Path rootPath;

    public OnePath(Class<?> clazz, Path rootPath, String suffix) {
        this.clazz = clazz;
        this.suffix = suffix;
        this.rootPath = rootPath;
    }

    public Path path() {
        final Path path = OnePath.toPath(clazz);
        return rootPath.resolve(path + suffix);
    }

    public Path folder() {
        return rootPath.resolve(toPath(clazz.getPackage()));
    }

    public static Path toPath(Class<?> aClass) {
        return toPath(aClass, "", "");
    }

    public static Path toPath(Class<?> aClass, String prefix, String suffix) {
        return toPath(aClass.getPackage()).resolve(prefix + aClass.getSimpleName() + suffix);
    }

    public static Path toPath(Package aPackage) {
        return Arrays.stream(aPackage.getName().split("\\."))
                .map(Paths::get)
                .reduce(Paths.get(""), Path::resolve);
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
