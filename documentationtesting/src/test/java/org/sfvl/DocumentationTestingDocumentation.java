package org.sfvl;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.docformatter.AsciidocFormatterTest;
import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.DocTestingDocumentation;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.ApprovalsExtensionTest;
import org.sfvl.doctesting.junitextension.FindLambdaMethod;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.doctesting.utils.NoTitle;
import org.sfvl.doctesting.writer.DocumentProducer;
import org.sfvl.doctesting.writer.Options;
import org.sfvl.howto.HowTo;
import org.sfvl.howto.KnownIssues;
import org.sfvl.howto.Tutorial;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

@DisplayName(value = "Documentation Testing Library")
public class DocumentationTestingDocumentation {

    @RegisterExtension
    static ApprovalsExtension doc = new SimpleApprovalsExtension();

    private final Formatter formatter = new AsciidocFormatter();
    /// Record all builder references as a link in documentation.
    private static Set<Class<? extends DocumentProducer>> buildersToGenerate = new HashSet<>();

    protected String getHeader() {
        return "include::../../../readme.adoc[leveloffset=+1]";
    }

    @AfterAll
    static public void writeIndexPage() throws IOException {
        final DocPath docPath = new DocPath(DocumentationTestingDocumentation.class);
        String content = String.join("\n",
                doc.getDocWriter().defineDocPath(Paths.get(".")),
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
    @NoTitle
    public void documentationTesting() {
        doc.write(getContent());
    }

    public String getContent() {
        final DocPath docPath = new DocPath(Paths.get(""), "indexContent");
        final Path from = docPath.resource().from(new DocPath(this.getClass()).approved());

        return String.join("\n",
                ":TUTORIAL_HTML: " + generatePageAndGetPath(Tutorial.class),
                ":HOW_TO_HTML: " + generatePageAndGetPath(HowTo.class),
                ":APPROVAL_EXTENSION_HTML: " + generatePageAndGetPath(ApprovalsExtensionTest.class),
                ":ASCIIDOC_FORMATTER_HTML: " + generatePageAndGetPath(AsciidocFormatterTest.class),
                ":DOC_TESTING_DOCUMENTATION_HTML: " + generatePageAndGetPath(DocTestingDocumentation.class),
                ":KNOWN_ISSUES_HTML:" + generatePageAndGetPath(KnownIssues.class),
                String.format("include::%s[leveloffset=+1]", DocPath.toAsciiDoc(from))
        );
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
        return String.format("link:%s[%s]\n",
                generatePageAndGetPath(clazz),
                title);
    }

    private String generatePageAndGetPath(Class<?> clazz) {
        final DocPath docPath = new DocPath(clazz);
        try {
            generatePage(docPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return DocPath.toAsciiDoc(docPath.doc().path());
    }

    public <T> String linkToMethod(FindLambdaMethod.SerializableConsumer<T> methodToInclude) {
        final Method method = FindLambdaMethod.getMethod(methodToInclude);

        String methodName = method.getName();
        String title = methodName.replace("_", " ");
        title = title.substring(0, 1).toUpperCase() + title.substring(1);

        return String.format("link:{%s}/%[%s]\n",
                Config.DOC_PATH_TAG,
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
