package org.sfvl.howto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.codeextraction.ClassFinder;
import org.sfvl.codeextraction.CodeExtractor;
import org.sfvl.codeextraction.CodePath;
import org.sfvl.docformatter.asciidoc.AsciidocFormatter;
import org.sfvl.docformatter.AsciidocFormatterTest;
import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.ApprovalsExtensionTest;
import org.sfvl.codeextraction.MethodReference;
import org.sfvl.doctesting.utils.*;
import org.sfvl.doctesting.writer.ClassDocumentation;
import org.sfvl.doctesting.writer.Classes;
import org.sfvl.doctesting.writer.DocWriter;
import org.sfvl.test_tools.IntermediateHtmlPage;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

@ExtendWith(IntermediateHtmlPage.class)
public class HowTo {

    public static class DocFormatter extends AsciidocFormatter {
        public String getInclude(Class aClass, int offset) {
            return new Classes(this).includeClasses(CodePath.toPath(aClass.getPackage()), Arrays.asList(aClass), offset).trim();
        }

        public <T> String getInclude(MethodReference.SerializableConsumer<T> methodToInclude, int offset) {
            final Method method = MethodReference.getMethod(methodToInclude);

            final OnePath approvedPath = new DocPath(method).approved();
            return include(approvedPath.from(new DocPath(this.getClass()).approved()).toString(), offset);
        }

        public String linkToClass(Class<?> clazz) {
            final String title = new ClassDocumentation(null).getTestClassTitle(clazz);

            return linkToClass(clazz, title);
        }

        public String linkToClass(Class<?> clazz, String title) {
            return linkTo(clazz, null, title);
        }

        public String linkToMethod(Method method, String title) {
            final Class<?> clazz = new ClassFinder().getMainFileClass(method.getDeclaringClass());
            return linkTo(clazz, doc.getDocWriter().titleId(method), title);
        }

        public String linkTo(Class clazz, String anchor, String title) {
            final DocPath docPath = new DocPath(clazz);
            // TODO do we generate page here ? It's not really the role of link formatting.
            generatePage(clazz);

            final String address = DocPath.toAsciiDoc(Paths.get("{" + Config.DOC_PATH_TAG + "}").resolve(docPath.html().path()));
            return linkToPage(address, anchor, title);
        }

        private void generatePage(Class<?> clazz) {
            new IntermediateHtmlPage().generate(clazz);
        }
    }

    private static final DocFormatter formatter = new DocFormatter();

    @RegisterExtension
    static ApprovalsExtension doc = new ApprovalsExtension(new DocWriter(formatter));

    @Test
    public void getting_started() {

        doc.write("To get started quickly, you can download link:https://github.com/sfauvel/TryDocAsTest[Try doc as test] project.",
                "It's a minimal project that is ready to use and that implements a small demo.",
                "",
                "You can alo follow steps of the " + formatter.linkToClass(Tutorial.class, "Get started") + " page.",
                "",
                "You can use it on your own project alongside your existing tests.",
                "Both can work together without modifying your tests.",
                "",
                "To do that, you need to:",
                "",
                "* " + formatter.linkToClass(InstallingLibrary.class, "Installing DocumentationTesting").trim() + " maven library and add dependency to your `pom.xml`",
                "",
                "* Create a test and register ApprovalsExtension extension adding the code below to the test class.",
                "[source,java,indent=0]",
                "----",
                "@RegisterExtension",
                "static ApprovalsExtension extension = new SimpleApprovalsExtension();",
                "----",
                "",
                "* Write in your test everything you want to see in your documentation using `doc.write(\"...\")`",
                "You don't have to write assertions, tests will be passed when generated documents are the same as the last time.\n");
    }

    @Test
    @NoTitle
    public void create_a_test() {
        doc.write(formatter.getInclude(ApprovalsExtensionTest::using_extension, 0));
    }

    @Test
    public void organize_documentation() {
        final Path nestedClassDoc = new DocPath(MethodReference.getMethod(ApprovalsExtensionTest::nested_class))
                .html().path();

        final String nested_class_html = String.format("link:%s[%s]\n",
                DocPath.toAsciiDoc(nestedClassDoc),
                "Nested class");
        doc.write("To organize documentation, we have to consider test methods as the generator of one chapter.",
                "Each test will have a title by default following by text added executing it.",
                "Some of them show a specific behavior like any classic test.",
                "",
                "But, it's also possible to have one test that runs several cases and presents a global report of the execution.",
                "Even if there is a regression for one case in that test, ",
                "all results will be written in the document and can be compared with the reference file.",
                "",
                "We also can create a test method that just write text without code execution.",
                "It can be used to add general description or to minimize what it is written in a test to simplify its reuse.",
                "",
                "The final document respect the order of the method in the test file.",
                "We can use subclasses to create subchapter in the document (see: " + formatter.linkToMethod(MethodReference.getMethod(ApprovalsExtensionTest::nested_class), "Nested class") + ")",
                "",
                "It's not necessary to follow source code organization in tests.",
                "Instead, organize your test packages according to the document you want to generate.");
    }

    @Test
    @NoTitle
    public void format_text() {
        String listText = "";
        {
            // >>>
            Formatter formatter = new AsciidocFormatter();
            final String text = formatter.listItems("Item A", "Item B", "Item C");
            // <<<
            listText = text;
        }
        doc.write(formatter.title(1, "Format text"),
                "",
                "To format text, we can use a formatter that hide technical syntax of the markup language used.",
                "",
                ".Formatter usage",
                CodeExtractor.extractPartOfCurrentMethod(),
                "",
                formatter.blockBuilder("----")
                        .title("Text generated")
                        .content(listText)
                        .build(),
                "",
                formatter.blockBuilder("====")
                        .title("Final rendering")
                        .content(listText)
                        .build(),
                "",
                "You can take a look at the " + formatter.linkToClass(AsciidocFormatterTest.class, "formatter's full documentation")
                        + " to get an idea of the features available."
        );
    }

    @Test
    @NoTitle
    public void create_a_document() {
        doc.write(
                formatter.getInclude(CreateADocument::generate_html, 0),
                "",
                "For more features, you can look at the " + formatter.linkToClass(CreateADocument.class, "documentation on creating a document").trim() + "."
        );
    }

    @Test
    @NoTitle
    public void use_your_own_style() {
        doc.write(formatter.getInclude(UseYourOwnStyle.class, 0));
    }

}