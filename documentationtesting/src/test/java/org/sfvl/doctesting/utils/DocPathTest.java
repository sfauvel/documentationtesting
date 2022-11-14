package org.sfvl.doctesting.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.codeextraction.CodeExtractor;
import org.sfvl.codeextraction.MethodReference;
import org.sfvl.docformatter.Formatter;
import org.sfvl.docformatter.asciidoc.AsciidocFormatter;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.sample.MyClass;
import org.sfvl.printer.CodeAndResultList;
import org.sfvl.printer.Printer;
import org.sfvl.samples.MyTest;
import org.sfvl.test_tools.FastApprovalsExtension;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Function;

@ClassToDocument(clazz = DocPath.class)
@DisplayName(value = "Document path")
public class DocPathTest {
    @RegisterExtension
    static ApprovalsExtension doc = new FastApprovalsExtension();

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

    private String linePath(DocPath docPath, MethodReference.SerializableFunction<DocPath, OnePath> methodOnDocPath, Function<OnePath, Path> functionToPath) {

        final String methodCalledOnDocPath = MethodReference.getName(methodOnDocPath);
        final Method lastMethod = MethodReference.getMethod(methodOnDocPath);

        final OnePath onePath = methodOnDocPath.apply(docPath);
        final Optional<String> comment = CodeExtractor.getComment(lastMethod);

        return String.format("a| %s() | %s | %s", methodCalledOnDocPath, DocPath.toAsciiDoc(functionToPath.apply(onePath)), comment.orElse(""));
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

        final MethodReference.SerializableFunction<DocPath, String> nameMethod = DocPath::name;
        final String methodCalledOnDocPath = MethodReference.getName(nameMethod);
        final String name = nameMethod.apply(docPath);

        doc.write("[%autowidth]",
                "[%header]",
                "|====",
                "| Method | Result",
                String.format("| %s() | %s", methodCalledOnDocPath, name),
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
        final DocPath docPath = new DocPath(MethodReference.getMethod(MyTest::test_A));
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


        final MethodReference.SerializableFunction<DocPath, String> nameMethod = DocPath::name;
        final String methodCalledOnDocPath = MethodReference.getName(nameMethod);
        final String name = nameMethod.apply(docPath);

        doc.write("[%autowidth]",
                "[%header]",
                "|====",
                "| Method | Result",
                String.format("| %s() | %s", methodCalledOnDocPath, name),
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

        final Method method = MethodReference.getMethod(MyClass.MySubClass::doSomething);
        doc.write(String.format("Name for method `%s` in nested class `%s` is `%s`.",
                        method.getName(),
                        method.getDeclaringClass().getName(),
                        new DocPath(method).name()),
                ""
        );
    }

    @Test
    public void make_path_independent_of_operating_system() {
        doc.write("Path in asciidoc files must used '/' independently of operating system and file separator.",
                "",
                "It's important to always generate the same reference file (.adoc) because we compare it with the last generated one.",
                "Otherwise, the test could fail when executed on another operating system.",
                "", "");
        // >>>
        Path path = Paths.get("src", "main", "java");
        String asciiDocPath = DocPath.toAsciiDoc(path);
        // <<<
        doc.write(formatter.sourceCode(CodeExtractor.extractPartOfCurrentMethod()),
                String.format("*asciiDocPath = %s*", asciiDocPath));
    }

    @Test
    public void build_a_path() {

        doc.write("You can create a " + DocPath.class.getSimpleName() + " using one of the constructor available.", "");

        final CodeAndResultList<DocPath> docPathCodeAndResultList = Printer.extractCodes(
                new DocPath(DocPathTest.class),
                new DocPath(DocPathTest.class.getPackage(), "DocPathTest"),
                new DocPath(Paths.get("org", "sfvl", "doctesting", "utils"), "DocPathTest"),
                new DocPath(Paths.get(""), "DocPathTest"),
                new DocPath("DocPathTest")
        );

        final String s = docPathCodeAndResultList.formatGroupedByValue(
                (value, code) -> value.approved().path().toString(),
                (value, codes) -> "\nWith one of this code:\n"
                        + Printer.join(codes,
                            code -> doc.getFormatter().sourceCode(code), "\n")
                        + "Approved file is:" + formatter.sourceCode(DocPath.toAsciiDoc(Paths.get(value))),
                "---"
        );
        doc.write(s);
    }



    private String formatOnePathToPathResult(OnePath onePath, MethodReference.SerializableFunction<OnePath, Path> pathToString) {
        return formatLineResult(pathToString, DocPath.toAsciiDoc(pathToString.apply(onePath)));
    }

    private String formatOnePathToPathResult(OnePath onePath, MethodReference.SerializableBiFunction<OnePath, OnePath, Path> pathToString, OnePath relativeToOnePath) {
        return formatLineResult(pathToString, DocPath.toAsciiDoc(pathToString.apply(onePath, relativeToOnePath)));
    }

    private String formatOnePathToStringResult(OnePath onePath, MethodReference.SerializableFunction<OnePath, String> pathToString) {
        return formatLineResult(pathToString, pathToString.apply(onePath));
    }

    private String formatLineResult(Serializable methodCalled, String result) {
        return String.format("%s() | %s", MethodReference.getName(methodCalled), result);
    }

    private String line(DocPath docPath, MethodReference.SerializableFunction<DocPath, OnePath> methodOnDocPath, OnePath relativeToApproved) {
        final String methodCalledOnDocPath = MethodReference.getName(methodOnDocPath);
        final OnePath onePath = methodOnDocPath.apply(docPath);

        return String.join("\n",
                ".5+a| `" + methodCalledOnDocPath + "()` | "
                        + formatOnePathToPathResult(onePath, OnePath::path)
                , "a| " + formatOnePathToPathResult(onePath, OnePath::folder)
                , "a| " + formatOnePathToStringResult(onePath, OnePath::filename)
                , "a| " + formatOnePathToPathResult(onePath, OnePath::from, relativeToApproved)
                , "a| " + formatOnePathToPathResult(onePath, OnePath::to, relativeToApproved)
        );
    }

}

