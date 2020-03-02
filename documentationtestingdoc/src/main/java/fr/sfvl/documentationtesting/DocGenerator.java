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


        final Path docRootPath = Paths.get(this.getClass().getClassLoader().getResource("").getPath());
        //docPath = docRootPath.resolve(Path.of("..", "adoc")).toAbsolutePath();
        docPath = docRootPath.resolve(Path.of("docs")).toAbsolutePath();

    }


    private void execute() throws IOException {

        final Path rootPath = Paths.get(this.getClass().getClassLoader().getResource("").getPath());

        final String demos = Files.list(rootPath.resolve("../../.."))
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
