package org.sfvl.doctesting.utils;

import org.sfvl.docextraction.ClassFinder;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * One of the essential points to create a documentation, is links between files.
 * It's even more important with documentation as test.
 * For a java method, there is the java file, the approved and received files, and the html file.
 * We need to easily create paths to those files.
 * We also need to reference one file from another one either to include it or to make a link to another page.
 *
 * This class helps to navigate between all that files.
 */
public class DocPath {

    private final Path packagePath;
    private final String name;

    public DocPath(Class<?> clazz) {
        this(clazz.getPackage(), extractClassName(clazz));
    }

    public DocPath(Method method) {
        this(method.getDeclaringClass().getPackage(), extractMethodName(method));
    }

    public DocPath(Package classPackage, String name) {
        this(toPath(classPackage), name);
    }

    public DocPath(Path packagePath, String name) {
        this.packagePath = packagePath;
        this.name = name;
    }

    public Path packagePath() {
        return packagePath;
    }

    /**
     * @return Name of the element associated with the doc.
     */
    public String name() {
        return name;
    }

    /**
     * The approved file.
     */
    public OnePath approved() {
        return new ApprovedPath(this);
    }
    /**
     * The received file.
     */
    public OnePath received() {
        return new ReceivedPath(this);
    }

    /**
     * The java source file.
     */
    public OnePath test() {
        return new TestPath(this);
    }
    public OnePath resource() {
        return new ResourcePath(this);
    }

    /**
     * The final rendered file.
     */
    public OnePath html() {
        return new HtmlPath(this);
    }

    /**
     * File to create a page in documentation.
     */
    public OnePath page() {
        return new PagePath(this);
    }

    private static final String extractClassName(Class<?> clazz) {
        return clazz.getCanonicalName().replace(clazz.getPackage().getName() + ".", "");
    }

    private static final String extractMethodName(Method method) {
        return String.format("%s.%s", extractClassName(method.getDeclaringClass()), method.getName());
    }

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

    public static String toAsciiDoc(Path path) {
        return path.toString().replace(File.separatorChar, '/');
    }

}
