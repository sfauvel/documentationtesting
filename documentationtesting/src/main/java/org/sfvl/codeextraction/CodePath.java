package org.sfvl.codeextraction;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CodePath {
    public static Path toPath(Package aPackage) {
        return Paths.get(aPackage.getName().replace('.', File.separatorChar));
    }

    public static Path toPath(Class<?> clazz) {
        return toPath(clazz.getPackage()).resolve(toFile(clazz));
    }

    public static Path toFile(Class<?> clazz) {
        final Class<?> mainClass = new ClassFinder().getMainFileClass(clazz);
        return Paths.get(String.format("%s.java", mainClass.getSimpleName()));
    }
}
