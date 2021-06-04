package fr.sfvl;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.FindLambdaMethod;
import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.doctesting.utils.DocWriter;
import org.sfvl.doctesting.utils.NoTitle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Collectors;

public class ConceptDocTest extends MyFormatter {

    @RegisterExtension
    static ApprovalsExtension doc = new ApprovalsExtension(new DocWriter() {
        @Override
        public String formatOutput(Class<?> clazz) {
            return String.join("\n",
                    ":notitle:",
                    super.formatOutput(clazz));
        }
    });

    @AfterAll
    static public void writeIndexPage() throws IOException, NoSuchMethodException {
        final Method method = FindLambdaMethod.getMethod(ConceptDocTest::index);

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

    /**
     * Get path of the project as a module.
     * To be compatible in different system, a File is created from the path and then retransform to a path.
     *
     * @return
     */
    protected final Path getProjectPath() {
        Path classesPath = new File(this.getClass().getClassLoader().getResource("").getPath()).toPath();
        return classesPath.getParent().getParent();
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
            readme = Files.lines(getProjectPath().getParent().resolve(README_FILE)).collect(Collectors.joining("\n"));
        } catch (IOException e) {
            readme = String.format("Error reading `%s` file.\n%s", README_FILE, e.getMessage());
        }
        doc.write(paragraph(
                standardOptions(),
                ":nofooter:",
                ":fulldoc:",
                readme,
                "Here, what you can find as resources on this site",
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
        return paragraph(
                "== " + linkToMethod(ConceptDocTest::explanation, "More explanation"),
                //"[.subtitle]",
                //"Understanding-oriented",
                "",
                "You want to have more information of the concept, the idea, the history, ...",
                "");
    }

    private String tutorialCell() {

        return paragraph(
                "== " + linkToMethod(ConceptDocTest::tutorial, "Just try it"),
                //"[.subtitle]",
                //"Learning-oriented",
                "",
                "We explain how to experiment whatever your language.",
                ""
        );
    }

    private String howtoCell() {
        return paragraph(
                "== " + linkToMethod(ConceptDocTest::howTo, "Some examples"),
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
                "== " + linkToMethod(ConceptDocTest::reference, "Tools"),
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

    @Test
    @NoTitle
    public void explanation() {
        final DocPath docPath = new DocPath(Paths.get(""), "explanation");
        final Path from = docPath.resource().from(this.getClass());
        doc.write(String.format("include::%s[]", from.toString()));

    }

    @Test
    @NoTitle
    public void tutorial() {
        final DocPath docPath = new DocPath(Paths.get(""), "tutorial");
        final Path from = docPath.resource().from(this.getClass());
        doc.write(String.format("include::%s[]", from.toString()));
    }

    @Test
    @NoTitle
    public void development() {
        final DocPath docPath = new DocPath(Paths.get(""), "development");
        final Path from = docPath.resource().from(this.getClass());
        doc.write(String.format("include::%s[]", from.toString()));
    }

    /**
     * Several examples are provided to show how we can use it and what it can produce.
     */
    @Test
    public void howTo() {
        doc.write(paragraph(
                include(new DocPath(FindLambdaMethod.getMethod(ConceptDocTest::demo_list)).approved().from(this.getClass()).toString()),
                include(new DocPath(FindLambdaMethod.getMethod(ConceptDocTest::tech_list)).approved().from(this.getClass()).toString())
        ));
    }

    /**
     * In this page, you will find links to resources that help you to implement a documentation that can be used as a non regression test.
     */
    @Test
    public void reference() {

        doc.write(paragraph(
                //"[.subtitle]",
                //"Information-oriented",
                "",
                listItems(
                        "Documentation of the concept: link:{github-pages}[]",
                        "Library for Java: link:{github-pages}/documentationtesting[DocumentationTesting]",
                        "To get source and examples: link:{github-repo}[]",
                        "A plugin for IntelliJ is provided in {github-repo}/approvalsDocPlugin[approvalsDocPlugin] folder")
        ));
    }

    @Test
    @NoTitle
    public void best_practice() {
        // With @NoTitle, there is no need to change leveloffset, otherwise add [leveloffset=+1]
        final DocPath docPath = new DocPath(Paths.get(""), "best_practice");
        final Path from = docPath.resource().from(this.getClass());
        doc.write(String.format("include::%s[]", from.toString()));
    }

    @Test
    @NoTitle
    public void ways_to_implement() {
        // With @NoTitle, there is no need to change leveloffset, otherwise add [leveloffset=+1]
        final DocPath docPath = new DocPath(Paths.get(""), "ways_to_implement");
        final Path from = docPath.resource().from(this.getClass());
        doc.write(String.format("include::%s[]", from.toString()));
    }

    /**
     * Those samples show different documentation that can be generated.
     * It allows to give ideas on different ways of presenting information
     */
    @Test
    @DisplayName(value = "Functional demos")
    public void demo_list() {
        try {
            final String demos = Files.list(getProjectPath().getParent().resolve("samples"))
                    .filter(p -> Files.isDirectory(p))
                    .filter(path -> path.getFileName().toString().startsWith("demo_"))
                    .sorted()
                    .map(demo -> addDemo(demo))
                    .collect(Collectors.joining("\n"));
            doc.write(demos);
        } catch (IOException e) {
            doc.write("Error listing demo samples files.",
                    e.getMessage());
        }
    }

    /**
     * These examples show different approaches related to the technical environment.
     * It could be about languages, tools or some specifics cases.
     */
    @Test
    @DisplayName(value = "Technical demos")
    public void tech_list() {
        try {
            final String demos = Files.list(getProjectPath().getParent().resolve("samples"))
                    .filter(p -> Files.isDirectory(p))
                    .filter(path -> path.getFileName().toString().startsWith("tech_"))
                    .sorted()
                    .map(demo -> addDemo(demo))
                    .collect(Collectors.joining("\n"));
            doc.write(demos);
        } catch (IOException e) {
            doc.write("Error listing tech samples files.",
                    e.getMessage());
        }
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
        return "Full project documentation is here: https://sfauvel.github.io/documentationtesting/";
    }


    private String addDemo(Path modulePath) {
        String moduleName = modulePath.getFileName().toString();
        Optional<String> description = generatePomDescriptionXXX(modulePath);
        if (!description.isPresent()) {
            description = generateDocumentationDescription(modulePath);
        }

        return "\n * link:" + (Paths.get("{ROOT_PATH}", "..", moduleName, "index.html").toString())
                + "[" + moduleName + "]: "
                + description.orElse("no description") + " \n";
    }

    private Optional<String> generatePomDescriptionXXX(Path modulePath) {
        Element root = null;
        try {
            root = parsePom(modulePath).getDocumentElement();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            return Optional.empty();
        }
        return Optional.of(root)
                .map(r -> r.getElementsByTagName("description"))
                .map(d -> d.item(0))
                .map(i -> i.getTextContent());
    }

    public Optional<String> generatePomDescription(Path module)
            throws ParserConfigurationException, IOException, SAXException {

        Element root = parsePom(module).getDocumentElement();

        return Optional.of(root)
                .map(r -> r.getElementsByTagName("description"))
                .map(d -> d.item(0))
                .map(i -> i.getTextContent());
    }

    private Optional<String> generateDocumentationDescription(Path modulePath) {
        final Path docFile = modulePath.resolve("docs").resolve("Documentation.adoc");

        final String prefix = ":description:";
        try {
            return Files.lines(docFile)
                    .filter(line -> line.startsWith(prefix))
                    .findFirst()
                    .map(line -> line.substring(prefix.length()).trim());
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private static Document parsePom(Path module)
            throws ParserConfigurationException, SAXException, IOException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        return builder.parse(module.resolve("pom.xml").toFile());
    }

}
