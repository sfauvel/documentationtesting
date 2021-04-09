package org.sfvl.doctesting.demo;

import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.utils.ClassFinder;
import org.sfvl.doctesting.utils.PathProvider;
import org.sfvl.doctesting.writer.Classes;
import org.sfvl.doctesting.writer.DocumentProducer;
import org.sfvl.doctesting.writer.Options;

import java.nio.file.Path;
import java.nio.file.Paths;


public abstract class DemoDocumentation implements DocumentProducer {

    protected final Formatter formatter = new AsciidocFormatter();
    protected final String documentationTitle;
    protected final Path docRootPath;

    public DemoDocumentation() {
        this("Documentation");
    }

    public DemoDocumentation(String documentationTitle) {
        this(documentationTitle, Paths.get(""));
    }

    public DemoDocumentation(String documentationTitle, Path docRootPath) {
        this.documentationTitle = documentationTitle;
        this.docRootPath = docRootPath;
    }

    public String getDocumentationTitle() {
        return documentationTitle;
    }

    protected String getHeader() {
        final Path readmePath = new PathProvider().getProjectPath().resolve(Paths.get("readme.adoc"));
        return "\n" + (readmePath.toFile().exists()
                ? "include::../../../readme.adoc[leveloffset=+1]"
                : "= " + documentationTitle) + "\n";
    }

    public String getContent() {
        final PathProvider pathProvider = new PathProvider();
        final Path projectFolderPath = pathProvider.getGitRootPath().relativize(pathProvider.getProjectPath());
        return "NOTE: The examples shown here are generated from the source code.\n" +
                "They therefore represent the behavior of the application at any times.\n" +
                "Non regression is ensured by checking the absence of change in this document.\n" +
                "Learn more here link:{github-pages}[]\n\n" +
                "View source of project on link:{github-repo}/" + projectFolderPath.toString() + "[Github]\n\n";
    }

    public String build() {
        return formatter.paragraphSuite(
                new Options(formatter).withCode(),
                formatter.title(1, documentationTitle),
                getContent(),
                new Classes(formatter).includeClasses(
                        docRootPath,
                        new ClassFinder().testClasses(this.getClass().getPackage())
                )
        );
    }

}