package org.sfvl.howto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.asciidoc.AsciidocFormatter;
import org.sfvl.docformatter.AsciidocFormatterTest;
import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.ApprovalsExtensionTest;
import org.sfvl.doctesting.junitextension.FindLambdaMethod;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.doctesting.utils.*;
import org.sfvl.doctesting.writer.Classes;
import org.sfvl.test_tools.IntermediateHtmlPage;

import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.Arrays;

public class HowTo {

    @RegisterExtension
    static ApprovalsExtension doc = new SimpleApprovalsExtension();

    private final AsciidocFormatter formatter = new AsciidocFormatter();

    @Test
    public void getting_started() {

        doc.write("To get started quickly, you can download link:https://github.com/sfauvel/TryDocAsTest[Try doc as test] project.",
                "It's a minimal project that is ready to use and which implementing a small demo.",
                "",
                "If you want to use it on your own project, you need to:",
                "",
                "* " + linkToClass(InstallingLibrary.class, "Installing DocumentationTesting").trim() + " maven library and add dependency to your `pom.xml`",
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
        doc.write(getInclude(ApprovalsExtensionTest::using_extension, 0));
    }

    @Test
    @NoTitle
    public void format_text() {
        // >>>
        Formatter formatter = new AsciidocFormatter();
        final String text = formatter.listItems("Item A", "Item B", "Item C");
        // <<<
        doc.write(formatter.title(1, "Format text"),
                "",
                "To format text, we can use a formatter that hide technical syntax of the markup language used.",
                "",
                ".Formatter usage",
                CodeExtractor.extractPartOfCurrentMethod(),
                "",
                formatter.blockBuilder("----")
                        .title("Text generated")
                        .content(text)
                        .build(),
                "",
                formatter.blockBuilder("====")
                        .title("Final rendering")
                        .content(text)
                        .build(),
                "",
                "You can take a look at the " + linkToClass(AsciidocFormatterTest.class, "formatter's full documentation")
                        + " to get an idea of the features available."
        );
    }

    @Test
    @NoTitle
    public void create_a_document() {
        doc.write(
                getInclude(CreateADocument::generate_html, 0),
                "",
                "For more features, you can look at the " + linkToClass(CreateADocument.class, "documentation on creating a document").trim() + "."
        );
    }

    @Test
    @NoTitle
    public void use_your_own_style() {
        doc.write(getInclude(UseYourOwnStyle.class, 0));
    }

    private void generatePage(Class<?> clazz) {
        new IntermediateHtmlPage().generate(clazz);
    }

    private String linkToClass(Class<?> clazz) {

        final String className = clazz.getSimpleName();
        final String title = className.substring(0, 1) +
                className.substring(1)
                        .replaceAll("([A-Z])", " $1")
                        .toLowerCase();

        return linkToClass(clazz, title);
    }

    private String linkToClass(Class<?> clazz, String title) {
        final DocPath docPath = new DocPath(clazz);
        generatePage(clazz);

        return String.format("link:%s[%s]\n",
                DocPath.toAsciiDoc(Paths.get("{"+Config.DOC_PATH_TAG+"}").resolve(docPath.doc().path())),
                title);
    }

    public String getInclude(Class aClass, int offset) {
        return new Classes(formatter).includeClasses(DocPath.toPath(aClass.getPackage()), Arrays.asList(aClass), offset).trim();
    }

    public <T> String getInclude(FindLambdaMethod.SerializableConsumer<T> methodToInclude, int offset) {
        final Method method = FindLambdaMethod.getMethod(methodToInclude);

        final OnePath approvedPath = new DocPath(method).approved();
        return formatter.include(approvedPath.from(new DocPath(this.getClass()).approved()).toString(), offset);
    }
}