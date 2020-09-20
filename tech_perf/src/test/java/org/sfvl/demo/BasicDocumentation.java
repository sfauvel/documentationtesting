package org.sfvl.demo;

import org.junit.jupiter.api.DisplayName;
import org.sfvl.Person;
import org.sfvl.doctesting.DocAsTestBase;
import org.sfvl.doctesting.MainDocumentation;
import org.sfvl.doctesting.PathProvider;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BasicDocumentation extends MainDocumentation {

    @Override
    protected String getDocumentationContent(String packageToScan) {

        String lines  = "";
        try {
            lines = parseTestReport();
        } catch (Exception e) {
            new RuntimeException(e);
        }

        String timeFromReports = "\n[%autowidth]\n|====\n" +
                "| Class | Time in seconds) | Description \n" +
                lines +
                "\n|====\n";

        final String perfDocumentation = "\n== Performances\n\n" +
                "We compare performance of tests using different approaches.\n" +
                "\nUsing Git, we can verify documentation on each test or not.\n" +
                "When we run all tests, this verification is done at the end by script that launch tests.\n" +
                "To have total time using Git without verification on each test, we should add time of global checking.\n" +
                "This step is fast, it's just a shell script with some git commands.\n" +
                timeFromReports;

        return getHeader() +
                perfDocumentation +
                getMethodDocumentation(packageToScan);
    }

    public static void main(String... args) throws IOException {
        final BasicDocumentation generator = new BasicDocumentation();

        generator.generate("org.sfvl");
    }

    public static String parseTestReport() throws ParserConfigurationException, SAXException, IOException, ClassNotFoundException {
        final PathProvider pathProvider = new PathProvider();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        final Path reportPath = pathProvider.getProjectPath().resolve(Paths.get("target", "surefire-reports"));
        final Set<File> testReports = Stream.of(reportPath.toFile().listFiles())
                .filter(file -> !file.isDirectory())
                .filter(file -> file.getName().startsWith("TEST-"))
                .collect(Collectors.toSet());

        StringBuffer buffer = new StringBuffer();
        for (File report : testReports) {
            final NamedNodeMap attributes = builder.parse(report)
                    .getElementsByTagName("testcase")
                    .item(0)
                    .getAttributes();
            final String timeItem = attributes.getNamedItem("time").getNodeValue();
            final String classname = attributes.getNamedItem("classname").getNodeValue();
            final Class<?> testClass = Class.forName(classname);
            final DisplayName annotation = testClass.getAnnotation(DisplayName.class);
            buffer.append("| " + testClass.getSimpleName() + " | " + timeItem + " | " + annotation.value() + "\n");
        }
        return buffer.toString();
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
}
