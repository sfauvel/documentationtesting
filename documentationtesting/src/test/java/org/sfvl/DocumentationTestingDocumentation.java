package org.sfvl;

import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.docformatter.Formatter;
import org.sfvl.docformatter.FormatterDocumentation;
import org.sfvl.doctesting.DocTestingDocumentation;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.FindLambdaMethod;
import org.sfvl.doctesting.junitextension.JUnitExtensionDocumentation;
import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.utils.DocumentationNamer;
import org.sfvl.doctesting.writer.Document;
import org.sfvl.doctesting.writer.DocumentProducer;
import org.sfvl.doctesting.writer.Options;
import org.sfvl.howto.HowTo;
import org.sfvl.howto.InstallingLibrary;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class DocumentationTestingDocumentation implements DocumentProducer {

    private final Formatter formatter = new AsciidocFormatter();
    /// Record all builder references as a link in documentation.
    private Set<Class<? extends DocumentProducer>> buildersToGenerate = new HashSet<>();

    protected String getHeader() {
        return "include::../../../readme.adoc[leveloffset=+1]";
    }

    protected String getContent() {
        return String.join("\n",
                "This project is composed of two main packages.",
                "",
                "* " + linkToDocClass(DocTestingDocumentation.class) + ": Tools to make test validating generated files.",
                "* " + linkToDocClass(FormatterDocumentation.class) + ": Utilities to format documentation.",
                "",
                "Section " + linkToClass(HowTo.class) + " shows how to do some common needs.",
                "",
                "== Getting started",
                "",
                "To get started quickly, you can download link:https://github.com/sfauvel/TryDocAsTest[Try doc as test] project.",
                "It's a minimal project that is ready to use and which implementing a small demo.",
                "",
                "",
                "If you want to use it on your own project, you need to:",
                "",
                "* " + linkToDocClass(InstallingLibrary.class, "Installing DocumentationTesting").trim() + " maven library and add dependency to your `pom.xml`",
                "",
                "* Create a test and register " + ApprovalsExtension.class.getSimpleName() + " extension adding the code below to the test class.",
                formatter.sourceCodeBuilder("java")
                        .source("private static final DocWriter doc = new DocWriter();\n" +
                                "@RegisterExtension\n" +
                                "static ApprovalsExtension extension = new ApprovalsExtension(docWriter);")
                        .build(),
                "",
                "* Write in your test everything you want to see in your documentation using `doc.write(\"...\")`",
                "You don't have to write assertions, tests will be passed when generated documents are the same as the last time.",
                "",
                "",
                "== Main features",
                "",
                "* " + linkToDocClass(JUnitExtensionDocumentation.class, "JUnit extension embedded Approvals"),
                "** Name file associate to each test",
                "** Execute verification after test",
                "* Generation of a general documentation that aggregate all test files",
                "* Tools to extract parts of code",
                "* " + linkToDocClass(FormatterDocumentation.class, "API to transform text") + " to output format");
    }

    private String linkToDocClass(Class<? extends DocumentProducer> clazz) {
        final String name = clazz.getPackage().getName();
        return linkToDocClass(clazz, name);
    }

    private String linkToDocClass(Class<? extends DocumentProducer> clazz, String name) {
        buildersToGenerate.add(clazz);
        return String.format("link:%s.html[%s]\n",
                clazz.getName().replace(".", "/"),
                name);
    }

    private String linkToClass(Class<?> clazz) {
        final String className = clazz.getSimpleName();
        final String title = className.substring(0, 1) +
                className.substring(1)
                        .replaceAll("([A-Z])", " $1")
                        .toLowerCase();

        return String.format("link:%s.html[%s]\n",
                DocumentationNamer.toPath(clazz),
                title);
    }

    public <T> String linkToMethod(FindLambdaMethod.SerializableConsumer<T> methodToInclude) {
        final Method method = FindLambdaMethod.getMethod(methodToInclude);
        final DocumentationNamer documentationNamer = new DocumentationNamer(Config.DOC_PATH, method);
        final String filename = documentationNamer.getApprovedPath(Config.DOC_PATH).toString();
        String methodName = method.getName();

        String title = methodName.replace("_", " ");
        title = title.substring(0, 1).toUpperCase() + title.substring(1);

        return String.format("link:%s[%s]\n",
                filename.replaceAll("\\.adoc", ".html"),
                title);

    }

    private void buildLinkedFile() {
        for (Class<? extends DocumentProducer> aClass : this.buildersToGenerate) {
            try {
                aClass.getDeclaredConstructor().newInstance().produce();
            } catch (IOException
                    | InstantiationException
                    | IllegalAccessException
                    | InvocationTargetException
                    | NoSuchMethodException e) {
                throw new RuntimeException("Not able to generate " + aClass.getSimpleName(), e);
            }
        }
    }

    public String build() {
        return formatter.paragraphSuite(
                new Options(formatter).withCode(),
                getHeader(),
                getContent()
        );

    }

    public void produce() throws IOException {
        new Document(build()).saveAs(Paths.get("index.adoc"));
    }

    public static void main(String... args) throws IOException {
        final DocumentationTestingDocumentation doc = new DocumentationTestingDocumentation();
        doc.produce();
        doc.buildLinkedFile();
    }

}
