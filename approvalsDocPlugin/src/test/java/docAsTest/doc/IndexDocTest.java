package docAsTest.doc;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.sfvl.docformatter.asciidoc.AsciidocFormatter;
import org.sfvl.doctesting.junitextension.HtmlPageExtension;
import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.doctesting.utils.NoTitle;
import org.sfvl.doctesting.writer.DocWriter;
import tools.ApprovalsJUnit4;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@DisplayName("DocAsTest plugin")
@RunWith(JUnit4.class)
public class IndexDocTest extends ApprovalsJUnit4 {

    AsciidocFormatter formatter = new AsciidocFormatter();

    @AfterClass
    public static void tearDown() throws IOException {
        new HtmlPage().generate(IndexDocTest.class);
    }

    @Test
    @NoTitle
    public void test_content() {
        write(
                "This plugin improve the user experience developing with DocAsTest.",
                "",
                "Features are:",
                formatter.listItems(
                        "Approved one or more files",
                        "Switch between `Java`, `approved` and `received` files",
                        "Compare `approved` and `received` files"
                ),
                "",
                "Those features are accessible from contextual menu on project explorer or on editor.",
                "",
                ""
        );
        write(formatter.include(approvedDocPathStringFrom(ShortCutTest.class, this.getClass())));
    }

    private String approvedDocPathStringFrom(Class<?> targetClass, Class<?> fromClass) {
        return DocPath.toAsciiDoc(new DocPath(targetClass).approved().from(fromClass));
    }

}

class HtmlPage extends HtmlPageExtension {
    @Override
    public String content(Class<?> clazz) {
        return String.join("\n",
                new DocWriter<AsciidocFormatter>(new AsciidocFormatter()).defineDocPath(Paths.get(".")),
                ":nofooter:",
                super.content(clazz));
    }

    @Override
    public Path getFilePath(Class<?> clazz) {
        return Config.DOC_PATH.resolve("index.adoc");
    }
}
