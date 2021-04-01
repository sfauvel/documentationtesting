package org.sfvl.doctesting;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DemoDocumentation extends DocumentationBuilder {
    public DemoDocumentation() {
        this("Documentation");
    }

    public DemoDocumentation(String documentationTitle) {
        this(documentationTitle, Paths.get(""));
    }

    public DemoDocumentation(String documentationTitle, Path docRootPath) {
        super(documentationTitle);
        withLocation(docRootPath);
        withClassesToInclude(new ClassFinder().testClasses(this.getClass().getPackage()));
        withStructureBuilder(DemoDocumentation.class,
                b -> b.getDocumentOptions(),
                b -> b.getHeader(),
                b -> b.getContent(),
                b -> includeClasses()
        );

    }

    protected String getHeader() {
        final Path readmePath = new PathProvider().getProjectPath().resolve(Paths.get("readme.adoc"));
        return "\n" + (readmePath.toFile().exists()
                ? "include::../../../readme.adoc[leveloffset=+1]"
                : "= " + getDocumentTitle()) + "\n";
    }

    protected String getContent() {
        final PathProvider pathProvider = new PathProvider();
        final Path projectFolderPath = pathProvider.getGitRootPath().relativize(pathProvider.getProjectPath());
        return "NOTE: The examples shown here are generated from the source code.\n" +
                "They therefore represent the behavior of the application at any times.\n" +
                "Non regression is ensured by checking the absence of change in this document.\n" +
                "Learn more here link:{github-pages}[]\n\n" +
                "View source of project on link:{github-repo}/" + projectFolderPath.toString() + "[Github]\n\n";
    }

    protected void generate(String packageToScan) throws IOException {
        new Document(build()).saveAs(Paths.get("Documentation.adoc"));
    }

}