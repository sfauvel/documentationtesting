package org.sfvl.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.sfvl.Person;
import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.doctesting.demo.DemoDocumentation;
import org.sfvl.doctesting.junitinheritance.DocAsTestBase;
import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.doctesting.utils.PathProvider;
import org.sfvl.doctesting.writer.ClassDocumentation;
import org.sfvl.doctesting.writer.Document;
import org.sfvl.doctesting.writer.Options;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BasicDocumentation extends DemoDocumentation {

    public String getContent() {
        final Map<String, String> perfAttributes = new HashMap<>();
        String lines = "";
        try {
            lines = parseTestReport(perfAttributes);
        } catch (Exception e) {
            new RuntimeException(e);
        }

        final String perfAttributesFileName = "perfAttributes.adoc";

        try (final FileWriter fileWriter = new FileWriter(getAbsoluteDocPath().resolve(perfAttributesFileName).toFile())) {
            String attributes = perfAttributes.entrySet().stream()
                    .map(entry -> String.format(":%s: %s", entry.getKey(), entry.getValue()))
                    .collect(Collectors.joining("\n"));
            fileWriter.write(attributes);
        } catch (IOException e) {
            throw new RuntimeException("Could not write '" + perfAttributesFileName + "' file", e);
        }

        String timeFromReports = "\n[%autowidth]\n|====\n" +
                "| Category | Class | Time in seconds) | Description \n" +
                lines +
                "\n|====\n";

        final String perfDocumentation = "\n== Performances\n\n" +
                "We compare performance of tests using different approaches.\n" +
                "\nUsing Git, we can verify documentation on each test or not.\n" +
                "When we run all tests, this verification is done at the end by script that launch tests.\n" +
                "To have total time using Git without verification on each test, we should add time of global checking.\n" +
                "This step is fast, it's just a shell script with some git commands.\n" +
                "\ninclude::" + perfAttributesFileName + "[]\n" +
                timeFromReports;

        return perfDocumentation +
                getMethodDocumentation(this.getClass().getPackage().getName());
    }

    protected String getMethodDocumentation(String packageToScan) {
        Set<Method> testMethods = getAnnotatedMethod(Test.class, packageToScan);

        Map<Class<?>, List<Method>> methodsByClass;
        try {
            methodsByClass = testMethods.stream()
                    .filter(m -> TestCategory.Cat.Simple.equals(m.getDeclaringClass().getAnnotation(TestCategory.class).category()))
                    .collect(Collectors.groupingBy(m -> m.getDeclaringClass()));
        } catch (Exception e) {
            throw e;
        }
        String testsDocumentation = methodsByClass.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().getSimpleName()))
                .map(e -> {
                    return new ClassDocumentation(new AsciidocFormatter(),
                            m -> new DocPath(m).approved().from(Config.DOC_PATH),
                            m -> e.getValue().contains(m),
                            c -> c.isAnnotationPresent(Nested.class)
                    ).getClassDocumentation(e.getKey(), 2);
                })
                .collect(Collectors.joining("\n"));

        return testsDocumentation;
    }

    private Path getAbsoluteDocPath() {
        return getProjectPath().resolve(getDocRootPath());
    }

    private Path getDocRootPath() {
        return Paths.get("src", "test", "docs");
    }

    private Path getProjectPath() {
        return new PathProvider().getProjectPath();
    }

    protected Set<Method> getAnnotatedMethod(Class<? extends Annotation> annotation, String packageToScan) {
        Reflections reflections = new Reflections(packageToScan, new MethodAnnotationsScanner());
        return reflections.getMethodsAnnotatedWith(annotation);
    }

    public String parseTestReport(Map<String, String> perfAttributes) throws ParserConfigurationException, SAXException, IOException, ClassNotFoundException {
        final PathProvider pathProvider = new PathProvider();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        final Path reportPath = pathProvider.getProjectPath().resolve(Paths.get("target", "surefire-reports"));
        final List<File> testReports = Stream.of(reportPath.toFile().listFiles())
                .filter(file -> !file.isDirectory())
                .filter(file -> file.getName().startsWith("TEST-"))
                .collect(Collectors.toList());

        List<String> lines = new ArrayList<>();
        for (File report : testReports) {
            System.out.println(report.getName());
            final Node testsuite = builder.parse(report).getElementsByTagName("testsuite").item(0);

            final NamedNodeMap attributes = testsuite.getAttributes();
            final String timeItem = attributes.getNamedItem("time").getNodeValue();
            final String classname = attributes.getNamedItem("name").getNodeValue();
            final Class<?> testClass = Class.forName(classname);
            final DisplayName annotation = testClass.getAnnotation(DisplayName.class);
            final String category = testClass.getAnnotation(TestCategory.class).category().name();
            final String attributeKey = category + "-" + testClass.getSimpleName();
            lines.add("| " + category + " | " + testClass.getSimpleName() + " | {" + attributeKey + "} | " + annotation.value());
            perfAttributes.put(attributeKey, timeItem);
        }
        return lines.stream()
                .sorted()
                .collect(Collectors.joining("\n"));
    }

    public static void generateTestDocumentation(DocAsTestBase doc) {
        final LocalDate now = LocalDate.now();
        final int current_year = now.getYear();
        final int age = 45;
        final String firstName = "John";
        final String lastName = "Doe";
        final int yearOfBirth = current_year - age;
        final Person person = new Person(firstName, lastName, LocalDate.of(yearOfBirth, 11, 23));

        doc.write("With an instance of Person:\n\n");
        doc.write("* *First Name*: " + firstName + "\n");
        doc.write("* *Last Name*: " + lastName + "\n");
        doc.write("* *Year of birth*: " + yearOfBirth + "\n");
        doc.write("\nResults calling methods are:\n\n");
        doc.write("* *age()*: " + person.age() + " (_if we are in " + now.getYear() + "_)\n");
        doc.write("* *toString()*: " + person.toString() + "\n");
    }

    public String build() {
        return formatter.paragraphSuite(
                new Options(formatter).withCode(),
                getHeader(),
                getContent()
        );
    }

    @Override
    public void produce() throws IOException {
        new Document(this.build()).saveAs(Config.DOC_PATH.resolve("Documentation.adoc"));
    }

    public static void main(String... args) throws IOException {
        new BasicDocumentation().produce();
    }

}
