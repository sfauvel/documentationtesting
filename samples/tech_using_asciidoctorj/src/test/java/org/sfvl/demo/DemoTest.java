package org.sfvl.demo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.sfvl.doctesting.junitinheritance.ApprovalsBase;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.doctesting.writer.ClassDocumentation;

import java.io.FileWriter;
import java.nio.file.Path;

/**
 * Demo of a simple usage to generate documentation.
 */
public class DemoTest extends ApprovalsBase {
    @AfterAll
    public static void afterAll(TestInfo testInfo) throws Exception {
        final Class<?> clazz = testInfo.getTestClass().get();

        final ClassDocumentation classDocumentation = new ClassDocumentation();
        final String content = String.join("\n",
                ":nofooter:",
                classDocumentation.getClassDocumentation(clazz));

        final Path docFilePath = new DocPath(clazz).approved().path();

        try (FileWriter fileWriter = new FileWriter(docFilePath.toFile())) {
            fileWriter.write(content);
        }
    }

    /**
     * When adding two simple numbers, the java operator '+' should return the sum of them.
     */
    @Test
    @DisplayName("Adding 2 simple numbers")
    public void should_be_5_when_adding_2_and_3() {
        int a = 2;
        int b = 3;
        write(String.format("%d + %d = %d", a, b, a + b));
    }
}
