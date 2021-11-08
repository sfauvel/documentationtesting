package org.sfvl.doctesting.utils;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithRange;
import com.github.javaparser.utils.SourceRoot;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CodeExtractor {
    private static final String TAG_BEGIN = ">>>";
    private static final String TAG_END = "<<<";
    private static final ClassFinder classFinder = new ClassFinder();

    private static ParsedClassRepository getDefaultParsedClassRepository() {
        return new ParsedClassRepository(Config.TEST_PATH, Config.SOURCE_PATH);
    }

    public static String getComment(Class<?> clazz) {
        return getComment(clazz, clazz);
    }

    public static String getComment(Class<?> classFile, Class<?> clazz) {
        return Optional.ofNullable(getDefaultParsedClassRepository().getComment(classFile, clazz)).orElse("");
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
        private final Path sourcePath = Config.TEST_PATH;
        private final Class<?> classToDetermineFile;

        public RangeExtractor(Class<?> classToDetermineFile) {
            this.classToDetermineFile = classToDetermineFile;
        }
        protected String extract(NodeWithRange<?> n) {
            final int firstLine = n.getBegin().get().line;
            final int lastLine = n.getEnd().get().line;
            final Path filePath = sourcePath.resolve(DocPath.toPath(classToDetermineFile));
            return CodeExtractor.extractFromFile(filePath, firstLine, lastLine);

            // With parser, some comments disappeared and code is reformatted.
//                    final String str = n.getBody()
//                            .map(body -> body.toString())
//                            .orElse("");
//                    javaCode.append(str);
        }
    }

    public static String classSource(Class<?> classToExtract) {
        return classSource(classToExtract, classToExtract);
    }

    public static String enumSource(Class<?> classToIdentifySourceFile, Class<?> enumToExtract) {
        final ParserCode parserCode = new ParserCode(Config.TEST_PATH);
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
            final Method method = Arrays.stream(classOfTheMethod.getDeclaredMethods()).filter(
                            m -> m.getName().equals(methodToExtract)
                    ).findFirst()
                    .orElseThrow(() -> new RuntimeException("Unknown method"));
            return source(method);
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
                    DocPath.toFile(classToDetermineFile).toString());
            return cu;
        }

    }

    public static String classSource(Class<?> classToIdentifySourceFile, Class<?> classToExtract) {

        final ParserCode parserCode = new ParserCode(Config.TEST_PATH);
        return parserCode.source(classToIdentifySourceFile, classToExtract);
    }

    public static String methodSource(Method methodToExtract) {
        final ParserCode parserCode = new ParserCode(Config.TEST_PATH);
        return parserCode.source(methodToExtract);
    }

    public static String methodSource(Class<?> classToExtract, String methodToExtract) {
        final ParserCode parserCode = new ParserCode(Config.TEST_PATH);
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

    private static String extractFromFile(Path path, int firstLine, int lastLine) {
        try (Stream<String> lines = Files.lines(path)) {
            return extractCodeBetweenLines(lines, firstLine, lastLine);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String extractCodeBetweenLines(Stream<String> lines, int firstLine, int lastLine) {
        return lines
                .skip(firstLine - 1)
                .limit(lastLine - firstLine + 1)
                .collect(Collectors.joining("\n"));
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
