package org.sfvl.doctesting;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaModel;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ClassesOrder {
    PathProvider pathProvider = new PathProvider();

    private static JavaProjectBuilder builder;

    public ClassesOrder() {
        if (builder == null) {
            builder = createJavaProjectBuilderWithTestPath();
        }
    }

    public static Stream<Class> sort(List<Class> testClasses) {
        return new ClassesOrder().getClassesInOrder(testClasses);
    }

    public Stream<Class> getClassesInOrder(List<Class> testClasses) {
        if (testClasses.isEmpty()) {
            return Stream.empty();
        }

        Map<String, Class> classesByName = testClasses.stream().collect(Collectors.toMap(
                Class::getSimpleName,
                m -> m
        ));

        final Class enclosingClass = testClasses.get(0).getEnclosingClass();
        JavaClass javaClass = builder.getClassByName(enclosingClass.getName());
        final List<JavaClass> nestedClasses = javaClass.getNestedClasses();
        nestedClasses.sort(Comparator.comparingInt(JavaModel::getLineNumber));

        return nestedClasses.stream().map(c -> {
            return classesByName.get(c.getSimpleName());
        });

    }

    private JavaProjectBuilder createJavaProjectBuilderWithTestPath() {
        JavaProjectBuilder builder = new JavaProjectBuilder();

        final Path testPath = pathProvider.getProjectPath().resolve(Paths.get("src", "test", "java"));
        builder.addSourceTree(testPath.toFile());
        return builder;
    }
}
