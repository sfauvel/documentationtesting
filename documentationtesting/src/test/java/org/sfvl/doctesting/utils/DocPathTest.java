package org.sfvl.doctesting.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sfvl.docformatter.Formatter;
import org.sfvl.docformatter.asciidoc.AsciidocFormatter;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.ClassToDocument;
import org.sfvl.doctesting.junitextension.FindLambdaMethod;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.doctesting.sample.MyClass;
import org.sfvl.samples.MyTest;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Function;

@ClassToDocument(clazz = DocPath.class)
@DisplayName(value = "Document path")
public class DocPathTest {
    @RegisterExtension
    static ApprovalsExtension doc = new SimpleApprovalsExtension();

    final Formatter formatter = new AsciidocFormatter();

    @Test
    public void available_paths_from_DocPath() {

        // >>>1
        Class<?> clazz = MyTest.class;
        final DocPath docPath = new DocPath(clazz);
        // <<<1

        doc.write(String.format("`%s` contains the information defining the location of the item to be documented.", DocPath.class.getSimpleName()),
                "It's not a real path but just the location in the tree of documents.",
                "From this class, we can generate the real paths to the different kinds of associated documents.",
                "",
                String.format("We can create a `%s` the code below (where `%s` is declared in package `%s`).",
                        DocPath.class.getSimpleName(), clazz.getSimpleName(), clazz.getPackage().getName()),
                formatter.sourceCodeBuilder("java")
                        .source(CodeExtractor.extractPartOfCurrentMethod("1"))
                        .build(),
                ""
        );

        Function<OnePath, Path> functionToPath = onePath ->
                // >>>2
                onePath.path()
                // <<<2
                ;


        doc.write("[%autowidth]",
                "[%header]",
                "|====",
                String.format("| Kind of document | Method called %s | Description", CodeExtractor.extractPartOfCurrentMethod("2").trim()),
                linePath(docPath, DocPath::page, functionToPath),
                linePath(docPath, DocPath::approved, functionToPath),
                linePath(docPath, DocPath::received, functionToPath),
                linePath(docPath, DocPath::test, functionToPath),
                linePath(docPath, DocPath::html, functionToPath),

                "|====");
    }

    private String linePath(DocPath docPath, Function<DocPath, OnePath> methodOnDocPath, Function<OnePath, Path> functionToPath) {
        final CallsRecorder recorder = new CallsRecorder();

        final DocPath spyDocPath = addSpyRecorderOn(docPath, recorder);

        final OnePath onePath = methodOnDocPath.apply(spyDocPath);
        final String methodCalledOnDocPath = recorder.lastCall();

        Method lastMethod = recorder.getLastMethod();
        final Optional<String> comment = CodeExtractor.getComment(lastMethod);

        return String.format("a| %s | %s | %s", methodCalledOnDocPath, DocPath.toAsciiDoc(functionToPath.apply(onePath)), comment.orElse(""));

    }

    @Test
    public void path_by_type() {

        // >>>1
        final DocPath docPath = new DocPath(MyTest.class);
        // <<<1

        Class<?> clazz = MyTest.class;
        final Class<?> classToCompare = DocPathTest.class;
        final OnePath relativeToApproved = new DocPath(classToCompare).approved();

        doc.write(
                "DocPath is created with this code:",
                formatter.sourceCodeBuilder()
                        .source(CodeExtractor.extractPartOfCurrentMethod("1").trim())
                        .build(),
                "",
                "Note that `" + clazz.getSimpleName() + "` class is declared in package `" + clazz.getPackage().getName() + "`.",
                ""
        );

        doc.write("",
                String.format("We also used `%s` class to display `from` and `to` results with approved file (`%s`).",
                        classToCompare.getCanonicalName(),
                        DocPath.toAsciiDoc(relativeToApproved.path())),
                "");


        final CallsRecorder recorder = new CallsRecorder();
        final DocPath spyDocPath = addSpyRecorderOn(docPath, recorder);
        final String name = spyDocPath.name();
        final String methodCalledOnDocPath = recorder.lastCall();

        doc.write("[%autowidth]",
                "[%header]",
                "|====",
                "| Method | Result",
                String.format("| %s | %s", methodCalledOnDocPath, name),
                "|====",
                "");

        doc.write("[%autowidth]",
                "[%header]",
                "|====",
                "| Code | Method | Result",
                line(docPath, DocPath::page, relativeToApproved),
                line(docPath, DocPath::approved, relativeToApproved),
                line(docPath, DocPath::received, relativeToApproved),
                line(docPath, DocPath::test, relativeToApproved),
                line(docPath, DocPath::resource, relativeToApproved),
                line(docPath, DocPath::html, relativeToApproved),
                "|====");
    }

