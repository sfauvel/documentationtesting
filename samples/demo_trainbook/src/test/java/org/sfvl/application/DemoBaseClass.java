package org.sfvl.application;

import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.HtmlPageExtension;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.doctesting.utils.PathProvider;

import java.nio.file.Path;


public abstract class DemoBaseClass {

    @RegisterExtension
    static ApprovalsExtension doc = new SimpleApprovalsExtension();

    @RegisterExtension
    static HtmlPageExtension index = new HtmlPageExtension("index");

    public void note_demo() {
        final PathProvider pathProvider = new PathProvider();
        final Path projectFolderPath = pathProvider.getGitRootPath().relativize(pathProvider.getProjectPath());
        doc.write("NOTE: The examples shown here are generated from the source code.",
                "They therefore represent the behavior of the application at any times.",
                "Non regression is ensured by checking the absence of change in this document.",
                "Learn more here link:{github-pages}[]",
                "",
                "View source of project on link:{github-repo}/" + projectFolderPath + "[Github]");
    }
}
