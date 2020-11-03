package org.sfvl.doctesting;

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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
        JavaClass javaClass = builder.getClassByName(clazz.getCanonicalName());

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
        JavaClass javaClass = builder.getClassByName(clazz.getCanonicalName());

        JavaMethod method = javaClass.getMethod(methodName, argumentList, false);
        while (method == null && javaClass.getSuperJavaClass() != null) {
            javaClass = javaClass.getSuperJavaClass();
            method = javaClass.getMethod(methodName, argumentList, false);
        }
        return Optional.ofNullable(method).map(c -> c.getComment());
    }

    public static String classSource(Class<?> classToExtract) {
        return new CodeExtractorVisitor(classToExtract) {
            @Override
            public void visit(ClassOrInterfaceDeclaration n, StringBuffer buffer) {
                if (classToExtract.getName().equals(n.getFullyQualifiedName().get())) {
                    buffer.append(extractRange(classToExtract, n));
                }
            }
        }.source();
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

    public static class CodeExtractorVisitor extends VoidVisitorAdapter<StringBuffer> {
        final CompilationUnit cu;

        public CodeExtractorVisitor(Class<?> classToExtract) {
            SourceRoot sourceRoot = new SourceRoot(Paths.get("src/test/java"));

            cu = sourceRoot.parse(
                    classToExtract.getPackage().getName(),
                    classToExtract.getSimpleName() + ".java");

        }

        public String source() {
            StringBuffer javaCode = new StringBuffer();
            cu.accept(this, javaCode);
            return javaCode.toString();
        }

        public String extractRange(Class<?> classToExtract, NodeWithRange<?> n) {
            return extractFromFile(n, Paths.get("src/test/java",
                    classToExtract.getPackage().getName().replace(".", "/"),
                    classToExtract.getSimpleName() + ".java"));
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
                return code.substring(begin.column - 1);
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
            if (s1.contains("// " + end)) {
                inTag = false;
            }
            if (inTag) {
                buffer.append(s1 + "\n");
            }
            if (s1.contains("// " + begin)) {
                inTag = true;
            }
        }
        return buffer.toString();
    }

}
