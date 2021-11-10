package org.sfvl.doctesting.utils;

import org.sfvl.docextraction.ParsedClassRepository;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassesOrder {

    PathProvider pathProvider = new PathProvider();

    private static ParsedClassRepository parserClassBuilder;

    public ClassesOrder() {
        if (parserClassBuilder == null) {
            parserClassBuilder = createParsedeClassBuilderWithTestPath();
        }
    }

    public static interface EncapsulateDeclared<T> {

        int getLineNumber();

        String getName();

        T getEncapsulatedObject();
    }

    public abstract static class EncapsulateJavaModel<T> implements EncapsulateDeclared<T> {
        private final T encapsulatedObject;

        public EncapsulateJavaModel(T encapsulatedObject) {
            this.encapsulatedObject = encapsulatedObject;
        }

        public T getEncapsulatedObject() {
            return encapsulatedObject;
        }

    }

    public static class EncapsulateDeclaredClass extends EncapsulateJavaModel<Class<?>> {
        public EncapsulateDeclaredClass(Class<?> encapsulatedClass) {
            super(encapsulatedClass);
        }
        @Override
        public String getName() {
            return getEncapsulatedObject().getSimpleName();
        }

        @Override
        public int getLineNumber() {
            return parserClassBuilder.getLineNumber(getEncapsulatedObject());
        }

    }

    public static class EncapsulateDeclaredMethod extends EncapsulateJavaModel<Method> {
        public EncapsulateDeclaredMethod(Method encapsulatedMethod) {
            super(encapsulatedMethod);
        }
        @Override
        public String getName() {
            return getEncapsulatedObject().getName();
        }

        @Override
        public int getLineNumber() {
            return parserClassBuilder.getLineNumber(getEncapsulatedObject());
        }
    }

    public Stream<EncapsulateDeclared> getDeclaredInOrder(Class clazz) {
        return getDeclaredInOrder(clazz, m -> true, c -> true);
    }

    public Stream<EncapsulateDeclared> getDeclaredInOrder(Class clazz, Predicate<Method> methodFilter, Predicate<Class> classFilter) {
        final Class<?>[] declaredClasses = clazz.getDeclaredClasses();
        final Method[] declaredMethods = clazz.getDeclaredMethods();

        Map<String, EncapsulateDeclared> methodsByName = Arrays.stream(declaredMethods)
                .filter(methodFilter)
                .filter(m -> !m.isSynthetic())
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

    private ParsedClassRepository createParsedeClassBuilderWithTestPath() {
        final Path testPath = pathProvider.getProjectPath().resolve(Config.TEST_PATH);
        ParsedClassRepository builder = new ParsedClassRepository(testPath);
        return builder;
    }
}
