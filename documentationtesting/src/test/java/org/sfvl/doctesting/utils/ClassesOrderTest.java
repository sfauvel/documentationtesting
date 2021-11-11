package org.sfvl.doctesting.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.codeextraction.CodeExtractor;
import org.sfvl.docformatter.asciidoc.AsciidocFormatter;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@DisplayName("Classes order")
class ClassesOrderTest {

    @RegisterExtension
    static ApprovalsExtension doc = new SimpleApprovalsExtension();

    @Test
    public void in_order(TestInfo testInfo) {
        // >>>
        Class<?> testClass = MyClass.class;
        Stream<ClassesOrder.EncapsulateDeclared> declaredInOrder
                = new ClassesOrder().getDeclaredInOrder(testClass);
        // <<<

        final AsciidocFormatter formatter = new AsciidocFormatter();

        doc.write("We retrieve names of methods and nested classes in the same order they appear in source file.", "");
        doc.write("",
                formatter.sourceCodeBuilder("java")
                        .title("Get declared methods and classes in order")
                        .source(CodeExtractor.extractPartOfMethod(testInfo.getTestMethod().get()))
                        .build());

        final List<String> items = declaredInOrder
                .map(ClassesOrder.EncapsulateDeclared::getName)
                .collect(Collectors.toList());

        doc.write("",
                "Only methods and classes directly under the class passed in parameter are returned.",
                formatter.listItems(items.toArray(new String[0])));

        doc.write("", "",
                formatter.sourceCodeBuilder("java")
                        .title("Test example using")
                        .indent(0)
                        .source(CodeExtractor.classSource(testClass))
                        .build(),
                "", "");
    }

    @Test
    public void filter_methods_and_classes() {
        final Class<?> testClass = MyClass.class;

        final ClassesOrder classesOrder = new ClassesOrder();
        final Stream<ClassesOrder.EncapsulateDeclared> declaredInOrder = classesOrder.getDeclaredInOrder(
                testClass,
                m -> m.getName().contains("_1"),
                c -> c.getName().contains("ClassB"));

        final AsciidocFormatter formatter = new AsciidocFormatter();

        doc.write("We can give filters to select methods and classes to return.", "");
        final List<String> items = declaredInOrder
                .map(ClassesOrder.EncapsulateDeclared::getName)
                .collect(Collectors.toList());
        doc.write(formatter.listItems(items.toArray(new String[0])));

        doc.write("", "",
                formatter.sourceCodeBuilder("java")
                        .title("Test example using")
                        .indent(0)
                        .source(CodeExtractor.classSource(testClass))
                        .build(),
                "", "");
    }

    @Test
    public void nested_class_using_outer_method() {
        final Class<?> testClass = MyClassWithOnlySubClassExterne.class;

        final ClassesOrder classesOrder = new ClassesOrder();
        Stream<ClassesOrder.EncapsulateDeclared> declaredInOrder = classesOrder.getDeclaredInOrder(testClass);
        final AsciidocFormatter formatter = new AsciidocFormatter();

        doc.write("A nested class calling an outer method generate some special method that are not in source.",
                "Those methods should not be in sorted method list",
                "");
        final List<String> items = declaredInOrder
                .map(ClassesOrder.EncapsulateDeclared::getName)
                .collect(Collectors.toList());
        doc.write(formatter.listItems(items.toArray(new String[0])));

        doc.write("", "",
                formatter.sourceCodeBuilder("java")
                        .title("Test example using")
                        .indent(0)
                        .source(CodeExtractor.classSource(testClass))
                        .build(),
                "", "");
    }

    class MyClass {

        public void method_1() {
        }

        class MySubClassA {
            public void method_A_1() {
            }
        }

        public void method_2() {
        }

        class MySubClassB {
            public void method_B_1() {
            }
        }

        public void method_3() {
        }
    }

    static class MyClassWithOnlySubClassExterne {

        private void methodOfClassAtFirstLevel() {
        }

        class MySubClassA {
            public void method_A_1() {
                methodOfClassAtFirstLevel();
            }
        }
    }
}


