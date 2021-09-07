package org.sfvl.samples.generateHtml;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.NotIncludeToDoc;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.test_tools.OnlyRunProgrammatically;

import java.io.FileWriter;
import java.io.IOException;

@NotIncludeToDoc
@OnlyRunProgrammatically
public class HtmlTest {
    @RegisterExtension
    static final ApprovalsExtension doc = new SimpleApprovalsExtension();

    @AfterAll
    public static void generatePage(TestInfo info) throws IOException {
        final DocPath docPath = new DocPath(info.getTestClass().get());
        String includeContent = String.join("\n",
                ":toc: left",
                ":nofooter:",
                "",
                String.format("include::%s[]", docPath.approved().fullname()));

        try (FileWriter fileWriter = new FileWriter(docPath.page().path().toFile())) {
            fileWriter.write(includeContent);
        }
    }

    @Test
    public void test_A() {
        doc.write("In my *test*");
    }

}
