package org.sfvl.doctesting;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaModel;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
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

    public Stream<Method> getMethodsInOrder(List<Method> testMethods) {
        Map<String, Method> methodsByName = testMethods.stream().collect(Collectors.toMap(
                Method::getName,
                m -> m
        ));

        JavaProjectBuilder builder = createJavaProjectBuilderWithTestPath();

        Method firstMethod = testMethods.get(0);
        JavaClass javaClass = builder.getClassByName(firstMethod.getDeclaringClass().getName());

        return javaClass.getMethods().stream()
                .filter(m -> methodsByName.containsKey(m.getName()))
                .sorted(Comparator.comparingInt(JavaModel::getLineNumber))
                .map(m -> methodsByName.get(m.getName()));
    }

    interface EncapsulateDeclared {
        JavaModel getEncapsulatedModel();

        int getLineNumber();

        String getName();
    }

    class EncapsulateDeclaredClass implements EncapsulateDeclared {

        private final Class<?> encapsulatedClass;
        private final JavaClass javaClass;

        public EncapsulateDeclaredClass(Class<?> encapsulatedClass) {
            this.encapsulatedClass = encapsulatedClass;
            javaClass = builder.getClassByName(encapsulatedClass.getName());
        }

        public Class<?> getEncapsulatedClass() {
            return encapsulatedClass;
        }

        @Override
        public JavaModel getEncapsulatedModel() {
            return javaClass;
        }

        @Override
        public int getLineNumber() {
            return javaClass.getLineNumber();
        }

        @Override
        public String getName() {
            return javaClass.getName();
        }
    }

    class EncapsulateDeclaredMethod implements EncapsulateDeclared {

        private final Method encapsulatedMethod;
        private final JavaMethod javaMethod;

        public EncapsulateDeclaredMethod(Method encapsulatedMethod) {
            this.encapsulatedMethod = encapsulatedMethod;
            final JavaClass javaClass = builder.getClassByName(encapsulatedMethod.getDeclaringClass().getName());
            javaMethod = javaClass.getMethods().stream()
                    .filter(m -> encapsulatedMethod.getName().equals(m.getName()))
                    .findFirst().get();
        }

        public Method getEncapsulatedMethod() {
            return encapsulatedMethod;
        }

        @Override
        public JavaModel getEncapsulatedModel() {
            return javaMethod;
        }

        @Override
        public int getLineNumber() {
            return javaMethod.getLineNumber();
        }

        @Override
        public String getName() {
            return javaMethod.getName();
        }
    }

    public Stream<EncapsulateDeclared> getDeclaredInOrder(Class clazz) {
        return getDeclaredInOrder(clazz, m -> true, c -> true);
    }

    public Stream<EncapsulateDeclared> getDeclaredInOrder(Class clazz, Predicate<Method> methodFilter, Predicate<Class> classFilter) {
        final Class<?>[] declaredClasses = clazz.getDeclaredClasses();
        final Method[] declaredMethods = clazz.getDeclaredMethods();

        final Set<String> methodsNameInSource = builder.getClassByName(clazz.getName())
                .getMethods().stream()
                .map(JavaMethod::getName)
                .collect(Collectors.toSet());

        Map<String, EncapsulateDeclared> methodsByName = Arrays.stream(declaredMethods)
                .filter(methodFilter)
                .filter(m -> methodsNameInSource.contains(m.getName()))
                .collect(Collectors.toMap(
                        Method::getName,
                        m -> new EncapsulateDeclaredMethod(m)
                ));

        Map<String, EncapsulateDeclared> classesByName = Arrays.stream(declaredClasses)
                .filter(classFilter)
                .collect(Collectors.toMap(
                        Class::getSimpleName,
                        m -> new EncapsulateDeclaredClass(m)
                ));

        Map<String, EncapsulateDeclared> hashMap = new HashMap<>();
        hashMap.putAll(methodsByName);
        hashMap.putAll(classesByName);

        return hashMap.values().stream()
                .sorted(Comparator.comparingInt(EncapsulateDeclared::getLineNumber));

    }

    private JavaProjectBuilder createJavaProjectBuilderWithTestPath() {
        JavaProjectBuilder builder = new JavaProjectBuilder();

        final Path testPath = pathProvider.getProjectPath().resolve(Paths.get("src", "test", "java"));
        builder.addSourceTree(testPath.toFile());
        return builder;
    }
}
