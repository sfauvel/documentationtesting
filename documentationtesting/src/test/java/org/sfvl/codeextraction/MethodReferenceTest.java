package org.sfvl.codeextraction;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.doctesting.utils.NoTitle;
import org.sfvl.printer.Printer;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;

@DisplayName(value = "Extract information from method reference")
public class MethodReferenceTest {
    @RegisterExtension
    static ApprovalsExtension doc = new SimpleApprovalsExtension();

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

        public static void myStaticConsumer(String parameter) {
        }

        public String mySpecificMethod(Integer param1, String param2, List<String> param3) {
            return null;
        }
    }

    /**
     * It's possible to extract method name from a method reference.
     * We can use either a reference using class or using an object instance (only on non-static method).
     */
    @Test
    @DisplayName("Find method name")
    public void find_method_name(TestInfo testInfo) {
        final MyClass myObject = new MyClass();

        final String lines = new Printer().showResultWithFormat(
                this::linesFormatter,
                MethodReference.getName(MyClass::myMethod),
                MethodReference.getName(myObject::myMethod),
                MethodReference.getName(MyClass::myConsumer),
                MethodReference.getName(MyClass::myFunction),
                MethodReference.getName(myObject::myFunction),
                MethodReference.getName(MyClass::myFunctionWithGeneric),
                MethodReference.getName(MyClass::myStaticConsumer));

        doc.write("",
                "[cols=\"6,1,2\";headers]",
                "|====",
                "| Code | Type | Returned value ",
                "",
                lines,
                "|====");
    }

    /**
     * It's possible to extract method from a method reference.
     *
     * @param testInfo
     */
    @Test
    @DisplayName("Find method")
    public void find_method(TestInfo testInfo) {

        final MyClass myObject = new MyClass();

        final String lines = new Printer().showResultWithFormat(
                this::linesFormatter,
                MethodReference.getMethod(MyClass::myMethod),
                MethodReference.getMethod(myObject::myMethod),
                MethodReference.getMethod(MyClass::myConsumer),
                MethodReference.getMethod(MyClass::myFunction),
                MethodReference.getMethod(myObject::myFunction),
                MethodReference.getMethod(MyClass::myFunctionWithGeneric),
                MethodReference.getMethod(MyClass::myStaticConsumer));


        doc.write("",
                "[cols=\"4,1,4\";headers]",
                "|====",
                "| Code | Type | Returned value ",
                "",
                lines,
                "|====");
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

        final String lines = new Printer().showResultWithFormat(
                this::linesFormatter,
                MethodReference.getName((MySpecificInterface) this::mySpecificMethod),
                MethodReference.getMethod((MySpecificInterface) this::mySpecificMethod));

        doc.write("",
                "[cols=\"4,1,4\";headers]",
                "|====",
                "| Code | Type | Returned value ",
                "",
                lines,
                "|====");
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

    private String linesFormatter(Object value, String code) {
        return String.format("a| %s .^| %s .^a| %s\n",
                doc.getFormatter().sourceCode(code),
                value.getClass().getSimpleName(),
                formatValueToDisplay(value));

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