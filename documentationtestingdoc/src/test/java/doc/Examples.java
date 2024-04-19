package doc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.Formatter;
import org.sfvl.docformatter.asciidoc.AsciidocFormatter;
import org.sfvl.doctesting.junitextension.HtmlPageExtension;
import org.sfvl.doctesting.utils.NoTitle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import tools.CustomApprovalsExtension;
import tools.CustomPageExtension;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ExtendWith(CustomPageExtension.class)
public class Examples {

    @RegisterExtension
    public static CustomApprovalsExtension doc = new CustomApprovalsExtension();
    Formatter formatter = new AsciidocFormatter();

    /**
     * Several examples are provided to show how we can use it and what it can produce.
     */
    @Test
    @NoTitle
    public void intro() {
    }

    /**
     * Those samples show different documentation that can be generated.
     * It allows to give ideas on different ways of presenting information
     */
    @Test
    @DisplayName(value = "Functional demos")
    public void demo_list() {
        try {
            final Path samplesPath = doc.getProjectPath().getParent().resolve("samples");
            final List<Path> samples = Files.list(samplesPath)
                    .filter(Files::isDirectory)
                    .filter(path -> path.getFileName().toString().startsWith("demo_"))
                    .sorted()
                    .collect(Collectors.toList());

            reorderPuttingFirst(samples, samplesPath,
                    "demo_minimal",
                    "demo_trivia");

            final String demos = samples.stream()
                    .map(this::addDemo)
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
            final String demos = Files.list(doc.getProjectPath().getParent().resolve("samples"))
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

    /**
     * Real projects that use this approch.
     */
    @Test
    @DisplayName(value = "Real projects")
    public void real_projects_list() {
        doc.write("\n * link:" + "https://sfauvel.github.io/documentationtesting/documentationtesting/index.html"
                + "[DocumentationTesting Java library]: "
                + "Library to implements this approach in Java." + " \n");

        doc.write("\n * link:" + "https://gcollic.github.io/ansi-console-to-html/"
                + "[ANSIConsoleToHtml]: "
                + "A `.NET` library to convert ANSI console output to HTML." + " \n");

        doc.write("\n * link:" + "https://github.com/gcollic/nmermaid/blob/main/Nmermaid.DocAsTests/Docs/FlowchartDoc.All.verified.md"
                + "[Mermaid C# library]: "
                + "This library helps generate `Mermaid` diagram from `C#`." + " \n");


    }


    private String addDemo(Path modulePath) {
        String moduleName = modulePath.getFileName().toString();
        Optional<String> description = generatePomDescription(modulePath);
        if (!description.isPresent()) {
            description = generateDocumentationDescription(modulePath);
        }

        return "\n * link:" + (Paths.get("{ROOT_PATH}", "..", moduleName, "index.html").toString())
                + "[" + moduleName + "]: "
                + description.orElse("no description") + " \n";
    }

    private void reorderPuttingFirst(List<Path> collect, Path samplesPath, String... samples) {
        final List<String> demo_in_order = Arrays.asList(samples);
        Collections.reverse(demo_in_order);
        for (String demo : demo_in_order) {
            final Path resolve = samplesPath.resolve(demo);
            if (collect.remove(resolve)) {
                collect.add(0, resolve);
            } else {
                System.out.println("WARNING: Try to reorder an unknown demo: " + demo);
            }
        }
    }

    private Optional<String> generatePomDescription(Path modulePath) {
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
