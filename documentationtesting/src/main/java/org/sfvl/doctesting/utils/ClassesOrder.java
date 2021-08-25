package org.sfvl.doctesting.utils;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.SourceRoot;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassesOrder {
    static class ParsedClassRepository {

        private SourceRoot sourceRoot;

        public ParsedClassRepository(Path path) {
            sourceRoot = new SourceRoot(path);
        }

        public int getLineNumber(Class clazz) {
            CompilationUnit cu = getCompilationUnit(clazz);

            final MyVisitorLineNumber v = new MyVisitorLineNumber(clazz);
            cu.accept(v, null);
            return v.line;
        }

        public int getLineNumber(Method method) {
            final Class<?> clazz = method.getDeclaringClass();
            CompilationUnit cu = getCompilationUnit(clazz);

            final MyVisitorLineNumber v = new MyVisitorLineNumber(method);
            cu.accept(v, null);
            return v.line;
        }

        /**
         * Not able to read classes that are not in the public class because we are looking in java file that has the same name of this public class.
         * @param clazz
         * @return
         */
        private CompilationUnit getCompilationUnit(Class clazz) {
            final String packageName = clazz.getPackage().getName();
            final Class<?> mainFileClass = getMainFileClass(clazz);

            final String fileName = mainFileClass.getSimpleName() + ".java";
            return sourceRoot.parse(packageName, fileName);
        }

        private Class<?> getMainFileClass(Class<?> clazz) {
            Class mainFileClass = null;

            Class enclosingClass = clazz;
            while (enclosingClass != null) {
                mainFileClass = enclosingClass;
                enclosingClass = mainFileClass.getEnclosingClass();
            }
            return mainFileClass;
        }
    }

    static class MyVisitorMethodLineNumber  extends VoidVisitorAdapter<Void> {
        private final Class<?> clazz;
        private final Method method;
        int line = -1;

        public MyVisitorMethodLineNumber(Method method) {
            this.clazz = method.getDeclaringClass();
            this.method = method;
        }

        @Override
        public void visit(MethodDeclaration n, Void arg) {
            // TODO add full signature
            if (n.getNameAsString().equals(method.getName())) {
                line = n.getBegin().get().line;
                return;
            }
        }
    }

    static class MyVisitorLineNumber  extends VoidVisitorAdapter<Void> {
        private final Class<?> clazz;
        private final Method method;
        int indent = 0;
        int line = -1;
        List<String> fullname = new ArrayList<>();

        public MyVisitorLineNumber(Class<?> clazz) {
            this.clazz = clazz;
            this.method = null;
        }

        public MyVisitorLineNumber(Method method) {
            this.clazz = method.getDeclaringClass();
            this.method = method;
        }

        @Override
        public void visit(ClassOrInterfaceDeclaration n, Void v) {
            fullname.add(n.getName().asString());
            final String fullClassName = fullname.stream().collect(Collectors.joining("."));
            final String fullNameToSearch = clazz.getPackage().getName() + "." + fullClassName;
            if (clazz.getCanonicalName().equals(fullNameToSearch)) {
                if (method == null) {
                    line = n.getBegin().get().line;
                } else {
                    final MyVisitorMethodLineNumber myVisitorMethodLineNumber = new MyVisitorMethodLineNumber(method);
                    myVisitorMethodLineNumber.visit(n, v);
                    line = myVisitorMethodLineNumber.line;
                }
                return;
            }
            indent++;
            super.visit(n, v);
            indent--;

            fullname.remove(fullname.size()-1);
        }
    }

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
