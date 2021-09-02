package org.sfvl.doctesting.utils;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.SourceRoot;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Extract information from source code.
 *
 * Extraction from method is a naive implementation.
 * We compare only class name without checking the scope (package).
 */
public class ParsedClassRepository {

    private static final ClassFinder classFinder = new ClassFinder();
    private List<SourceRoot> sourceRoots = new ArrayList<>();

    public ParsedClassRepository(Path... paths) {
        for (Path path : paths) {
            sourceRoots.add(new SourceRoot(path));
        }
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

    public String getComment(Class<?> clazz) {
        return getComment(clazz, clazz);
    }

    public String getComment(Class<?> classFile, Class<?> clazz) {
        CompilationUnit cu = getCompilationUnit(classFile);

        final MyVisitorComment v = new MyVisitorComment(clazz);
        cu.accept(v, null);
        return v.comment;
    }

    public String getComment(Method method) {
        return getComment(method.getDeclaringClass(), method);
    }

    public String getComment(Class<?> classFile, Method method) {
        CompilationUnit cu = getCompilationUnit(classFile);

        final MyVisitorComment v = new MyVisitorComment(method);
        cu.accept(v, null);
        return v.comment;
    }

    /**
     * Not able to read classes that are not in the public class because we are looking in java file that has the same name of this public class.
     *
     * @param clazz
     * @return
     */
    private CompilationUnit getCompilationUnit(Class clazz) {
        final String packageName = clazz.getPackage().getName();
        final Class<?> mainFileClass = classFinder.getMainFileClass(clazz);

        final String fileName = mainFileClass.getSimpleName() + ".java";
        for (SourceRoot sourceRoot : sourceRoots) {
            try {
                return sourceRoot.parse(packageName, fileName);
            } catch (ParseProblemException e) {
                // try with next path
            }
        }
        throw new RuntimeException(String.format("Enable to parse %s in package %s", fileName, packageName));

    }

    static class MyVisitorMethodLineNumber extends MyMethodVisitor {
        int line = -1;

        public MyVisitorMethodLineNumber(Method method) {
            super(method);
        }

        @Override
        protected void actionOnMethod(MethodDeclaration n) {
            line = n.getBegin().get().line;
        }
    }

    static class MyVisitorLineNumber extends MyClassVisitor {
        private final Method method;
        int line = -1;

        public MyVisitorLineNumber(Class<?> clazz) {
            super(clazz);
            this.method = null;
        }

        public MyVisitorLineNumber(Method method) {
            super(method.getDeclaringClass());
            this.method = method;
        }

        @Override
        protected void actionOnClass(ClassOrInterfaceDeclaration n) {
            if (method == null) {
                line = n.getBegin().get().line;
            } else {
                final MyVisitorMethodLineNumber myVisitorMethodLineNumber = new MyVisitorMethodLineNumber(method);
                myVisitorMethodLineNumber.visit(n, null);
                line = myVisitorMethodLineNumber.line;
            }
        }
    }

    static class MyVisitorComment extends MyClassVisitor {
        private final Method method;
        String comment;

        public MyVisitorComment(Class<?> clazz) {
            super(clazz);
            this.method = null;
        }

        public MyVisitorComment(Method method) {
            super(method.getDeclaringClass());
            this.method = method;
        }

        @Override
        protected void actionOnClass(ClassOrInterfaceDeclaration n) {
            if (method == null) {
                n.getJavadoc().ifPresent(doc -> comment = doc.getDescription().toText());
            } else {
                final MyVisitorCommentMethod myVisitorCommentMethod = new MyVisitorCommentMethod(method);
                myVisitorCommentMethod.visit(n, null);
                comment = myVisitorCommentMethod.comment;
            }

        }
    }

    static class MyVisitorCommentMethod extends MyMethodVisitor {
        private String comment;

        public MyVisitorCommentMethod(Method method, Class<?>... parameters) {
            super(method);
        }

        @Override
        protected void actionOnMethod(MethodDeclaration n) {
            n.getJavadoc().ifPresent(doc -> comment = doc.getDescription().toText());
        }
    }

    static abstract class MyMethodVisitor extends VoidVisitorAdapter<Void> {
        private final Class<?> clazz;
        private final Method method;

        public MyMethodVisitor(Method method) {
            this.clazz = method.getDeclaringClass();
            this.method = method;
        }

        @Override
        public void visit(MethodDeclaration n, Void arg) {
            if (!n.getNameAsString().equals(method.getName())) {
                return;
            }
            if (n.getParameters().size() != method.getParameterCount()) {
                return;
            }
            for (int i = 0; i < method.getParameterCount(); i++) {
                final com.github.javaparser.ast.body.Parameter paramJavaParser = n.getParameter(i);
                final Type type = paramJavaParser.getType();
                final java.lang.reflect.Parameter paramReflect = method.getParameters()[i];

                String typeName = type.toString();
                if (type.isClassOrInterfaceType()) {
                    typeName = type.asClassOrInterfaceType().getName().asString();
                }
                try {
                    if (!typeName.equals(paramReflect.getType().getSimpleName())) {
                        return;
                    }
                } catch (Exception e) {
                    throw e;
                }
            }
            actionOnMethod(n);
        }

        protected abstract void actionOnMethod(MethodDeclaration n);
    }

    static abstract class MyClassVisitor extends VoidVisitorAdapter<Void> {
        protected final Class<?> classToSearch;

        public MyClassVisitor(Class<?> clazz) {
            this.classToSearch = clazz;
            if (clazz.isLocalClass()) {
                /* TODO Local class is a classs defined in a method: https://docs.oracle.com/javase/tutorial/java/javaOO/localclasses.html
                * It's more complexe to define retrieve the class so we decide to not deal with it at first.
                * getCanonicalName return null
                * getName return class name but not contain method name.
                * Class name is prefix by $[Number] (ex: ParentClass$1MyClass, ParentClass$2MyClass
                * All local classes in a class have the same parent but we can have the same class name if it defines in different methods.
                * In that case, the name will be the same so the number behind '$' is incremented.
                * When
                * Class XXX {
                *   Class YYY {}
                * }
                * => XXX$YYY
                * Class XXX {
                *   void foo() {
                *       Class YYY {}
                *   }
                *   void bar() {
                *       Class ZZZ {}
                *       Class YYY {}
                *   }
                * }
                * => XXX$1YYY
                * => XXX$1ZZZ
                * => XXX$2YYY
                */
                throw new RuntimeException("Local classes are not handled");
            }
        }

        @Override
        public void visit(ClassOrInterfaceDeclaration n, Void v) {
            // Local classes haven't FullyQualifiedName and we do not deal with it.
            if (!n.getFullyQualifiedName().isPresent()) {
                return;
            }

            final String canonicalName = classToSearch.getCanonicalName();
            if (canonicalName.equals(n.getFullyQualifiedName().get())) {
                actionOnClass(n);
                return;
            }
            if (canonicalName.startsWith(n.getFullyQualifiedName().get())) {
                super.visit(n, v);
            }

        }

        protected abstract void actionOnClass(ClassOrInterfaceDeclaration n);
    }
}
