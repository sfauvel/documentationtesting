package org.sfvl;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.docformatter.AsciidocFormatterTest;
import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.DocTestingDocumentation;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.FindLambdaMethod;
import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.doctesting.utils.DocWriter;
import org.sfvl.doctesting.writer.DocumentProducer;
import org.sfvl.doctesting.writer.Options;
import org.sfvl.howto.HowTo;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class DocumentationTestingDocumentation {

    @RegisterExtension
    static ApprovalsExtension doc = new ApprovalsExtension(new DocWriter() {
        @Override
        public String formatOutput(Class<?> clazz) {
            return String.join("\n",
                    ":notitle:",
                    super.formatOutput(clazz));
        }
    });

    private final Formatter formatter = new AsciidocFormatter();
    /// Record all builder references as a link in documentation.
    private static Set<Class<? extends DocumentProducer>> buildersToGenerate = new HashSet<>();

    protected String getHeader() {
        return "include::../../../readme.adoc[leveloffset=+1]";
    }

    @AfterAll
    static public void writeIndexPage() throws IOException {
        final Method method = FindLambdaMethod.getMethod(DocumentationTestingDocumentation::documentationTesting);

        final DocPath docPath = new DocPath(method);
        String content = String.join("\n",
                ":nofooter:",
                "include::" + docPath.approved().from(Config.DOC_PATH).toString() + "[]");

        final Path indexFile = Config.DOC_PATH.resolve("index.adoc");
        try (FileWriter fileWriter = new FileWriter(indexFile.toFile())) {
            fileWriter.write(content);
        }
    }

    private void generatePage(DocPath docPath) throws IOException {
        String includeContent = String.join("\n",
                ":toc: left",
                ":nofooter:",
                ":stem:",
                ":source-highlighter: rouge",
                ":toclevels: 4",
                "",
                String.format("include::%s[]", docPath.approved().fullname()));

        try (FileWriter fileWriter = new FileWriter(docPath.page().path().toFile())) {
            fileWriter.write(includeContent);
        }
    }

    @Test
    public void documentationTesting() {
        doc.write(getContent());
    }

    public String getContent() {
        return String.join("\n",
                "[cols=2]",
                "[.DocumentationTestingDoc.intro]",
                "|====",
                "^.a| == Tutorial",
                "[.subtitle]",
                "Learning-oriented",
                "",
                "In progress...",
                "",

                "^.a| == How-to guides",
                "[.subtitle]",
                "Problem-oriented",
                "",
                "Section " + linkToClass(HowTo.class) + " shows how to do some common needs.",

                "^.a| == Explanation",
                "[.subtitle]",
                "Understanding-oriented",
                "",
                "[.noborder]",
                "!====",
                "a!",
                "* " + linkToClass(org.sfvl.doctesting.junitextension.ApprovalsExtensionTest.class, "JUnit extension embedded Approvals"),
                "** Name file associate to each test",
                "** Execute verification after test",
                "* Generation of a general documentation that aggregate all test files",
                "* Tools to extract parts of code",
                "* " + linkToClass(AsciidocFormatterTest.class, "API to transform text") + " to output format",
                "!====",

                "^.a| == Reference",
                "[.subtitle]",
                "Information-oriented",
                "",
                "[.noborder]",
                "!====",
                "a!",
                "* " + linkToClass(DocTestingDocumentation.class) + ": Tools to make test validating generated files.",
                "* " + linkToClass(AsciidocFormatterTest.class) + ": Utilities to format documentation.",
                "!====",
                "",
                "|====",
                "++++",
                "<style>",
                "table.DocumentationTestingDoc.grid-all > * > tr > * {",
                "    border-width:3px;",
                "    border-color:#AAAAAA;",
                "}",
                "",
                ".DocumentationTestingDoc.intro td {",
                "    background-color:#05fdCC;",
                "    //border: 30px solid #BFBFBF;",
                "    -webkit-box-shadow: 3px 3px 6px #A9A9A9;",
                "}",
                "",
                ".DocumentationTestingDoc .subtitle {",
                "    color: #888888;",
                "}",
                ".DocumentationTestingDoc .noborder td{",
                "    border: none;",
                "    -webkit-box-shadow: none;",
                "}",
                ".DocumentationTestingDoc table.noborder  {",
                "    border: none;",
                "}",
                "",
                "</style>",
                "++++");
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
        try {
            generatePage(docPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return String.format("link:%s[%s]\n",
                docPath.doc().path(),
                title);
    }

    public <T> String linkToMethod(FindLambdaMethod.SerializableConsumer<T> methodToInclude) {
        final Method method = FindLambdaMethod.getMethod(methodToInclude);

        String methodName = method.getName();
        String title = methodName.replace("_", " ");
        title = title.substring(0, 1).toUpperCase() + title.substring(1);

        return String.format("link:%[%s]\n",
                new DocPath(method).doc().path(),
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
                new Options(formatter)
                        .with("nofooter")
                        .build(),
                getHeader(),
                getContent()
        );

    }

}