    @Test
    public void path_by_type_with_method() {

        // >>>1
        final DocPath docPath = new DocPath(FindLambdaMethod.getMethod(MyTest::test_A));
        // <<<1

        Class<?> clazz = MyTest.class;
        final Class<?> classToCompare = DocPathTest.class;
        final OnePath relativeToApproved = new DocPath(classToCompare).approved();

        doc.write(
                "DocPath is created with this code:",
                formatter.sourceCodeBuilder()
                        .source(CodeExtractor.extractPartOfCurrentMethod("1").trim())
                        .build(),
                "",
                "Note that `" + clazz.getSimpleName() + "` class is declared in package `" + clazz.getPackage().getName() + "`.",
                ""
        );

        doc.write("",
                String.format("We also used `%s` class to display `from` and `to` results with approved file (`%s`).",
                        classToCompare.getCanonicalName(),
                        DocPath.toAsciiDoc(relativeToApproved.path())),
                "");


        final CallsRecorder recorder = new CallsRecorder();
        final DocPath spyDocPath = addSpyRecorderOn(docPath, recorder);
        final String name = spyDocPath.name();
        final String methodCalledOnDocPath = recorder.lastCall();

        doc.write("[%autowidth]",
                "[%header]",
                "|====",
                "| Method | Result",
                String.format("| %s | %s", methodCalledOnDocPath, name),
                "|====",
                "");

        doc.write("[%autowidth]",
                "[%header]",
                "|====",
                "| Code | Method | Result",
                line(docPath, DocPath::page, relativeToApproved),
                line(docPath, DocPath::approved, relativeToApproved),
                line(docPath, DocPath::received, relativeToApproved),
                line(docPath, DocPath::test, relativeToApproved),
                line(docPath, DocPath::html, relativeToApproved),
                "|====");
    }

    @Test
    public void nested_class() {
        final Class<?> clazz = MyClass.MySubClass.class;

        doc.write(String.format("Name for nested class `%s` is `%s`.",
                        clazz.getName(),
                        new DocPath(clazz).name()),
                "",
                ""
        );

        final Method method = FindLambdaMethod.getMethod(MyClass.MySubClass::doSomething);
        doc.write(String.format("Name for method `%s` in nested class `%s` is `%s`.",
                        method.getName(),
                        method.getDeclaringClass().getName(),
                        new DocPath(method).name()),
                ""
        );
    }

    /**
     * Path in asciidoc files must used '/' independently of operating system and file separator.
     *
     * It's important to always generate the same reference file (.adoc) because we compare it with the last generated one.
     * Otherwise, the test could fail when executed on another operating system.
     */
    @Test
    public void make_path_independent_of_operating_system() {
        // >>>
        Path path = Paths.get("src", "main", "java");
        String asciiDocPath = DocPath.toAsciiDoc(path);
        // <<<
        doc.write(formatter.sourceCode(CodeExtractor.extractPartOfCurrentMethod()),
                String.format("*asciiDocPath = %s*", asciiDocPath));
    }

    class CallsRecorder<T extends Object> implements Answer<T> {
        String lastCall = "";
        Method lastMethod = null;
        @Override
        public T answer(InvocationOnMock a) throws Throwable {
            final Object result = a.callRealMethod();

//                final Object[] arguments = a.getArguments();
//                String parameters = Arrays.stream(arguments).map(v -> "" + v).collect(Collectors.joining(", "));
//                lastCall = a.getMethod().getName() + "(" + parameters + ")";
            lastMethod = a.getMethod();
            lastCall = lastMethod.getName() + "()";
            return (T) result;
        }

        String lastCall() {
            return lastCall;
        }

