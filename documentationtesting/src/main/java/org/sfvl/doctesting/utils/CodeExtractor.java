package org.sfvl.doctesting.utils;

import com.github.javaparser.Position;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithRange;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.SourceRoot;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaType;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class CodeExtractor {
    private static final String TAG_BEGIN = ">>>";
    private static final String TAG_END = "<<<";
    static JavaProjectBuilder builder;

    /**
     * Init JavaProjectBuilder it's a bit long (more than 100ms).
     * We init it only once to avoid this low performances.
     */
    static {
        if (builder == null) {
            final PathProvider pathProvider = new PathProvider();
            builder = new JavaProjectBuilder();
            builder.addSourceTree(pathProvider.getProjectPath().resolve(Paths.get("src/main/java")).toFile());
            builder.addSourceTree(pathProvider.getProjectPath().resolve(Paths.get("src/test/java")).toFile());
        }
    }

    public static JavaProjectBuilder getBuilder() {
        return builder;
    }

    public static String getComment(Class<?> clazz) {
        JavaClass javaClass = builder.getClassByName(clazz.getName());

        return Optional.ofNullable(javaClass.getComment()).orElse("");
    }

    public static Optional<String> getComment(Method testMethod) {
        return getComment(
                testMethod.getDeclaringClass(),
                testMethod.getName(),
                getJavaClasses(testMethod.getParameterTypes())
        );
    }

    public static Optional<String> getComment(Class<?> clazz, String methodName) {
        return getComment(clazz, methodName, Collections.emptyList());
    }

    public static Optional<String> getComment(Class<?> clazz, String methodName, List<JavaType> argumentList) {
        JavaClass javaClass = builder.getClassByName(clazz.getName());

        JavaMethod method = javaClass.getMethod(methodName, argumentList, false);
        while (method == null && javaClass.getSuperJavaClass() != null) {
            javaClass = javaClass.getSuperJavaClass();
            method = javaClass.getMethod(methodName, argumentList, false);
        }
        return Optional.ofNullable(method).map(c -> c.getComment());
    }

    public static String classSource(Class<?> classToExtract) {
        return classSource(classToExtract, classToExtract);
    }

    public static String classSource(Class<?> classToIdentifySourceFile, Class<?> classToExtract) {
        return new CodeExtractorVisitor(classToIdentifySourceFile) {

            @Override
            public void visit(ClassOrInterfaceDeclaration n, StringBuffer buffer) {
                if (classToExtract.getCanonicalName().equals(n.getFullyQualifiedName().get())) {
                    buffer.append(extractRange(classToExtract, n));
                } else {
                    super.visit(n, buffer);
                }
            }
        }.source();
    }

    public static String methodSource(Method methodToExtract) {
        return methodSource(methodToExtract.getDeclaringClass(), methodToExtract.getName());
    }

    public static String methodSource(Class<?> classToExtract, String methodToExtract) {
        return new CodeExtractorVisitor(classToExtract) {
            @Override
            public void visit(MethodDeclaration n, StringBuffer buffer) {
                if (methodToExtract.equals(n.getNameAsString())) {
                    buffer.append(extractRange(classToExtract, n.getBody().get()));
                }
            }
        }.source();
    }

    public static String extractMethodBody(Class<?> classToExtract, String methodToExtract) {
        String code = methodSource(classToExtract, methodToExtract);
        return code.substring(code.indexOf("{") + 1, code.lastIndexOf("}"));
    }

    public static List<Class<?>> enclosingClasses(Class<?> clazz) {
        final ArrayList<Class<?>> classes = new ArrayList<>();
        Class<?> enclosingClass = clazz;
        do {
            classes.add(0, enclosingClass);
            enclosingClass = enclosingClass.getEnclosingClass();
        } while (enclosingClass != null);
        return classes;
    }

    public static Class<?> getFirstEnclosingClassBefore(Method method, Class<?> clazzBefore) {
        return getFirstEnclosingClassBefore(method.getDeclaringClass(), clazzBefore);

    }

    public static Class<?> getFirstEnclosingClassBefore(Class<?> clazz, Class<?> clazzBefore) {
        if (clazz.equals(clazzBefore)) {
            return clazz;
        }
        Class<?> firstEnclosingClass = clazz;
        while (firstEnclosingClass.getEnclosingClass() != null && !firstEnclosingClass.getEnclosingClass().equals(clazzBefore)) {
            firstEnclosingClass = firstEnclosingClass.getEnclosingClass();
        }
        return firstEnclosingClass;
    }

    public static class CodeExtractorVisitor extends VoidVisitorAdapter<StringBuffer> {
        final CompilationUnit cu;
        private final Package classPackage;
        private final Class<?> classToDetermineFile;
        private final Path sourcePath;

        public CodeExtractorVisitor(Class<?> classToExtract) {
            sourcePath = Paths.get("src/test/java");
            classToDetermineFile = getFirstEnclosingClassBefore(classToExtract, null);
            classPackage = classToExtract.getPackage();

            SourceRoot sourceRoot = new SourceRoot(sourcePath);
            cu = sourceRoot.parse(
                    classPackage.getName(),
                    classToDetermineFile.getSimpleName() + ".java");
        }

        public String source() {
            StringBuffer javaCode = new StringBuffer();
            cu.accept(this, javaCode);
            return javaCode.toString();
        }

        public String extractRange(Class<?> classToExtract, NodeWithRange<?> n) {
            return extractFromFile(n, sourcePath.resolve(Paths.get(
                    classPackage.getName().replace(".", "/"),
                    classToDetermineFile.getSimpleName() + ".java")));
            // With parser, some comments disappeared and code is reformatted.
//                    final String str = n.getBody()
//                            .map(body -> body.toString())
//                            .orElse("");
//                    javaCode.append(str);
        }

        public String extractFromFile(NodeWithRange<?> n, Path path) {
            final Position begin = n.getBegin().get();
            final Position end = n.getEnd().get();

            try {
                final String collect = Files.lines(path).collect(Collectors.joining("\n"));
                final String code = Files.lines(path)
                        .skip(begin.line - 1)
                        .limit(end.line - begin.line + 1)
                        .collect(Collectors.joining("\n"));
                // Keep first characters to not remove indentation.
                return code; //code.substring(begin.column - 1);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public static List<JavaType> getJavaClasses(Class<?>... classes) {
        return Arrays.stream(classes)
                .map(clazz -> getBuilder().getClassByName(clazz.getCanonicalName()))
                .collect(Collectors.toList());
    }

    public static String extractPartOfMethod(Method method) {
        return extractPartOfMethod(method, "");
    }

    public static String extractPartOfMethod(Method method, String suffix) {
        return extractPartOfMethod(method.getDeclaringClass(), method.getName(), suffix);
    }

    public static String extractPartOfMethod(Class<?> clazz, String methodName) {
        return extractPartOfMethod(clazz, methodName, "");
    }

    public static String extractPartOfMethod(Class<?> clazz, String methodName, String suffix) {
        final String source = CodeExtractor.extractMethodBody(clazz, methodName);
        return extractCodeBetween(source, TAG_BEGIN + suffix, TAG_END + suffix);
    }

    public static String extractCodeBetween(String source, String begin, String end) {
        // Not compatible with JDK1.8
//                return s.lines()
//                .dropWhile(line -> line.contains("//"+ " tag::"))
//                .takeWhile(line -> line.contains("//"+ " end::"))
//                .collect(Collectors.joining("\n"));

        StringBuffer buffer = new StringBuffer();
        boolean inTag = false;
        for (String s1 : source.split("\n")) {
            if (s1.trim().equals("// " + end)) {
                inTag = false;
            }
            if (inTag) {
                buffer.append(s1 + "\n");
            }
            if (s1.trim().equals("// " + begin)) {
                inTag = true;
            }
        }
        return buffer.toString();
    }

}
