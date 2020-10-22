package org.sfvl.doctesting;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.SourceRoot;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaType;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CodeExtractor {
    static JavaProjectBuilder builder;

    /**
     * Init JavaProjectBuilder it's a bit long (more than 100ms).
     * We init it only once to avoid this low performances.
     */
    static {
        if (builder == null) {
            builder = new JavaProjectBuilder();
            builder.addSourceTree(new File("src/main/java"));
            builder.addSourceTree(new File("src/test/java"));
        }
    }

    public static JavaProjectBuilder getBuilder() {
        return builder;
    }

    public static String getComment(Class<?> clazz) {
        JavaClass javaClass = builder.getClassByName(clazz.getCanonicalName());

        return Optional.ofNullable(javaClass.getComment()).orElse("");
    }

    public static String getComment(Class<?> clazz, String methodName) {
        return getComment(clazz, methodName, Collections.emptyList());
    }

    public static String getComment(Class<?> clazz, String methodName, List<JavaType> argumentList) {
        JavaClass javaClass = builder.getClassByName(clazz.getCanonicalName());

        JavaMethod method = javaClass.getMethod(methodName, argumentList, false);
        while (method == null && javaClass.getSuperJavaClass() != null) {
            javaClass = javaClass.getSuperJavaClass();
            method = javaClass.getMethod(methodName, argumentList, false);
        }
        return Optional.ofNullable(method).map(c -> c.getComment()).orElse("");
    }

    public static String getCode(Class<?> clazz) {
        JavaClass javaClass = builder.getClassByName(clazz.getCanonicalName());
        return javaClass.getCodeBlock().replace("\t", "    ");
    }

    public static String extractMethodBody(Class<?> classToExtract, String methodToExtract) {
        SourceRoot sourceRoot = new SourceRoot(Paths.get("src/test/java"));

        CompilationUnit cu = sourceRoot.parse(
                classToExtract.getPackage().getName(),
                classToExtract.getSimpleName() + ".java");

        StringBuffer javaCode = new StringBuffer();
        cu.accept(new VoidVisitorAdapter<StringBuffer>() {
            @Override
            public void visit(MethodDeclaration n, StringBuffer arg) {
                if (methodToExtract.equals(n.getNameAsString())) {
                    final String str = n.getBody()
                            .map(body -> body.toString())
                            .orElse("");
                    javaCode.append(str);

                }
            }
        }, null);
        return javaCode.toString();
    }
}