        Method getLastMethod() {
            return lastMethod;
        }
    }

    @Nested
    @DisplayName(value = "Method 'toPath'")
    class MethodToPath {
        @Test
        public void path_from_a_package() {
            // >>>
            final Class<?> clazz = org.sfvl.samples.MyTest.class;
            final Path path = DocPath.toPath(clazz.getPackage());
            final String pathText = DocPath.toAsciiDoc(path);
            // <<<
            doc.write(
                    ".Code",
                    formatter.sourceCode(CodeExtractor.extractPartOfCurrentMethod()),
                    "Result",
                    formatter.blockBuilder("====")
                            .content(pathText)
                            .build());
        }

        @Test
        public void path_from_a_class() {
            // >>>
            final Class<?> clazz = org.sfvl.samples.MyTest.class;
            final Path path = DocPath.toPath(clazz);
            final String pathText = DocPath.toAsciiDoc(path);
            // <<<
            doc.write(
                    ".Code",
                    formatter.sourceCode(CodeExtractor.extractPartOfCurrentMethod()),
                    "Result",
                    formatter.blockBuilder("====")
                            .content(pathText)
                            .build());
        }

        @Test
        public void path_from_a_nested_class() {
            // >>>
            final Class<?> clazz = org.sfvl.samples.MyTestWithNestedClass.MyNestedClass.class;
            final Path path = DocPath.toPath(clazz);
            final String pathText = DocPath.toAsciiDoc(path);
            // <<<
            doc.write(
                    ".Code",
                    formatter.sourceCode(CodeExtractor.extractPartOfCurrentMethod()),
                    "Result",
                    formatter.blockBuilder("====")
                            .content(pathText)
                            .build());
        }

        @Test
        public void file_of_a_class() {
            // >>>
            final Class<?> clazz = org.sfvl.samples.MyTest.class;
            final Path path = DocPath.toFile(clazz);
            final String pathText = DocPath.toAsciiDoc(path);
            // <<<
            doc.write(
                    ".Code",
                    formatter.sourceCode(CodeExtractor.extractPartOfCurrentMethod()),
                    "Result",
                    formatter.blockBuilder("====")
                            .content(pathText)
                            .build());
        }

        /**
         * With a nested class, the file is that of the main class of the file.
         */
        @Test
        public void file_of_a_nested_class() {
            // >>>
            final Class<?> clazz = org.sfvl.samples.MyTestWithNestedClass.MyNestedClass.class;
            final Path path = DocPath.toFile(clazz);
            final String pathText = DocPath.toAsciiDoc(path);
            // <<<
            doc.write(
                    ".Code",
                    formatter.sourceCode(CodeExtractor.extractPartOfCurrentMethod()),
                    "Result",
                    formatter.blockBuilder("====")
                            .content(pathText)
                            .build());
        }
    }

    String callResult(CallsRecorder recorder, Path path) {
        return String.format("%s | %s", recorder.lastCall(), DocPath.toAsciiDoc(path));
    }

    String callResult(CallsRecorder recorder, String t) {
        return String.format("%s | %s", recorder.lastCall(), t);
    }

    String line(DocPath docPath, Function<DocPath, OnePath> methodOnDocPath, OnePath relativeToApproved) {
        final CallsRecorder recorder = new CallsRecorder();

        final DocPath spyDocPath = addSpyRecorderOn(docPath, recorder);

        final OnePath onePath = methodOnDocPath.apply(spyDocPath);
        final String methodCalledOnDocPath = recorder.lastCall();

        final OnePath spy = addSpyRecorderOn(onePath, recorder);

        return String.join("\n",
                ".5+a| `" + methodCalledOnDocPath + "` | "
                        + callResult(recorder, spy.path())
                , "a| " + callResult(recorder, spy.folder())
                , "a| " + callResult(recorder, spy.filename())
                , "a| " + callResult(recorder, spy.from(relativeToApproved))
                , "a| " + callResult(recorder, spy.to(relativeToApproved))
        );
    }

    private <T> T addSpyRecorderOn(T docPath, Answer recorder) {
        return Mockito.mock((Class<T>) docPath.getClass(),
                Mockito.withSettings().spiedInstance(docPath).defaultAnswer(recorder));
    }

}

