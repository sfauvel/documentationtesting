package org.sfvl.codeextraction;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.doctesting.utils.NoTitle;
import org.sfvl.printer.CodeAndResult;
import org.sfvl.printer.Printer;
import org.sfvl.test_tools.FastApprovalsExtension;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@DisplayName(value = "Extract information from method reference")
public class MethodReferenceTest {
    @RegisterExtension
    static ApprovalsExtension doc = new FastApprovalsExtension();

    @Test
    @NoTitle
    public void description() {
        doc.write(String.format("`%s` class provides tools to extract information from a method reference.", this.getClass().getSimpleName()),
                "It retrieves the method associated with the method reference, so it's possible to get name.",
                "This allows to use the method reference instead of writing the method name as a string.",
                "Using refactoring tools, names are automatically updated and there is no risk to misspelled a name.",
                "");
    }

    static class MyClass {
        public void myMethod() {
        }

        public void myConsumer(String parameter) {
        }

        public String myFunction(Integer parameter) {
            return null;
        }

        public List<String> myFunctionWithGeneric(List<Integer> parameter) {
            return null;
        }

        public String myBiFunction(Integer param1, String param2) {
            return null;
        }

        public static void myStaticConsumer(String parameter) {
        }

        public String mySpecificMethod(Integer param1, String param2, List<String> param3) {
            return null;
        }
    }

    @Test
    @DisplayName("Find method name")
    public void find_method_name(TestInfo testInfo) {
        doc.write("It's possible to extract method name from a method reference.",
                "We can use either a reference using class or using an object instance (only on non-static method).",
                "", "");

        final MyClass myObject = new MyClass();

        final List<CodeAndResult<Object>> codeAndResult = new Printer().getCodeAndResult(
                MethodReference.getName(MyClass::myMethod),
                MethodReference.getName(myObject::myMethod),
                MethodReference.getName(MyClass::myConsumer),
                MethodReference.getName(MyClass::myFunction),
                MethodReference.getName(myObject::myFunction),
                MethodReference.getName(MyClass::myFunctionWithGeneric),
                MethodReference.getName(myObject::myBiFunction),
                MethodReference.getName(MyClass::myStaticConsumer));

        doc.write("",
                "[cols=\"6a,.^1,.^2a\";headers]",
                doc.getFormatter().tableWithHeader(
                        Arrays.asList("Code", "Type", "Returned value"),
                        formatToTableData(codeAndResult))
        );
    }

    @Test
    @DisplayName("Find method")
    public void find_method(TestInfo testInfo) {
        doc.write("It's possible to extract method from a method reference.", "", "");

        final MyClass myObject = new MyClass();

        final List<CodeAndResult<Object>> codeAndResult = new Printer().getCodeAndResult(
                MethodReference.getMethod(MyClass::myMethod),
                MethodReference.getMethod(myObject::myMethod),
                MethodReference.getMethod(MyClass::myConsumer),
                MethodReference.getMethod(MyClass::myFunction),
                MethodReference.getMethod(myObject::myFunction),
                MethodReference.getMethod(MyClass::myFunctionWithGeneric),
                MethodReference.getMethod(myObject::myBiFunction),
                MethodReference.getMethod(MyClass::myStaticConsumer));

        doc.write("",
                "[cols=\"4a,.^1,.^4a\";headers]",
                doc.getFormatter().tableWithHeader(
                        Arrays.asList("Code", "Type", "Returned value"),
                        formatToTableData(codeAndResult))
        );
    }

    public String mySpecificMethod(Integer param1, String param2, List<String> param3) {
        return null;
    }

    // >>>MySpecificInterface
    interface MySpecificInterface extends Serializable {
        String apply(Integer param1, String param2, List<String> param3);
    }
    // <<<MySpecificInterface

    @Test
    @DisplayName("Find method name from a specific method")
    public void find_method_name_from_complex_method(TestInfo testInfo) {

        doc.write(String.format("`%s` class provides some common interface declarations so you can use it with simple methods.", MethodReference.class.getSimpleName()),
                "When you have a method with several parameters, you need to create your own interface.",
                "Those methods could not work with ambiguous method reference (same method name with different parameters).",
                "", "");

        final String code = CodeExtractor.extractPartOfFile(
                new DocPath(this.getClass()).test().path(), MySpecificInterface.class.getSimpleName());

        doc.write(doc.getFormatter().sourceCodeBuilder("java")
                .title("Specific interface to create")
                .content(code)
                .build(), "");

        final List<CodeAndResult<Object>> codeAndResult = new Printer().getCodeAndResult(
                MethodReference.getName((MySpecificInterface) this::mySpecificMethod),
                MethodReference.getMethod((MySpecificInterface) this::mySpecificMethod));

        doc.write("",
                "[cols=\"4a,.^1,.^4a\";headers]",
                doc.getFormatter().tableWithHeader(
                        Arrays.asList("Code", "Type", "Returned value"),
                        formatToTableData(codeAndResult))
        );
    }

    @Test
    @DisplayName("Class used in examples")
    public void classUsed() {
        final String classCode = CodeExtractor.classSource(this.getClass(), MyClass.class);
        doc.write("",
                doc.getFormatter().sourceCodeBuilder()
                        .title("Class used for the examples")
                        .content(classCode)
                        .build(),
                "");
    }

    private List<List<?>> formatToTableData(List<CodeAndResult<Object>> codeAndResult) {
        return codeAndResult.stream()
                .map(cr -> Arrays.asList(
                        doc.getFormatter().sourceCode(cr.getCode()),
                        cr.getValue().getClass().getSimpleName(),
                        formatValueToDisplay(cr.getValue())))
                .collect(Collectors.toList());
    }

    private String formatValueToDisplay(Object value) {
        if (value instanceof Method) {
            Class declaringClass = ((Method) value).getDeclaringClass();
            // To allow line break when displayed on table
            return doc.getFormatter().sourceCode(value.toString()
                    .replace(declaringClass.getName(), declaringClass.getSimpleName())
                    .replaceAll("([\\(,])", "$1 "));
        } else {
            return value.toString();
        }
    }

}