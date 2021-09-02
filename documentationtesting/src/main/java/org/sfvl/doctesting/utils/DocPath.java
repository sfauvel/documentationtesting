package org.sfvl.doctesting.utils;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class DocPath {

    private final Path folder;
    private final String name;

    public DocPath(Class<?> clazz) {
        this(clazz.getPackage(),
                extractClassName(clazz));
    }

    public DocPath(Method method) {
        this(method.getDeclaringClass().getPackage(),
                extractMethodName(method));
    }

    public DocPath(Package classPackage, String name) {
        this(toPath(classPackage),
                name);
    }

    public DocPath(Path folder, String name) {
        this.folder = folder;
        this.name = name;
    }

    private static final String extractClassName(Class<?> clazz) {
        return clazz.getCanonicalName().replace(clazz.getPackage().getName() + ".", "");
    }

    private static final String extractMethodName(Method method) {
        return String.format("%s.%s", extractClassName(method.getDeclaringClass()), method.getName());
    }

    public OnePath approved() {
        return new OnePath(Config.DOC_PATH, folder, "_" + name, ".approved.adoc");
    }

    public OnePath received() {
        return new OnePath(Config.DOC_PATH, folder, "_" + name, ".received.adoc");
    }

    public OnePath test() {
        return new OnePath(Config.TEST_PATH, folder, name, ".java");
    }
    public OnePath resource() {
        return new OnePath(Config.RESOURCE_PATH, folder, name, ".adoc");
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
        return Paths.get(aPackage.getName().replace('.', File.separatorChar));
    }

    public static Path toPath(Class<?> clazz) {
        final Class<?> mainClass = new ClassFinder().getMainFileClass(clazz);
        return toPath(clazz.getPackage())
                .resolve(String.format("%s.java", mainClass.getSimpleName()));
    }

    public static String toAsciiDoc(Path path) {
        return path.toString().replace(File.separatorChar, '/');
    }

}
