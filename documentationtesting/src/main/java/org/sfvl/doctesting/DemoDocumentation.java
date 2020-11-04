package org.sfvl.doctesting;

import java.nio.file.Path;

public class DemoDocumentation extends MainDocumentation {
    public DemoDocumentation() {
    }

    public DemoDocumentation(String documentationTitle) {
        super(documentationTitle);
    }

    public DemoDocumentation(String documentationTitle, Path docRootPath) {
        super(documentationTitle, docRootPath);
    }

    @Override
    protected String generalInformation() {
        final Path projectFolderPath = getGitRootPath().relativize(getProjectPath());
        return "NOTE: The examples shown here are generated from the source code.\n" +
                "They therefore represent the behavior of the application at any times.\n" +
                "Non regression is ensured by checking the absence of change in this document.\n" +
                "Learn more here link:{github-pages}[]\n\n" +
                "View source of project on link:{github-repo}/" + projectFolderPath.toString() + "[Github]\n\n";
    }

}
