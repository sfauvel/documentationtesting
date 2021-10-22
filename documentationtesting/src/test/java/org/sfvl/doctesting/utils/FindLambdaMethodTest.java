package org.sfvl.doctesting.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.Formatter;
import org.sfvl.docformatter.asciidoc.AsciidocFormatter;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.doctesting.utils.CodeExtractor;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.doctesting.utils.FindLambdaMethod;
import org.sfvl.doctesting.utils.NoTitle;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;

@DisplayName(value = "Extract information from method reference")
public class FindLambdaMethodTest {
    @RegisterExtension
    static ApprovalsExtension doc = new SimpleApprovalsExtension();

    final Formatter formatter = new AsciidocFormatter();

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

        doc.write("[cols=\"6,1,2\";headers]",
                "|====",
                "| Code | Type | Returned value ",
                "",
                findMethodNameLine(testInfo, "method",
                        // >>>method
                        FindLambdaMethod.getName(MyClass::myMethod)
                        // <<<method
                ),
                findMethodNameLine(testInfo, "methodOnInstance",
                        // >>>methodOnInstance
                        FindLambdaMethod.getName(myObject::myMethod)
                        // <<<methodOnInstance
                ),
                findMethodNameLine(testInfo, "consumer",
                        // >>>consumer
                        FindLambdaMethod.getName(MyClass::myConsumer)
                        // <<<consumer
                ),
                findMethodNameLine(testInfo, "functionOnClass",
                        // >>>functionOnClass
                        FindLambdaMethod.getName(MyClass::myFunction)
                        // <<<functionOnClass
                ),
                findMethodNameLine(testInfo, "functionOnInstance",
                        // >>>functionOnInstance
                        FindLambdaMethod.getName(myObject::myFunction)
                        // <<<functionOnInstance
                ),
                findMethodNameLine(testInfo, "functionWithGeneric",
                        // >>>functionWithGeneric
                        FindLambdaMethod.getName(MyClass::myFunctionWithGeneric)
                        // <<<functionWithGeneric
                ),
                findMethodNameLine(testInfo, "myStaticConsumer",
                        // >>>myStaticConsumer
                        FindLambdaMethod.getName(MyClass::myStaticConsumer)
                        // <<<myStaticConsumer
                ),

                "|====",
                "");

    }

    /**
     * It's possible to extract method from a method reference.
     *
     * @param testInfo
     */
    @Test
    @DisplayName("Find method")
    public void find_method(TestInfo testInfo) {

        FindLambdaMethod.getMethod(MyClass::myFunctionWithGeneric);
        final MyClass myObject = new MyClass();

        doc.write("[cols=\"4,1,4\";headers]",
                "|====",
                "| Code | Type | Returned value ",
                "",
                findMethodNameLine(testInfo, "method",
                        // >>>method
                        FindLambdaMethod.getMethod(MyClass::myMethod)
                        // <<<method
                ),
                findMethodNameLine(testInfo, "methodOnInstance",
                        // >>>methodOnInstance
                        FindLambdaMethod.getMethod(myObject::myMethod)
                        // <<<methodOnInstance
                ),
                findMethodNameLine(testInfo, "consumer",
                        // >>>consumer
                        FindLambdaMethod.getMethod(MyClass::myConsumer)
                        // <<<consumer
                ),
                findMethodNameLine(testInfo, "functionOnClass",
                        // >>>functionOnClass
                        FindLambdaMethod.getMethod(MyClass::myFunction)
                        // <<<functionOnClass
                ),
                findMethodNameLine(testInfo, "functionOnInstance",
                        // >>>functionOnInstance
                        FindLambdaMethod.getMethod(myObject::myFunction)
                        // <<<functionOnInstance
                ),
                findMethodNameLine(testInfo, "functionWithGeneric",
                        // >>>functionWithGeneric
                        FindLambdaMethod.getMethod(MyClass::myFunctionWithGeneric)
                        // <<<functionWithGeneric
                ),
                findMethodNameLine(testInfo, "myStaticConsumer",
                        // >>>myStaticConsumer
                        FindLambdaMethod.getMethod(MyClass::myStaticConsumer)
                        // <<<myStaticConsumer
                ),

                "|====",
                "");

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

        // >>>1
        String methodName = FindLambdaMethod.getName((MySpecificInterface) this::mySpecificMethod);
        // <<<1

        doc.write(String.format("`%s` class provides some common interface declarations so you can use it with simple methods.", FindLambdaMethod.class.getSimpleName()),
                        "When you have a method with several parameters, you need to create your own interface.",
                        "Those methods could not work with ambiguous method reference (same method name with different parameters).",
                "", "");
        final AsciidocFormatter formatter = new AsciidocFormatter();
        final String code = CodeExtractor.extractPartOfFile(
                new DocPath(this.getClass()).test().path(), MySpecificInterface.class.getSimpleName());

        doc.write(formatter.sourceCodeBuilder("java")
                .title("Specific interface to create")
                .content(code)
                .build(), "");

        doc.write("[cols=\"4,1,4\";headers]",
                "|====",
                "| Code | Type | Returned value ",
                "",
                findMethodNameLine(testInfo, "getName",
                        // >>>getName
                        FindLambdaMethod.getName((MySpecificInterface) this::mySpecificMethod)
                        // <<<getName
                ),
                findMethodNameLine(testInfo, "getMethod",
                        // >>>getMethod
                        FindLambdaMethod.getMethod((MySpecificInterface) this::mySpecificMethod)
                        // <<<getMethod
                ),
                "|====");
    }

    @Test
    @DisplayName("Class used in examples")
    public void classUsed() {
        final String classCode = CodeExtractor.classSource(this.getClass(), MyClass.class);
        doc.write("",
                formatter.sourceCodeBuilder()
                        .title("Class used for the examples")
                        .content(classCode)
                        .build(),
                "");
    }

    public String findMethodNameLine(TestInfo testInfo, String tagToExtract, Object returnedValue) {
        String valueToDisplay = returnedValue.toString();
        if (returnedValue instanceof Method) {
            Class declaringClass = ((Method) returnedValue).getDeclaringClass();
            valueToDisplay = returnedValue.toString()
                    .replace(declaringClass.getName(), declaringClass.getSimpleName())
                    .replaceAll("([\\(,])", "$1 "); // To allow line break when displayed on table
            valueToDisplay = formatter.sourceCode(valueToDisplay);
        }
        return String.format("a| %s .^| %s .^a| %s",
                extractMarkedCode(testInfo, tagToExtract),
                returnedValue.getClass().getSimpleName(),
                valueToDisplay);
    }

    private String extractMarkedCode(TestInfo testInfo) {
        return formatSourceCode(CodeExtractor.extractPartOfMethod(testInfo.getTestMethod().get()));
    }

    private String extractMarkedCode(TestInfo testInfo, String suffix) {
        return formatSourceCode(CodeExtractor.extractPartOfMethod(testInfo.getTestMethod().get(), suffix));
    }

    private <T> String formatSourceCode(String source) {
        return String.join("\n",
                "[source, java, indent=0]",
                "----",
                source,
                "----");
    }
}