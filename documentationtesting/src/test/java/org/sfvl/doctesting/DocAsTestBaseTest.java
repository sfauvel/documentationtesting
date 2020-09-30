package org.sfvl.doctesting;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mockito;
import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.docformatter.AsciidocFormatterTest.TestOption;
import org.sfvl.docformatter.Formatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

class DocAsTestBaseTest extends ApprovalsBase {

    private static Formatter formatter = new AsciidocFormatter();
    DocAsTestBase docAsTest = new DocAsTestBase() {
        @Override
        protected void approvalAfterTestSpecific(String content, DocumentationNamer documentationNamer) throws Exception {
        }
    };

    public void simple_method_to_format_title() {

    }

    @Test
    @TestOption(includeMethodDoc = "formatTitle")
    @DisplayName("Formatted title")
    public void title(TestInfo testinfo) throws NoSuchMethodException {
        final String displayName = "Get display name";
        JavaProjectBuilder builder = new JavaProjectBuilder();
        final JavaClass testInfoJavaClass = builder.getClassByName(TestInfo.class.getCanonicalName());

        final Optional<TestOption> annotation = Optional.ofNullable(testinfo.getTestMethod()
                .get()
                .getAnnotation(TestOption.class));
        final JavaClass classByName = builder.getClassByName(TestInfo.class.getCanonicalName());
        annotation.map(TestOption::includeMethodDoc)
                .filter(name -> !name.isEmpty())
                .map(methodName -> getComment(DocAsTestBase.class, methodName, Arrays.asList(testInfoJavaClass)))
                .ifPresent(doc -> write(doc + "\n"));


        TestInfo testInfo = Mockito.mock(TestInfo.class);
        Mockito.when(testInfo.getTestMethod()).thenReturn(Optional.of(DocAsTestBaseTest.class.getMethod("simple_method_to_format_title")));

        List<List<? extends Object>> table = new ArrayList<>();
        table.add(Arrays.asList("Display name", "Method name", "Title"));
        Mockito.when(testInfo.getDisplayName()).thenReturn(displayName);
     //   writeFormatTitle(testInfo);
        table.add(getFormatTitleLine(testInfo));

        Mockito.when(testInfo.getDisplayName()).thenReturn("simple_method_to_format_title()");
       // writeFormatTitle(testInfo);
        table.add(getFormatTitleLine(testInfo));

        Mockito.when(testInfo.getDisplayName()).thenReturn("display_name_is_not_method_name()");
        table.add(getFormatTitleLine(testInfo));

        write(formatter.tableWithHeader(table));
    }

    public void writeFormatTitle(TestInfo testInfo) {
        write("\n\n");
        write(String.format("With display name from _testInfo_ is '%s' +\n", testInfo.getDisplayName()));
        write(String.format("and method name is '%s' +\n", testInfo.getTestMethod().get().getName()));
        write(String.format("then formatTitle is '%s'\n\n", docAsTest.formatTitle(testInfo)));
    }

    public List<String> getFormatTitleLine(TestInfo testInfo) {
        return Arrays.asList(
                testInfo.getDisplayName(),
                testInfo.getTestMethod().get().getName(),
                docAsTest.formatTitle(testInfo)
                );
    }
}