package fr.sfvl.documentationtesting;

import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.docformatter.Formatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Collectors;

public class DocGenerator {
    private final Formatter formatter;
    private final Path docPath;
    private final String docName = "demo.adoc";

    public DocGenerator(Formatter formatter) {
        this.formatter = formatter;

        docPath = getProjectPath().resolve(Paths.get("target", "classes", "docs")).toAbsolutePath();
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


    private void execute() throws IOException {
        System.out.println(getProjectPath());
        final String demos = Files.list(getProjectPath().getParent().resolve("samples"))
                .filter(p -> Files.isDirectory(p))
                .filter(path -> path.getFileName().toString().startsWith("demo_"))
                .sorted()
                .map(demo -> addDemo(demo))
                .collect(Collectors.joining());

        final String demos_tech = Files.list(getProjectPath().getParent().resolve("samples"))
                .filter(p -> Files.isDirectory(p))
                .map(p -> p.getFileName().toString())
                .filter(name -> name.startsWith("tech_"))
                .sorted()
                .map(demo -> addDemo(demo))
                .collect(Collectors.joining());

        String doc = formatter.standardOptions() +
                ":nofooter:\n" +
                ":fulldoc:\n" +
                formatter.tableOfContent(4) +
                formatter.include(docPath.relativize(getProjectPath().getParent().resolve("README.adoc").toAbsolutePath()).toString()) +
                formatter.include("ways_to_implement.adoc");

        Files.createDirectories(docPath);

        generateDocFile(docName, doc);
        generateDocFile("demo_list.adoc", "\n=== Documentation produced\n\n" + demos);
        generateDocFile("tech_list.adoc", "\n=== Technical alternatives\n\n" + demos_tech);

    }

    private String addDemo(Path modulePath) {
        String moduleName = modulePath.getFileName().toString();
        String description = null;
        try {
            description = generatePomDescription(getProjectPath().getParent().resolve(moduleName));
        } catch (ParserConfigurationException | IOException | SAXException e) {
            description = "no description";
        }
        return "\n * link:" + (Paths.get(".", moduleName, "index.html").toString()) + "[" + moduleName + "]: " + description + " \n";
    }

    private String addDemo(String module) {
        String description = null;
        try {
            description = generatePomDescription(getProjectPath().getParent().resolve("samples").resolve(module));
        } catch (ParserConfigurationException | IOException | SAXException e) {
            description = "no description";
        }
        return "\n * link:" + (Paths.get(".", module, "index.html").toString()) + "[" + module + "]: " + description + " \n";
    }

    private void generateDocFile(String docName, String doc) throws IOException {
        Files.createDirectories(docPath);

        File adocFile = docPath.resolve(docName).toFile();
        try (PrintWriter writer = new PrintWriter(new FileOutputStream(adocFile))) {
            writer.append(doc);
        }

    }

    public String generatePomDescription(Path module)
            throws ParserConfigurationException, IOException, SAXException {

        Element root = parsePom(module).getDocumentElement();

        return Optional.of(root)
                .map(r -> r.getElementsByTagName("description"))
                .map(d -> d.item(0))
                .map(i -> i.getTextContent())
                .orElse("no description");

    }

    private static Document parsePom(Path module)
            throws ParserConfigurationException, SAXException, IOException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        return builder.parse(module.resolve("pom.xml").toFile());
    }

    public static void main(String... args) throws IOException {

        new DocGenerator(new AsciidocFormatter()).execute();
    }
}
