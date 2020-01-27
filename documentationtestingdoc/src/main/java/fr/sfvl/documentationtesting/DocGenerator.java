package fr.sfvl.documentationtesting;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DocGenerator {
    private final Formatter formatter;
    private final Path docPath;
    private final String docName = "demo.adoc";

    public DocGenerator(Formatter formatter) {
        this.formatter = formatter;


        final Path docRootPath = Paths.get(this.getClass().getClassLoader().getResource("").getPath());
        docPath = docRootPath.resolve(Path.of("..", "adoc")).toAbsolutePath();

    }


    private void execute() throws IOException {

        String doc = formatter.standardOptions() +
                formatter.tableOfContent() +
                formatter.title(1, "Documentation testing") +
                formatter.include(docPath.relativize(Paths.get("..", "README.adoc").toAbsolutePath()).toString()) +
                formatter.title(2, "Examples") +
                addDemo("demo_basic") +
                addDemo("demo_fizzbuzz") +
                addDemo("demo_gildedrose")
                ;

        Files.createDirectories(docPath);

        generateReport(doc);

    }

    private String addDemo(String module) {
        return formatter.title(3, "Demo basic")
                + "\nlink:"+(Paths.get(".", module, "index.html").toString())+"[" + module + "]\n";
    }

    private void generateReport(String doc) throws IOException {
        System.out.println(doc);
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
