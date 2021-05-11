package org.sfvl.doctesting.utils;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class DocPath {

    private final Path folder;
    private final String name;

    public DocPath(Class<?> clazz) {
        this.folder = toPath(clazz.getPackage());
        this.name = extractClassName(clazz);
    }

    public DocPath(Method method) {
        final Class<?> clazz = method.getDeclaringClass();
        this.folder = toPath(clazz.getPackage());
        String className = extractClassName(clazz);
        this.name = String.format("%s.%s", className, method.getName());
    }

    private final String extractClassName(Class<?> clazz) {
        return clazz.getCanonicalName().replace(clazz.getPackage().getName() + ".", "");
    }

    public OnePath approved() {
        return new OnePath(Config.DOC_PATH, folder, name, ".approved.adoc");
    }

    public OnePath received() {
        return new OnePath(Config.DOC_PATH, folder, name, ".received.adoc");
    }

    public OnePath test() {
        return new OnePath(Config.TEST_PATH, folder, name, ".java");
    }

    public OnePath doc() {
        return new OnePath(Paths.get(""), folder, name, ".html");
    }

    public OnePath page() {
        return new OnePath(Config.DOC_PATH, folder, name, ".adoc");
    }

    public String name() {
        return name;
    }

    public static Path toPath(Package aPackage) {
        return Arrays.stream(aPackage.getName().split("\\."))
                .map(Paths::get)
                .reduce(Paths.get(""), Path::resolve);
    }

}
