package fr.sfvl.documentationtesting;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class DocGenerator {
    private final Formatter formatter;
    private final Path docPath;
    private final String docName = "demo.adoc";

    public DocGenerator(Formatter formatter) {
        this.formatter = formatter;

        docPath = getProjectPath().resolve(Path.of("target", "adoc")).toAbsolutePath();
    }

    /**
     * Get path of the project as a module.
     * To be compatible in different system, a File is created from the path and then retransform to a path.
     * @return
     */
    protected final Path getProjectPath() {
        Path classesPath = new File(this.getClass().getClassLoader().getResource("").getPath()).toPath();
        return classesPath.getParent().getParent();
    }


    private void execute() throws IOException {

        final String demos = Files.list(getProjectPath().getParent())
                .filter(p -> Files.isDirectory(p))
                .map(p -> p.getFileName().toString())
                .filter(name -> name.startsWith("demo_"))
                .map(demo -> addDemo(demo))
                .collect(Collectors.joining());

        String doc = formatter.standardOptions() +
                ":nofooter:\n" +
                ":fulldoc:\n" +
                formatter.tableOfContent() +
                formatter.include(docPath.relativize(Paths.get("README.adoc").toAbsolutePath()).toString()) +
                formatter.include("ways_to_implement.adoc");

        Files.createDirectories(docPath);

        generateDocFile(docName, doc);
        generateDocFile("demo_list.adoc", demos);

    }

    private String addDemo(String module) {
        return "\n * link:"+(Paths.get(".", module, "index.html").toString())+"[" + module + "]\n";
    }

    private void generateDocFile(String docName, String doc) throws IOException {
        Files.createDirectories(docPath);

        File adocFile = docPath.resolve(docName).toFile();
        try (PrintWriter writer = new PrintWriter(new FileOutputStream(adocFile))) {
            writer.append(doc);
        }

    }

    public static void main (String... args) throws IOException {

        new DocGenerator(new Formatter.AsciidoctorFormatter()).execute();
    }
}
