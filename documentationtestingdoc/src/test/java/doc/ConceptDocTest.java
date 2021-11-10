package doc;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docextraction.MethodReference;
import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.doctesting.utils.NoTitle;
import tools.CustomApprovalsExtension;
import tools.MyFormatter;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class ConceptDocTest extends MyFormatter {

    @RegisterExtension
    static CustomApprovalsExtension doc = new CustomApprovalsExtension();

    @AfterAll
    static public void writeIndexPage() throws IOException, NoSuchMethodException {
        final Method method = MethodReference.getMethod(ConceptDocTest::index);

        final DocPath docPath = new DocPath(method);
        String content = String.join("\n",
                doc.getDocWriter().defineDocPath(Paths.get(".")),
                ":nofooter:",
                "include::" + docPath.approved().from(Config.DOC_PATH).toString() + "[]");

        final Path indexFile = Config.DOC_PATH.resolve("index.adoc");
        try (FileWriter fileWriter = new FileWriter(indexFile.toFile())) {
            fileWriter.write(content);
        }
    }

    @Test
    @NoTitle
    public void index() {
        // 4 parts of the documentation
        // tutorial
        // howto
        // explanation: the first introduction
        // reference
        final String README_FILE = "README.adoc";
        String readme;
        try {
            readme = Files.lines(doc.getProjectPath().getParent().resolve(README_FILE)).collect(Collectors.joining("\n"));
        } catch (IOException e) {
            readme = String.format("Error reading `%s` file.\n%s", README_FILE, e.getMessage());
        }
        doc.write(paragraph(
                standardOptions(),
                ":nofooter:",
                ":fulldoc:",
                readme,
                "Here, what resources you can find on this site",
                table("DocumentationTestingDoc.intro.bis",
                        $(
                                $(explanationCell(), tutorialCell()),
                                $(howtoCell(), referenceCell())
                        )
                ),
                fullDocumentationLink(),
                styleSection()
        ));
    }

    private String explanationCell() {
        //"Understanding-oriented"
        return paragraph(
                linkToPage(Explanation.class, "More explanation"),
                "You want to have more information of the concept, the idea, the history, ..."
        );
    }

    private String tutorialCell() {
        //"Learning-oriented"
        return paragraph(
                linkToPage(Tutorial.class, "Just try it"),
                "We explain how to experiment whatever your language."
        );
    }

    private String howtoCell() {
        return paragraph(
                linkToPage(Examples.class, "Some examples"),
                //"[.subtitle]",
                //"Problem-oriented",
                "",
                "We will present some documentation.",
                "That shows how we can present some information in a documentation that is also a test.",
                ""
        );

    }

    private String referenceCell() {
        return paragraph(
                linkToPage(Reference.class, "Tools"),
                //"[.subtitle]",
                //"Information-oriented",
                "",
                "Tools, full discussions and choices",
                ""
        );

    }

    private String titleWithLink(String link, String title) {
        String titleWithLink = paragraph(
                "++++",
                "<a href=\"" + link + "\" style=\"display: inline-block;\">",
                "<div style=\"height:100%;width:100%\">",
                "++++",
                "== " + title,
                "++++",
                "</div>",
                "</a>",
                "++++");
        return titleWithLink;
    }

    private String styleSection() {
        return style(paragraph(
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
                ".DocumentationTestingDoc.intro.bis td {",
                "    background-color:#fdfff0;",
                "    padding:2em;",
                "}",
//                ".DocumentationTestingDoc.intro.bis td:hover {",
//                "    background-color:#fcfddc;",
//                "}",
                ".DocumentationTestingDoc.intro.bis td h2 {",
                "    margin-top:0 !important;",
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
                "#content {",
                "max-width: 75%;",
                "}"));
    }

    private String fullDocumentationLink() {
        return String.join("\n",
                "Full project documentation is here: {github-pages}",
                "",
                "Repository github: {github-repo}"
        );
    }

}
