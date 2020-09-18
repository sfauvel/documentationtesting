package org.sfvl.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sfvl.Person;
import org.sfvl.doctesting.DocAsTestBase;
import org.sfvl.doctesting.MainDocumentation;
import org.sfvl.doctesting.PathProvider;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BasicDocumentation extends MainDocumentation {

    @Override
    protected String getDocumentationContent(String packageToScan) {
        Set<Method> testMethods = getAnnotatedMethod(Test.class, packageToScan);

        final Map<Class<?>, List<Method>> methodsByClass = testMethods.stream().collect(Collectors.groupingBy(
                m -> m.getDeclaringClass()
        ));

        final String perfs = methodsByClass.keySet().stream()
                .map(clazz -> "include::" + clazz.getSimpleName() + ".adoc[]")
                .collect(Collectors.joining("\n"));

        final String perfDocumentation = "\n== Performances\n\n" +
                "We compare performance of tests using different approaches.\n" +
                "\nUsing Git, we can verify documentation on each test or not.\n" +
                "When we run all tests, this verification is done at the end by script that launch tests.\n" +
                "To have total time using Git without verification on each test, we should add time of global checking.\n" +
                "This step is fast, it's just a shell script with some git commands.\n" +
                "\n[%autowidth]\n|====\n" +
                perfs +
                "\n|====\n";

        return getHeader() +
                perfDocumentation +
                getMethodDocumentation(packageToScan);
    }

    public static void main(String... args) throws IOException {
        final BasicDocumentation generator = new BasicDocumentation();

        generator.generate("org.sfvl");
    }


    public static void writeDuration(Class<?> aClass, LocalDateTime begin, LocalDateTime end) throws IOException {
        final PathProvider pathProvider = new PathProvider();
        final int durationInMilliseconds = Duration.between(begin, end).getNano() / (1000 * 1000);
        System.out.println(aClass.getSimpleName()+ ": " + durationInMilliseconds +"ms");

        final DisplayName annotation = aClass.getAnnotation(DisplayName.class);

        final Path docPath = new MainDocumentation().getDocRootPath();
        final Path path = docPath.resolve(aClass.getSimpleName()+".adoc");
        try (FileWriter fileWriter = new FileWriter(path.toFile())) {
            fileWriter.write("| " + aClass.getSimpleName()
                    + " | " + durationInMilliseconds +"ms"
                    + " | " + annotation.value());
        }
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
        doc.write("* *age()*: " + person.age() + " (_if we are in "+now.getYear()+"_)\n");
        doc.write("* *toString()*: " + person.toString() + "\n");
    }
}
