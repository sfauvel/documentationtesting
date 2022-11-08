package org.sfvl.codeextraction;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithRange;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.SourceRoot;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CodeExtractor {
    public static final String TAG_BEGIN = ">>>";
    public static final String TAG_END = "<<<";
    private static final ClassFinder classFinder = new ClassFinder();
    private static Path TEST_PATH;
    private static Path SOURCE_PATH;

    public static void init(Path testPath, Path sourcePath) {
        TEST_PATH = testPath;
        SOURCE_PATH = sourcePath;
    }

    private static ParsedClassRepository getDefaultParsedClassRepository() {
        return new ParsedClassRepository(TEST_PATH, SOURCE_PATH);
    }

    public static Optional<String> getComment(Class<?> clazz) {
        return getComment(clazz, clazz);
    }

    public static Optional<String> getComment(Class<?> classFile, Class<?> clazz) {
        return Optional.ofNullable(getDefaultParsedClassRepository().getComment(classFile, clazz));
    }

    public static Optional<String> getComment(Class<?> classFile, Method testMethod) {
        return Optional.ofNullable(getDefaultParsedClassRepository().getComment(classFile, testMethod));
    }

    public static Optional<String> getComment(Method testMethod) {
        return Optional.ofNullable(getDefaultParsedClassRepository().getComment(testMethod));
    }

    public static Optional<String> getComment(Enum enumToExtract) {
        return getComment(enumToExtract.getClass(), enumToExtract);
    }

    public static Optional<String> getComment(Class<?> classFile, Enum testEnum) {
        return Optional.ofNullable(getDefaultParsedClassRepository().getComment(classFile, testEnum));
    }

    static class VisitorMethodCode extends ParsedClassRepository.MyMethodVisitor {
        private final StringBuffer buffer = new StringBuffer();
        private final RangeExtractor rangeExtractor;

        public VisitorMethodCode(Method method) {
            super(method);
            this.rangeExtractor = new RangeExtractor(method.getDeclaringClass());
        }

        @Override
        protected void actionOnMethod(MethodDeclaration n) {
            buffer.append(rangeExtractor.extract(n.getBody().get()));
        }
    }

    static class VisitorClassCode extends ParsedClassRepository.MyClassVisitor {
        private final StringBuffer buffer = new StringBuffer();
        private final RangeExtractor rangeExtractor;

        VisitorClassCode(Class<?> classToDetermineFile, Class<?> classToExtract) {
            super(classToExtract);
            this.rangeExtractor = new RangeExtractor(classToDetermineFile);
        }

        /**
         * We not use `node.toString()` because it formats code.
         *
         * @param node
         */
        @Override
        protected void actionOnClass(ClassOrInterfaceDeclaration node) {
            extractCode(node);
        }

        @Override
        protected void actionOnEnum(EnumDeclaration node) {
            extractCode(node);
        }

        private void extractCode(Node node) {
            final String extract = rangeExtractor.extract(node);
            node.getComment().ifPresent(c -> {
                Matcher matchSpaces = Pattern.compile("(\\s*)").matcher(extract);
                if (matchSpaces.find()) {
                    final String spaces = matchSpaces.group(0);
                    buffer.append(Arrays.stream(c.toString().split("\n"))
                            .map(line -> spaces + line)
                            .collect(Collectors.joining("\n", "", "\n")));
                } else {
                    buffer.append(c);
                }
            });
            buffer.append(extract);
        }
    }

    static class RangeExtractor {
        private final Path filePath;

        public RangeExtractor(Path filePath) {
            this.filePath = filePath;
        }

        public RangeExtractor(Class<?> classToDetermineFile) {
            this(TEST_PATH.resolve(CodePath.toPath(classToDetermineFile)));
        }

        protected String extract(NodeWithRange<?> n) {
            final int firstLine = n.getBegin().get().line;
            final int firstColumn = n.getEnd().get().column;
            final int lastLine = n.getEnd().get().line;
            final int lastColumn = n.getEnd().get().column;

            return extractFromFile(filePath, firstLine, lastLine, firstColumn, lastColumn);

            // With parser, some comments disappeared and code is reformatted.
//                    final String str = n.getBody()
//                            .map(body -> body.toString())
//                            .orElse("");
//                    javaCode.append(str);
        }

        private String extractFromFile(Path path, int firstLine, int lastLine, int firstColumn, int lastColumn) {
            try (Stream<String> lines = Files.lines(path)) {
                return extractCodeBetweenLines(lines, firstLine, lastLine, firstColumn, lastColumn);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private String extractCodeBetweenLines(Stream<String> lines, int firstLine, int lastLine, int firstColumn, int lastColumn) {
            Function<Stream<String>, Stream<String>> mapper = s -> {
                final List<String> linesList = s.collect(Collectors.toList());
                final int lastLineIndex = linesList.size() - 1;
                linesList.set(lastLineIndex, linesList.get(lastLineIndex).substring(0, lastColumn));
                return linesList.stream();
            };
            return extractCodeBetweenLines(lines, firstLine, lastLine, mapper);
        }

        private String extractCodeBetweenLines(Stream<String> lines, int firstLine, int lastLine, Function<Stream<String>, Stream<String>> mapper) {
            final Stream<String> linesInRange = lines
                    .skip(firstLine - 1)
                    .limit(lastLine - firstLine + 1);

            return mapper.apply(linesInRange).collect(Collectors.joining("\n"));
        }
    }

    public static String classSource(Class<?> classToExtract) {
        return classSource(classToExtract, classToExtract);
    }

    public static String enumSource(Class<?> classToIdentifySourceFile, Class<?> enumToExtract) {
        final ParserCode parserCode = new ParserCode(TEST_PATH);
        return parserCode.source(classToIdentifySourceFile, enumToExtract);
    }

    public static String enumSource(Class<?> enumToExtract) {
        return enumSource(enumToExtract, enumToExtract);
    }

    static class ParserCode {

        private final SourceRoot sourceRoot;

        public ParserCode(Path path) {
            this.sourceRoot = new SourceRoot(path);
        }

        public String source(Class<?> classToExtract) {
            return source(classFinder.getMainFileClass(classToExtract), classToExtract);
        }

        public String source(Class<?> classToDetermineFile, Class<?> classToExtract) {
            CompilationUnit cu = getCompilationUnit(classToDetermineFile);

            final VisitorClassCode myClassVisitor = new VisitorClassCode(classToDetermineFile, classToExtract);
            cu.accept(myClassVisitor, null);
            return myClassVisitor.buffer.toString();
        }

        public String source(Class<?> classOfTheMethod, String methodToExtract) {
            final List<Method> methods = Arrays.stream(classOfTheMethod.getDeclaredMethods())
                    .filter(m -> m.getName().equals(methodToExtract))
                    .collect(Collectors.toList());

            if (methods.isEmpty()) throw new RuntimeException("No method found with name '" + methodToExtract + "'");
            if (methods.size() > 1)
                throw new RuntimeException("More than one method with name '" + methodToExtract + "'");

            return source(methods.get(0));
        }

        public String source(Method methodToExtract) {
            final Class<?> classToDetermineFile = methodToExtract.getDeclaringClass();
            CompilationUnit cu = getCompilationUnit(classToDetermineFile);

            final VisitorMethodCode visitor = new VisitorMethodCode(methodToExtract);
            cu.accept(visitor, null);
            return visitor.buffer.toString();
        }

        private CompilationUnit getCompilationUnit(Class<?> classToDetermineFile) {
            CompilationUnit cu = sourceRoot.parse(
                    classToDetermineFile.getPackage().getName(),
                    CodePath.toFile(classToDetermineFile).toString());
            return cu;
        }

    }

    public static String classSource(Class<?> classToIdentifySourceFile, Class<?> classToExtract) {

        final ParserCode parserCode = new ParserCode(TEST_PATH);
        return parserCode.source(classToIdentifySourceFile, classToExtract);
    }

    public static String methodSource(Method methodToExtract) {
        final ParserCode parserCode = new ParserCode(TEST_PATH);
        return parserCode.source(methodToExtract);
    }

    public static String methodSource(Class<?> classToExtract, String methodToExtract) {
        final ParserCode parserCode = new ParserCode(TEST_PATH);
        return parserCode.source(classToExtract, methodToExtract);
    }

    public static String extractMethodBody(Method methodToExtract) {
        String code = methodSource(methodToExtract);
        return code.substring(code.indexOf("{") + 1, code.lastIndexOf("}"));
    }

    public static String extractMethodBody(Class<?> classToExtract, String methodToExtract) {
        String code = methodSource(classToExtract, methodToExtract);
        return code.substring(code.indexOf("{") + 1, code.lastIndexOf("}"));
    }

    public static String extractPartOfFile(Path path, String tag) {
        try {
            final String source = Files.lines(path).collect(Collectors.joining("\n"));
            return extractCodeBetween(source, TAG_BEGIN + tag, TAG_END + tag);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String extractPartOfCurrentMethod() {
        return extractPartOfCurrentMethod(Thread.currentThread().getStackTrace()[2], "");
    }

    public static String extractPartOfCurrentMethod(String suffix) {
        return extractPartOfCurrentMethod(Thread.currentThread().getStackTrace()[2], suffix);
    }

    private static String extractPartOfCurrentMethod(StackTraceElement callerStack, String suffix) {
        try {
            final Class<?> callerClass = Class.forName(callerStack.getClassName());
            final String callerMethod = callerStack.getMethodName();
            return extractPartOfMethod(callerClass, callerMethod, suffix);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
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

    public static class ArgumentCodeOfMethodCallerFromCode extends ArgumentCodeOfMethodCaller {
        public ArgumentCodeOfMethodCallerFromCode(StackTraceElement callerStack) {
            super(callerStack);
        }

        protected <T extends Node> void extractFromNode(T n, List<String> codes, Function<T, NodeList<Expression>> argumentGetter) {
            codes.addAll(argumentGetter.apply(n).stream()
                    .map(Node::toString)
                    .collect(Collectors.toList()));
        }
    }

    public static class ArgumentCodeOfMethodCallerFromSource extends ArgumentCodeOfMethodCaller {
        private final String fileName;

        public ArgumentCodeOfMethodCallerFromSource(StackTraceElement callerStack) {
            super(callerStack);
            final String declaringClass = callerStack.getClassName().split("\\$")[0];
            this.fileName = TEST_PATH.resolve(declaringClass.replace('.', '/') + ".java").toString();
        }

        protected <T extends Node> void extractFromNode(T n, List<String> codes, Function<T, NodeList<Expression>> argumentGetter) {
            codes.addAll(argumentGetter.apply(n).stream()
                    .map(arg -> new RangeExtractor(Paths.get(this.fileName)).extract(arg))
                    .collect(Collectors.toList()));
        }
    }

    public static abstract class ArgumentCodeOfMethodCaller extends VoidVisitorAdapter<List<String>> {

        private final int lineNumber;

        public ArgumentCodeOfMethodCaller(StackTraceElement callerStack) {
            lineNumber = callerStack.getLineNumber();
        }

        @Override
        public void visit(MethodDeclaration n, List<String> codes) {
            if (isLineInThatCode(n)) {
                super.visit(n, codes);
            }
        }

        private boolean isLineInThatCode(Node node) {
            return isLineInThatCode(node.getBegin().get().line, node.getEnd().get().line);
        }

        private boolean isLineInThatCode(int begin, int end) {
            return begin <= lineNumber && lineNumber <= end;
        }

        @Override
        public void visit(MethodCallExpr n, List<String> codes) {
            extractArgumentsCode(n, codes, MethodCallExpr::getArguments);
        }

        @Override
        public void visit(ObjectCreationExpr n, List<String> codes) {
            extractArgumentsCode(n, codes, ObjectCreationExpr::getArguments);
        }

        private <T extends Node> void extractArgumentsCode(T n, List<String> codes, Function<T, NodeList<Expression>> argumentGetter) {
            if (isLineInThatCode(n)) {
                extractFromNode(n, codes, argumentGetter);
            }
        }

        abstract protected <T extends Node> void extractFromNode(T n, List<String> codes, Function<T, NodeList<Expression>> argumentGetter);
    }

    public static List<String> extractParametersCode(Object... values) {
        return extractParametersCodeFromStackDepth(2);
    }

    public static List<String> extractParametersCodeAsItWrite(Object... values) {
        return extractParametersCodeFromStackDepth(2, true);
    }

    public static List<String> extractParametersCodeFromStackDepth(int stack_depth) {
        return extractParametersCodeFromStackDepth(stack_depth + 1, false);
    }

    public static List<String> extractParametersCodeFromStackDepth(int stack_depth, boolean asItWrite) {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        final StackTraceElement callerMethod = IntStream.range(stack_depth + 1, stackTrace.length)
                .mapToObj(index -> stackTrace[index])
                .filter(stack -> !stack.getMethodName().contains("access$0"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Stacktrace depth is out of bounds"));

        CompilationUnit cu = CodeExtractor.getDefaultParsedClassRepository().getCompilationUnit(callerMethod);

        final ArgumentCodeOfMethodCaller visitor = asItWrite
                ? new ArgumentCodeOfMethodCallerFromSource(callerMethod)
                : new ArgumentCodeOfMethodCallerFromCode(callerMethod);

        List<String> codes = new ArrayList<>();
        cu.accept(visitor, codes);

        return codes.stream().collect(Collectors.toList());
    }

}
