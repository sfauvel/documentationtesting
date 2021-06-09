package org.sfvl.doctesting;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaModel;
import org.approvaltests.Approvals;
import org.approvaltests.namer.ApprovalNamer;
import org.approvaltests.writers.ApprovalTextWriter;
import org.junit.*;
import org.junit.rules.TestName;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Base class for test.
 */
public class ApprovalsJUnit4 {

    private static final PathProvider pathProvider = new PathProvider();
    private static final JavaProjectBuilder builder = new JavaProjectBuilder();
    static {
        builder.addSourceTree(new File("src/test/java"));
    }

    @Rule
    public TestName testName = new TestName();

   private StringBuffer sb = new StringBuffer();

    protected void write(String... lines) {
        sb.append(Arrays.stream(lines).collect(Collectors.joining("\n")));
    }

    /**
     * Give path where docs are generated.
     *
     * @return
     */
    protected Path getDocPath() {
        return ApprovalsJUnit4.pathProvider.getProjectPath().resolve(Paths.get("src", "test", "docs"));
    }


    protected void approved(DocumentationNamer documentationNamer, String content) {
        ApprovalNamer approvalNamer = new ApprovalNamer() {

            @Override
            public String getApprovalName() {
                return documentationNamer.getApprovalName();
            }

            @Override
            public String getSourceFilePath() {
                return documentationNamer.getSourceFilePath();
            }
        };

        Approvals.verify(
                new ApprovalTextWriter(content, "adoc"),
                approvalNamer,
                Approvals.getReporter());
    }


    public static String fileName;
    public static List<String> testFileName;
    public static List<Method> testMethods;
    private static DocumentationNamer lastNamer;
    private static Class<?> testClass;


    @BeforeClass
    public static void resetDoc() {
        fileName = null;
        testFileName = new ArrayList<>();
        testMethods = new ArrayList<>();
    }

    @Before
    public void initDoc() {
        fileName = "_" + this.getClass().getSimpleName();
    }

    @After
    public void approvedAfterTest() throws NoSuchMethodException {

        final String methodName = testName.getMethodName();
        final Method testMethod = this.getClass().getDeclaredMethod(methodName);
        String content = String.join("\n\n",
                "= " + formatTitle(methodName),
                getComment(this.getClass(), methodName),
                sb.toString());

        testFileName.add(fileName + "." + testMethod.getName() + ".approved.adoc");
        testMethods.add(testMethod);

        lastNamer = new DocumentationNamer(getDocPath(), testMethod);
        testClass = testMethod.getDeclaringClass();

        approved(lastNamer, content);
    }

    @AfterClass
    public static void writeTestDoc() throws IOException {
        final File fileName = new File(ApprovalsJUnit4.fileName + ".adoc");

        final String sourceFilePath = lastNamer.getSourceFilePath();

        JavaClass javaClassUnderTest = getJavaClassUnderTest();


        try (final FileWriter fileWriter = new FileWriter(sourceFilePath+fileName)) {

            final String content = getMethodsInOrder(testMethods)
                    .map(method -> String.format("_%s.%s.approved.adoc", method.getDeclaringClass().getSimpleName(), method.getName()))
                    .map(name -> String.format("include::%s[leveloffset=+2]", name))
                    .collect(Collectors.joining("\n"));

            fileWriter.write("= " + javaClassUnderTest.getSimpleName() + "\n:toc:\n\n"
                    + "== Description\n\n"
                    + Optional.ofNullable(javaClassUnderTest.getComment()).orElse("")
                    + "\n\n"
                    + "== Examples\n\n"
                    + content);
        }
    }

    private static JavaClass getJavaClassUnderTest() {
        final String simpleName = testClass.getCanonicalName();
        final String s = simpleName
                .replaceFirst("DocTest$", "")
                .replaceFirst("Test$", "");
        System.out.println("Class under test: " + s);

        JavaProjectBuilder builder = new JavaProjectBuilder();
        builder.addSourceTree(new File("src/main/java"));

        return builder.getClassByName(s);
    }

    private String formatTitle(String methodName) {
        String title = methodName
                .replace("_", " ");

        return title.substring(0, 1).toUpperCase() + title.substring(1);
    }

    public String getComment(Class<?> clazz, String methodName) {

        JavaClass javaClass = builder.getClassByName(clazz.getCanonicalName());

        JavaMethod method = javaClass.getMethod(methodName, Collections.emptyList(), false);
        return Optional.ofNullable(method.getComment()).orElse("");
    }

    private static Stream<Method> getMethodsInOrder(List<Method> testMethods) {
        Map<String, Method> methodsByName = testMethods.stream().collect(Collectors.toMap(
                m -> m.getName(),
                m -> m
        ));

        JavaProjectBuilder builder = createJavaProjectBuilderWithTestPath();

        Method firstMethod = testMethods.get(0);
        JavaClass javaClass = builder.getClassByName(firstMethod.getDeclaringClass().getCanonicalName());

        return javaClass.getMethods().stream()
                .filter(m -> methodsByName.containsKey(m.getName()))
                .sorted(Comparator.comparingInt(JavaModel::getLineNumber))
                .map(m -> methodsByName.get(m.getName()));

    }

    private static JavaProjectBuilder createJavaProjectBuilderWithTestPath() {
        JavaProjectBuilder builder = new JavaProjectBuilder();

        final Path testPath = pathProvider.getProjectPath().resolve(Paths.get("src", "test", "java"));
        builder.addSourceTree(testPath.toFile());
        return builder;
    }

}
