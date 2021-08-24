package org.sfvl.doctesting.utils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.SourceRoot;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaModel;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassesOrder {
    static class ParsedClassRepository {

        private Path path;
        private SourceRoot sourceRoot;

        public void addSourceTree(Path path) {
            this.path = path;

            sourceRoot = new SourceRoot(Config.TEST_PATH);
            parsedFiles();
        }
        public void parsedFiles() {
//            try {
//                final Set<java.io.File> javaFile = Files.walk(path)
//                        .map(Path::toFile)
//                        .filter(java.io.File::isFile)
//                        .filter(f -> f.toString().endsWith(".java"))
//                        .collect(Collectors.toSet());
//                System.out.println("=========================");
//                javaFile.stream().forEach(System.out::println);
//                System.out.println("=========================");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }

        public int getLineNumber(String packageName, String className) {
            System.out.println("ParsedClassRepository.getLineNumber : " + packageName + " / " + className);

//            CompilationUnit cu = sourceRoot.parse(
//                    javaClass.getPackageName(),
//                    /* javaClass.getSimpleName() +*/ "ClassesOrderTest" + ".java");
////            final MyVisitor v = new MyVisitor(javaClass.getFullyQualifiedName());
            return 0;
        }

        public int getLineNumber(Class clazz) {
            final String packageName = clazz.getPackage().getName();
            System.out.println("ParsedClassRepository.getLineNumber : " + packageName + " / " + clazz.getName());
            final String fileAsText = clazz.getName().split("\\$")[0];

            System.out.println("ParsedClassRepository.getLineNumber " + fileAsText);
            final String[] splitName = fileAsText.split("\\.");

            final String fileName = splitName[splitName.length-1].replaceAll("\\.", "/") + ".java";
            System.out.println("ParsedClassRepository.getLineNumber " + fileName);

            CompilationUnit cu = sourceRoot.parse(packageName, fileName);

            final MyVisitorLineNumber v = new MyVisitorLineNumber(clazz);
            cu.accept(v, null);
            return v.line;
        }
    }

    static class MyVisitorLineNumber  extends VoidVisitorAdapter<Void> {
        private final Class clazz;
        int indent = 0;
        int line = -1;
        List<String> fullname = new ArrayList<>();

        public MyVisitorLineNumber(Class clazz) {

            this.clazz = clazz;
        }

        @Override
        public void visit(ClassOrInterfaceDeclaration n, Void v) {
//            System.out.println("MyVisitor.visit ("+indent+"): " + n.getName());
//            System.out.println("           line    : " + n.getBegin().get().line);
            fullname.add(n.getName().asString());
            final String collect = fullname.stream().collect(Collectors.joining("."));
            System.out.println("MyVisitorLineNumber.visit ("+indent+"):" + collect);
            System.out.println("   Search:" + clazz.getCanonicalName());
            final String fullNameToSearch = clazz.getPackage().getName() + "." + collect;
            System.out.println(String.format("%s = %s : %s", clazz.getCanonicalName(), fullNameToSearch, clazz.getCanonicalName().equals(fullNameToSearch)));
            if (clazz.getCanonicalName().equals(fullNameToSearch)) {
                System.out.println("*******************************  " + collect);
                line = n.getBegin().get().line;
                return;
//                System.out.println("MyVisitor.visit fullname:" + collect + "(" +n.getBegin().get().line+ ")");
//                line = n.getBegin().get().line;
            }
            indent++;
            super.visit(n, v);
            indent--;

            fullname.remove(fullname.size()-1);
//            if (classToExtract.getCanonicalName().equals(n.getFullyQualifiedName().get())) {
//                buffer.append(extractRange(classToExtract, n));
//            } else {
//                super.visit(n, buffer);
//            }
        }
    }

    PathProvider pathProvider = new PathProvider();

    private static JavaProjectBuilder builder;
    private static ParsedClassRepository parserClassBuilder;

    public ClassesOrder() {
        if (builder == null) {
            builder = createJavaProjectBuilderWithTestPath();
        }
        if (parserClassBuilder == null) {
            parserClassBuilder = createParsedeClassBuilderWithTestPath();
        }
    }

    static interface MyJavaModel {

        int getLineNumber();
    }

//    static class MyVisitor  extends VoidVisitorAdapter<Void> {
//        private final String fullyQualifiedName;
//        int indent = 0;
//        int line = -1;
//        List<String> fullname = new ArrayList<>();
//
//        public MyVisitor(String fullyQualifiedName) {
//
//            this.fullyQualifiedName = fullyQualifiedName;
//        }
//
//        @Override
//        public void visit(ClassOrInterfaceDeclaration n, Void v) {
//            System.out.println("MyVisitor.visit ("+indent+"): " + n.getName());
//            System.out.println("           line    : " + n.getBegin().get().line);
//            fullname.add(n.getName().asString());
//            final String collect = fullname.stream().collect(Collectors.joining("."));
//            if (fullyQualifiedName.equals("org.sfvl.doctesting.utils." + collect)) {
//                System.out.println("*********************************");
//                System.out.println("MyVisitor.visit fullname:" + collect + "(" +n.getBegin().get().line+ ")");
//                line = n.getBegin().get().line;
//            }
//            System.out.println("MyVisitor.visit fullname:" + collect);
//            indent++;
//            super.visit(n, v);
//            indent--;
//            fullname.remove(fullname.size()-1);
////            if (classToExtract.getCanonicalName().equals(n.getFullyQualifiedName().get())) {
////                buffer.append(extractRange(classToExtract, n));
////            } else {
////                super.visit(n, buffer);
////            }
//        }
//    }
    static class MyJavaClass implements MyJavaModel  {

        private final JavaClass javaClass;
        private final Class<?> encapsulatedClass;

//        public MyJavaClass(JavaClass javaClass) {
//            this.javaClass = javaClass;
//        }

        public MyJavaClass(JavaClass javaClass, Class<?> encapsulatedClass) {
            this.javaClass = javaClass;
            this.encapsulatedClass = encapsulatedClass;
        }

        @Override
        public int getLineNumber() {

            int parsedLine = parserClassBuilder.getLineNumber(encapsulatedClass);
//            SourceRoot sourceRoot = new SourceRoot(Config.TEST_PATH);
//            CompilationUnit cu = sourceRoot.parse(
//                    javaClass.getPackageName(),
//                   /* javaClass.getSimpleName() +*/ "ClassesOrderTest" + ".java");
//            final MyVisitor v = new MyVisitor(javaClass.getFullyQualifiedName());
//            cu.accept(v, null);
//            System.out.println("MyJavaClass.getLineNumber \n" +
//                    "class:" + javaClass.getName() + "\n" +
//                    "class:" + javaClass.getFullyQualifiedName() + "\n" +
//                    "line :" + javaClass.getLineNumber());
//            System.out.println(String.format("======\n%d / %d / %d: %s", parsedLine, javaClass.getLineNumber(), v.line, javaClass.getFullyQualifiedName()));
//            return v.line;//javaClass.getLineNumber();
//            System.out.println(String.format("======\n%d / %d: %s", parsedLine, javaClass.getLineNumber(), javaClass.getFullyQualifiedName()));
            System.out.println(String.format("======\n%d : %s", parsedLine, encapsulatedClass.getCanonicalName()));
            return parsedLine;
        }

        public String getName() {
//            return javaClass.getName();
            return encapsulatedClass.getSimpleName();
        }

    }

    static class MyJavaMethod implements MyJavaModel {

        private final JavaMethod javaMethod;

        public MyJavaMethod(JavaMethod javaMethod) {
            this.javaMethod = javaMethod;
        }

        @Override
        public int getLineNumber() {
            return javaMethod.getLineNumber();
        }

        public String getName() {
            return javaMethod.getName();
        }
    }
    public static interface EncapsulateDeclared<T> {
        MyJavaModel getJavaModel();

        int getLineNumber();

        String getName();

        T getEncapsulatedObject();
    }

    public abstract static class EncapsulateJavaModel<T, J extends MyJavaModel> implements EncapsulateDeclared<T> {
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
//            return 0;
            return javaModel.getLineNumber();
        }


    }

    public static class EncapsulateDeclaredClass extends EncapsulateJavaModel<Class<?>, MyJavaClass> {
        public EncapsulateDeclaredClass(Class<?> encapsulatedClass) {
            super(encapsulatedClass, new MyJavaClass(builder.getClassByName(encapsulatedClass.getName()), encapsulatedClass));
        }
        @Override
        public String getName() {
            return getJavaModel().getName();
        }
    }

    public static class EncapsulateDeclaredMethod extends EncapsulateJavaModel<Method, MyJavaMethod> {
        public EncapsulateDeclaredMethod(Method encapsulatedMethod) {
            super(encapsulatedMethod,
                    new MyJavaMethod(builder.getClassByName(encapsulatedMethod.getDeclaringClass().getName())
                            .getMethods().stream()
                            .filter(m -> encapsulatedMethod.getName().equals(m.getName()))
                            .findFirst().get()));
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

        parserClassBuilder.getLineNumber(clazz);
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


        final Stream<EncapsulateDeclared> sorted = hashMap.values().stream()
                .sorted(Comparator.comparingInt(EncapsulateDeclared::getLineNumber));
        final String collect1 = sorted.map(e -> e.getName() + ": " + e.getLineNumber())
                .collect(Collectors.joining("\n"));

        final String collect = hashMap.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue().getName() + "  " + e.getValue().getLineNumber())
                .collect(Collectors.joining("\n"));
        System.out.println("___________  " + clazz + "  _______\n");
        System.out.println("_______________________\n" + collect1 + "\n_______________________");

        return hashMap.values().stream()
                .sorted(Comparator.comparingInt(EncapsulateDeclared::getLineNumber));

    }

    private JavaProjectBuilder createJavaProjectBuilderWithTestPath() {
        JavaProjectBuilder builder = new JavaProjectBuilder();


        final Path testPath = pathProvider.getProjectPath().resolve(Config.TEST_PATH);
        builder.addSourceTree(testPath.toFile());
        return builder;
    }


    private ParsedClassRepository createParsedeClassBuilderWithTestPath() {
        ParsedClassRepository builder = new ParsedClassRepository();


        final Path testPath = pathProvider.getProjectPath().resolve(Config.TEST_PATH);
        builder.addSourceTree(testPath);
        return builder;
    }
}
