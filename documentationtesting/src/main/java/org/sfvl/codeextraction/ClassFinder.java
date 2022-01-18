package org.sfvl.codeextraction;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * It's help to find in project classes that match some criteria.
 */
public class ClassFinder {

    public List<Class<?>> classesWithAnnotatedMethod(Package packageToScan, Class<? extends Annotation> annotation) {
        return classesWithAnnotatedMethod(packageToScan, annotation, m -> true);
    }

    public List<Class<?>> classesWithAnnotatedMethod(Package packageToScan, Class<? extends Annotation> annotation, Predicate<Method> methodFilter) {
        final String prefix = packageToScan.getName();

        Reflections reflections = new Reflections(prefix, new MethodAnnotationsScanner());

        final Stream<Method> methodsAnnotatedWith = reflections.getMethodsAnnotatedWith(annotation).stream()
                .filter(methodFilter);

        return methodsAnnotatedWith
                .map(method -> getMainFileClass(method.getDeclaringClass()))
                .distinct()
                .sorted(Comparator.comparing(Class::getName))
                .collect(Collectors.toList());
    }

    public Class<?> getMainFileClass(Class<?> clazz) {
        return clazz.getEnclosingClass() != null
                ? getMainFileClass(clazz.getEnclosingClass())
                : clazz;
    }

}