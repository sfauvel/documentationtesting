package org.sfvl.doctesting.utils;

import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassFinder {
    public List<Class<?>> testClasses(Package packageToScan, Predicate<Method> methodFilter) {
        final String prefix = DocPath.toAsciiDoc(DocPath.toPath(packageToScan));
        Reflections reflections = new Reflections(prefix, new MethodAnnotationsScanner());

        final Stream<Method> methodsAnnotatedWith = reflections.getMethodsAnnotatedWith(Test.class).stream()
                .filter(methodFilter);

        return methodsAnnotatedWith
                .map(method -> getMainFileClass(method.getDeclaringClass()))
                .distinct()
                .sorted(Comparator.comparing(Class::getName))
                .collect(Collectors.toList());
    }

    public List<Class<?>> testClasses(Package packageToScan) {
        return testClasses(packageToScan, m -> true);
    }


    public Class<?> getMainFileClass(Class<?> clazz) {
        return clazz.getEnclosingClass() != null
                ? getMainFileClass(clazz.getEnclosingClass())
                : clazz;
    }

}