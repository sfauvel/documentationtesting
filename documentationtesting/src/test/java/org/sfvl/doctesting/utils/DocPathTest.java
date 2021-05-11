package org.sfvl.doctesting.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.FindLambdaMethod;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.doctesting.sample.MyClass;
import org.sfvl.samples.MyTest;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.function.Function;

@DisplayName(value = "Document path")
public class DocPathTest {
    @RegisterExtension
    static ApprovalsExtension doc = new SimpleApprovalsExtension();

    final Formatter formatter = new AsciidocFormatter();
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
                        relativeToApproved.path()),
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
                line(docPath, DocPath::doc, relativeToApproved),
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
                        relativeToApproved.path()),
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
                line(docPath, DocPath::doc, relativeToApproved),
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

    class CallsRecorder<T extends Object> implements Answer<T> {
        String lastCall = "";

        @Override
        public T answer(InvocationOnMock a) throws Throwable {
            final Object result = a.callRealMethod();

//                final Object[] arguments = a.getArguments();
//                String parameters = Arrays.stream(arguments).map(v -> "" + v).collect(Collectors.joining(", "));
//                lastCall = a.getMethod().getName() + "(" + parameters + ")";
            lastCall = a.getMethod().getName() + "()";
            return (T) result;
        }

        String lastCall() {
            return lastCall;
        }

    }

    String line(DocPath docPath, Function<DocPath, OnePath> methodOnDocPath, OnePath realtiveToApproved) {
        final CallsRecorder recorder = new CallsRecorder();

        final DocPath spyDocPath = addSpyRecorderOn(docPath, recorder);

        final OnePath onePath = methodOnDocPath.apply(spyDocPath);
        final String methodCalledOnDocPath = recorder.lastCall();

        final OnePath spy = addSpyRecorderOn(onePath, recorder);

        Function<Object, String> callResult = p -> String.format("%s | %s", recorder.lastCall(), p);

        return String.join("\n",
                ".5+a| `" + methodCalledOnDocPath + "` | "
                        + callResult.apply(spy.path())
                , "a| " + callResult.apply(spy.folder())
                , "a| " + callResult.apply(spy.fullname())
                , "a| " + callResult.apply(spy.from(realtiveToApproved))
                , "a| " + callResult.apply(spy.to(realtiveToApproved))
        );
    }

    private <T> T addSpyRecorderOn(T docPath, Answer recorder) {
        return Mockito.mock((Class<T>)docPath.getClass(),
                Mockito.withSettings().spiedInstance(docPath).defaultAnswer(recorder));
    }

}

