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

    public static interface EncapsulateDeclared<T> {
        JavaModel getJavaModel();

        int getLineNumber();

        String getName();

        T getEncapsulatedObject();
    }

    public abstract static class EncapsulateJavaModel<T, J extends JavaModel> implements EncapsulateDeclared<T> {
        private final T encapsulatedObject;
        private final J javaModel;

        public EncapsulateJavaModel(T encapsulatedObject, J javaModel) {
            this.encapsulatedObject = encapsulatedObject;
            this.javaModel = javaModel;
        }

        public T getEncapsulatedObject() {
            return encapsulatedObject;
        }

        @Override
        public J getJavaModel() {
            return javaModel;
        }

        @Override
        public int getLineNumber() {
            return javaModel.getLineNumber();
        }


    }

    public static class EncapsulateDeclaredClass extends EncapsulateJavaModel<Class<?>, JavaClass> {
        public EncapsulateDeclaredClass(Class<?> encapsulatedClass) {
            super(encapsulatedClass, builder.getClassByName(encapsulatedClass.getName()));
        }
        @Override
        public String getName() {
            return getJavaModel().getName();
        }
    }

    public static class EncapsulateDeclaredMethod extends EncapsulateJavaModel<Method, JavaMethod> {
        public EncapsulateDeclaredMethod(Method encapsulatedMethod) {
            super(encapsulatedMethod,
                    builder.getClassByName(encapsulatedMethod.getDeclaringClass().getName())
                            .getMethods().stream()
                            .filter(m -> encapsulatedMethod.getName().equals(m.getName()))
                            .findFirst().get());
        }
        @Override
        public String getName() {
            return getJavaModel().getName();
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
